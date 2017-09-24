package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Metric;
import jenkins.model.Jenkins;

import java.util.Map;

public class SystemMetricFactory {

    private final Jenkins jenkins;
    private final long metricTimestamp;
    private final String scope;

    /**
     * Provides factory methods to provide Argus Metrics for the Jenkins system. All metrics will carry the
     * scope provided here, a host tag from the Jenkins host, and the datapoint will be based on the metricTimestamp
     *
     * @param jenkins - used to generate the host tag
     * @param metricTimestamp - used for all metric datapoints
     * @param scope - scope to be utilized for the metrics
     */
    SystemMetricFactory(Jenkins jenkins, long metricTimestamp, String scope) {
        this.jenkins = jenkins;
        this.metricTimestamp = metricTimestamp;
        this.scope = scope;
    }

    /**
     * Get a Metric set where the metric will be name and the value will be the datapoint for the
     * metricTimestamp for this SystemMetricFactory
     *
     * @param name - metric name
     * @param numericValue - value of the datapoint at metricTimestamp time
     * @return Argus Metric
     */
    Metric getMetric(String name, Number numericValue) {
        Metric metric = new Metric();
        metric.setScope(scope);
        metric.setMetric(name);

        metric.setTags(TagFactory.hostTag(jenkins));
        Map<Long, Double> datapoints =
                ImmutableMap.<Long, Double>builder()
                        .put(metricTimestamp, numericValue.doubleValue())
                        .build();
        metric.setDatapoints(datapoints);
        return metric;
    }
}
