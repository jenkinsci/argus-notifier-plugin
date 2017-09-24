package org.jenkinsci.plugins.argusnotifier;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class PeriodicJenkinsMetricsSender extends AsyncPeriodicWork {
    private static final String ARGUS_NOTIFIER_PERIODIC_WORK = "Argus Notifier Periodic Sender";
    private static final Logger logger = Logger.getLogger(ArgusRunListener.class.getName());

    public PeriodicJenkinsMetricsSender() {
        super(ARGUS_NOTIFIER_PERIODIC_WORK);
    }

    @Override
    protected void execute(TaskListener taskListener) throws IOException, InterruptedException {
        Jenkins instance = Jenkins.getInstance();
        ArgusNotifier.DescriptorImpl argusNotifierDescriptor =
                (ArgusNotifier.DescriptorImpl) instance.getDescriptor(ArgusNotifier.class);
        if (argusNotifierDescriptor != null &&
                argusNotifierDescriptor.isNotifierConfigured() && argusNotifierDescriptor.isSendSystemMetrics()) {

            OffsetDateTime now = OffsetDateTime.now();
            long metricTimestamp = now.toEpochSecond();

            SystemMetricFactory systemMetricFactory =
                    new SystemMetricFactory(instance, metricTimestamp, argusNotifierDescriptor.getScope());
            ImmutableList.Builder<Metric> metricListBuilder = ImmutableList.builder();
            ImmutableList.Builder<Annotation> annotationBuilder = ImmutableList.builder();
            final MetricRegistry metricRegistry = Metrics.metricRegistry();
            metricRegistry.getGauges().forEach((String key, Gauge value) -> {
                final Object gaugeVal = value.getValue();
                if (gaugeVal instanceof Number) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest(String.format("Sending %s = %s", key, ((Number) gaugeVal).doubleValue()));
                    }
                    metricListBuilder.add(systemMetricFactory.getMetric(key, (Number) gaugeVal));
                } else {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest(String.format("Currently unsupported: %s = %s", key, gaugeVal.getClass().getName()));
                    }
                }
            });
            ArgusConnectionInfo argusConnectionInfo = argusNotifierDescriptor.getArgusConnectionInfo();
            ArgusDataSender.sendArgusData(argusConnectionInfo, metricListBuilder.build(), annotationBuilder.build());
        }
    }

    @Override
    public long getRecurrencePeriod() {
        return MIN;
    }
}
