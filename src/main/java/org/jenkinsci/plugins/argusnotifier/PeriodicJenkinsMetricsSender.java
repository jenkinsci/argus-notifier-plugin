package org.jenkinsci.plugins.argusnotifier;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import jenkins.metrics.api.Metrics;

import java.io.IOException;
import java.util.logging.Logger;

@Extension
public class PeriodicJenkinsMetricsSender extends AsyncPeriodicWork {
    private static final String ARGUS_NOTIFIER_PERIODIC_WORK = "Argus Notifier Periodic Sender";
    private static final Logger logger = Logger.getLogger(PeriodicJenkinsMetricsSender.class.getName());

    public PeriodicJenkinsMetricsSender() {
        super(ARGUS_NOTIFIER_PERIODIC_WORK);
    }

    @Override
    protected void execute(TaskListener taskListener) throws IOException, InterruptedException {
        final MetricRegistry metricRegistry = Metrics.metricRegistry();
        metricRegistry.getGauges().forEach((String key, Gauge value) -> {
            final Object gaugeVal = value.getValue();
            // TODO: do the type stuff here
        });

    }

    @Override
    public long getRecurrencePeriod() {
        return MIN;
    }
}
