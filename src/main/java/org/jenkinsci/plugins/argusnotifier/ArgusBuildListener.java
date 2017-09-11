package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.collect.ImmutableList;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.Extension;
import hudson.model.AbstractBuild;
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
 * Our ArgusBuildListener is a RunListener that is used to send metrics to Argus for all completed builds on the system.
 */
@Extension
public class ArgusBuildListener extends RunListener<AbstractBuild> {
    private static final Logger logger = Logger.getLogger(ArgusBuildListener.class.getName());

    public ArgusBuildListener() {
        super(AbstractBuild.class);
    }

    /**
     * Override the onCompleted method to send metrics and annotations for all completed builds on a system.
     *
     * @param build the build to generate metrics from
     * @param listener listener that we could use to write to the build logger
     */
    @Override
    public void onCompleted(AbstractBuild build, @Nonnull TaskListener listener) {
        final Optional<Jenkins> optionalInstance = JenkinsOptionalConverter.getOptionalInstance(logger);
        if (optionalInstance.isPresent()) {
            Jenkins instance = optionalInstance.get();
            ArgusNotifier.DescriptorImpl argusNotifierDescriptor =
                    (ArgusNotifier.DescriptorImpl) instance.getDescriptor(ArgusNotifier.class);
            if (argusNotifierDescriptor.isNotifierConfigured() && argusNotifierDescriptor.isSendForAllBuilds()) {
                String credentialsId = argusNotifierDescriptor.getCredentialsId();
                UsernamePasswordCredentials credentials = ArgusNotifier.getCredentialsById(credentialsId);

                OffsetDateTime now = OffsetDateTime.now();
                long metricTimestamp = now.toEpochSecond();
                String argusUrl = argusNotifierDescriptor.getArgusUrl();
                String scope = argusNotifierDescriptor.getScope();
                String source = argusNotifierDescriptor.getSource();

                BuildMetricFactory buildMetricFactory = new BuildMetricFactory(instance, build, metricTimestamp, scope);

                List<Metric> metrics =
                        ImmutableList.<Metric>builder()
                                .add(buildMetricFactory.getBuildStatusMetric())
                                .addAll(buildMetricFactory.getBuildTimeMetrics())
                                .build();

                AnnotationFactory annotationFactory = new AnnotationFactory(instance, build, metricTimestamp, scope, source);
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
}
