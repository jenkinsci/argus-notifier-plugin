package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.collect.ImmutableList;
import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.excpetions.TokenExpiredException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
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

import java.io.IOException;
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
        OffsetDateTime now = OffsetDateTime.now();
        String argusUrl = getDescriptor().argusUrl;
        String credentialsId = getDescriptor().credentialsId;
        String scope = getDescriptor().scope;
        String source = getDescriptor().source;

        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            logger.warning("Argus Notifier: Could not talk to Jenkins. Skipping...");
            // TODO: Consider adding configurable option to fail build
            return true;
        }
        long metricTimestamp = now.toEpochSecond();

        MetricFactory metricFactory = new MetricFactory(jenkins, build, metricTimestamp, scope);

        List<Metric> metrics =
                ImmutableList.<Metric>builder()
                        .add(metricFactory.getBuildStatusMetric())
                        .addAll(metricFactory.getBuildTimeMetrics())
                        .build();

        AnnotationFactory annotationFactory = new AnnotationFactory(jenkins, build, metricTimestamp, scope, source);
        List<Annotation> annotations = annotationFactory.getAnnotationsFor(metrics);

        try (
                // TODO: URL shouldn't have a '/' at the end? Seems like a potential issue with URL forming in the SDK
                ArgusService service = ArgusService.getInstance(argusUrl, 1)
        ) {
            UsernamePasswordCredentials credentials = getCredentialsById(credentialsId);
            service.getAuthService().login(credentials.getUsername(), credentials.getPassword().getPlainText());

            service.getMetricService().putMetrics(metrics);

            service.getAnnotationService().putAnnotations(annotations);

            logger.info("Argus Notifier: Sent message to Argus successfully!");
            service.getAuthService().logout();
        } catch (TokenExpiredException tokenExpired) {
            logger.warning("Token EXPIRED!!"); //TODO: do something?
        } catch (IOException e) {
            logger.severe("Argus Notifier: Error - " + e.getMessage());
        }
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

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckCredentialsId(@QueryParameter String value) {
            UsernamePasswordCredentials c = getCredentialsById(value);
            // TODO: We should probably validate some of these fields
            if (c == null) {
                return FormValidation.error("Please enter a Username with Password credentials id");
            }

            if(c.getUsername().length() == 0)
                return FormValidation.error("This credential should have a username");

            if (Secret.toString(c.getPassword()).length() == 0) {
                return FormValidation.error("This credential should have a password");
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
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withMatching(
                            CredentialsMatchers.allOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class)),
                            CredentialsProvider.lookupCredentials(StandardCredentials.class,
                                    Jenkins.getInstance(),
                                    ACL.SYSTEM,
                                    Collections.emptyList())
                    );
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

