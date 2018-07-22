package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.collect.ImmutableList;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.User;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link ArgusNotifier} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked. 
 *
 * @author Justin Harringa
 */
public class ArgusNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(ArgusNotifier.class.getName());

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ArgusNotifier() {
    }

    /**
     * Return true to ensure that we run <b>after</b> the build has been finalized (as the method suggests).
     * @return true
     */
    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        ArgusConnectionInfo argusConnectionInfo = getDescriptor().getArgusConnectionInfo();
        String scope = getDescriptor().scope;
        String source = getDescriptor().source;

        Jenkins jenkins = Jenkins.getInstance();

        OffsetDateTime now = OffsetDateTime.now();
        long metricTimestamp = now.toEpochSecond();

        BuildMetricFactory buildMetricFactory = new BuildMetricFactory(jenkins, build, metricTimestamp, scope);

        List<Metric> metrics =
                ImmutableList.<Metric>builder()
                        .add(buildMetricFactory.getBuildStatusMetric())
                        .addAll(buildMetricFactory.getBuildTimeMetrics())
                        .build();

        AnnotationFactory annotationFactory = new AnnotationFactory(jenkins, build, metricTimestamp, scope, source);
        List<Annotation> annotations = annotationFactory.getAnnotationsFor(metrics);

        ArgusDataSender.sendArgusData(argusConnectionInfo, metrics, annotations);
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    /**
     * Descriptor for {@link ArgusNotifier}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Symbol("argusNotifier")
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use {@code transient}.
         */
        private String credentialsId, argusUrl, scope, source;
        private boolean sendForAllBuilds = true, sendSystemMetrics = true;

        public String getCredentialsId() {
            return credentialsId;
        }
        public String getArgusUrl() {
            return argusUrl;
        }
        public String getScope() {
            return scope;
        }
        public String getSource() {
            return source;
        }
        public boolean isSendForAllBuilds() {
            return sendForAllBuilds;
        }
        public boolean isSendSystemMetrics() {
            return sendSystemMetrics;
        }
        ArgusConnectionInfo getArgusConnectionInfo() {
            return new ArgusConnectionInfo(argusUrl, getCredentialsById(getCredentialsId()));
        }

        boolean isNotifierConfigured() {
            return credentialsId != null && !credentialsId.trim().isEmpty() &&
                    argusUrl != null && !argusUrl.trim().isEmpty() &&
                    scope != null && !scope.trim().isEmpty();
        }

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Returns whether the current user is logged in and has ADMINISTER permission.
         * @return
         */
        private boolean isUserAdmin() {
            User currentUser = User.current();
            return currentUser != null && currentUser.hasPermission(Jenkins.ADMINISTER);
        }

        /**
         * Test a connection to the given argusUrl with the given credential
         * @param argusUrl - Argus web service URL to test the connection against
         * @param credentialsId - Jenkins credential to use for the test
         * @return
         */
        @RequirePOST
        public FormValidation doTestConnection(@QueryParameter("argusUrl") String argusUrl,
                                               @QueryParameter("credentialsId") String credentialsId) {
            if (isUserAdmin()) {
                if (argusUrl == null || argusUrl.trim().isEmpty() || credentialsId == null || credentialsId.trim().isEmpty()) {
                    return FormValidation.error("Please fill in the connection details.");
                } else if (ArgusDataSender.testConnection(stripTrailingSlash(argusUrl), getCredentialsById(credentialsId))) {
                    return FormValidation.ok("Success!");
                } else {
                    return FormValidation.error(
                            MessageFormat.format("Error connecting to {0}. More details can be found in the logs.",
                                    argusUrl));
                }
            } else {
                logger.warning(
                        String.format("%s (unprivileged) tried to test an Argus connection with the following URL: %s",
                                User.current(), argusUrl));
                return FormValidation.error("Access denied: You have not logged in or do not have administer permissions");
            }
        }

        public FormValidation doCheckCredentialsId(@QueryParameter("credentialsId") String credentialsId,
                                                   @QueryParameter("argusUrl") String argusUrl) {
            UsernamePasswordCredentials credToCheck = getCredentialsById(credentialsId);
            if (credToCheck == null) {
                if (argusUrl != null && !argusUrl.trim().isEmpty()) {
                    return FormValidation.error("Please enter a Username with Password credentials id");
                }
            } else {
                if (credToCheck.getUsername().length() == 0)
                    return FormValidation.error("This credential should have a username");

                if (Secret.toString(credToCheck.getPassword()).length() == 0) {
                    return FormValidation.error("This credential should have a password");
                }
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Argus Notifier";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            credentialsId = formData.getString("credentialsId");
            argusUrl = stripTrailingSlash(formData.getString("argusUrl"));
            scope = formData.getString("scope");
            source = formData.getString("source");
            sendForAllBuilds = formData.getBoolean("sendForAllBuilds");
            sendSystemMetrics = formData.getBoolean("sendSystemMetrics");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        /**
         * There is currently a limitation in the Argus SDK where a trailing slash triggers an authentication error.
         * This appears to be due to the way that they construct URLs. Once that is fixed, we can remove this.
         *
         * @param url url to strip trailing slash from
         * @return a URL without a trailing slash
         */
        static String stripTrailingSlash(String url) {
            if (url != null && url.endsWith("/")) {
                return url.substring(0, url.lastIndexOf('/'));
            }
            return url;
        }

        /**
         * Populate the credentials dropdown box
         * @return A ListBoxModel containing all global credentials
         */
        public ListBoxModel doFillCredentialsIdItems() {
            if (isUserAdmin()) {
                return new StandardListBoxModel()
                        .includeEmptyValue()
                        .withMatching(
                                CredentialsMatchers.allOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)),
                                CredentialsProvider.lookupCredentials(StandardCredentials.class,
                                        Jenkins.getInstance(),
                                        ACL.SYSTEM,
                                        Collections.emptyList())
                        );
            } else {
                logger.warning(
                        String.format("%s (unprivileged) tried to get the list of possible Argus credentials",
                                User.current()));
                return new ListBoxModel();
            }
        }

    }

    /**
     * Helper method to return credentials by id
     * @param id The credentials id
     * @return A UsernamePasswordCredential object that encapsulates usernames and passwords
     */
    static UsernamePasswordCredentials getCredentialsById(String id) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(UsernamePasswordCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.emptyList()), CredentialsMatchers.withId(id));
    }

}

