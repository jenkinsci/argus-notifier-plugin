package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.collect.ImmutableList;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Our ArgusRunListener is a RunListener that is used to send metrics to Argus for all completed builds on the system.
 */
@Extension
public class ArgusRunListener extends RunListener<Run> {
    private static final Logger logger = Logger.getLogger(ArgusRunListener.class.getName());

    public ArgusRunListener() {
        super(Run.class);
    }

    /**
     * Override the onCompleted method to send metrics and annotations for all completed builds on a system.
     *
     * @param run the run to generate metrics from
     * @param listener listener that we could use to write to the run logger
     */
    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        Jenkins instance = Jenkins.getInstance();
        ArgusNotifier.DescriptorImpl argusNotifierDescriptor =
                (ArgusNotifier.DescriptorImpl) instance.getDescriptor(ArgusNotifier.class);
        if (argusNotifierDescriptor != null &&
                argusNotifierDescriptor.isNotifierConfigured() && argusNotifierDescriptor.isSendForAllBuilds()) {
            String credentialsId = argusNotifierDescriptor.getCredentialsId();
            UsernamePasswordCredentials credentials = ArgusNotifier.getCredentialsById(credentialsId);

            OffsetDateTime now = OffsetDateTime.now();
            long metricTimestamp = now.toEpochSecond();
            String argusUrl = argusNotifierDescriptor.getArgusUrl();
            String scope = argusNotifierDescriptor.getScope();
            String source = argusNotifierDescriptor.getSource();

            BuildMetricFactory buildMetricFactory = new BuildMetricFactory(instance, run, metricTimestamp, scope);

            List<Metric> metrics =
                    ImmutableList.<Metric>builder()
                            .add(buildMetricFactory.getBuildStatusMetric())
                            .addAll(buildMetricFactory.getBuildTimeMetrics())
                            .build();

            AnnotationFactory annotationFactory = new AnnotationFactory(instance, run, metricTimestamp, scope, source);
            List<Annotation> annotations = annotationFactory.getAnnotationsFor(metrics);

            if (logger.isLoggable(Level.INFO)) {
                logger.info(MessageFormat.format("Sending metrics to: {0} with username: {1}",
                        argusUrl,
                        credentials.getUsername()));
            }
            ArgusDataSender.sendArgusData(argusUrl, credentials, metrics, annotations);
        }
    }
}
