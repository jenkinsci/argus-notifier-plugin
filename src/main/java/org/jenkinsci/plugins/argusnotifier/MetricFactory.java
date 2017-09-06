package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.model.AbstractBuild;
import jenkins.metrics.impl.TimeInQueueAction;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Map;

class MetricFactory {

    static final String BUILD_STATUS = "build.status";
    static final String BUILD_TIME_METRIC = "build.time";
    static final String QUEUE_TIME_METRIC = "queue.time";
    static final String TOTAL_BUILD_TIME_METRIC = "total.build.time";
    static final String BUILD_STATUS_LABEL = "Build Status";
    static final String BUILD_TIME_LABEL = "Build Time";
    static final String QUEUE_TIME_LABEL = "Queue Time";
    static final String TOTAL_BUILD_TIME_LABEL = "Total Build Time";

    private final AbstractBuild build;
    private final long metricTimestamp;
    private final String scope;
    private final JenkinsBuildFormatter jenkinsBuildFormatter;

    MetricFactory(Jenkins jenkins, AbstractBuild build, long metricTimestamp, String scope) {
        this.build = build;
        this.jenkinsBuildFormatter = new JenkinsBuildFormatter(jenkins, build);
        this.metricTimestamp = metricTimestamp;
        this.scope = scope;
    }

    Metric getBuildStatusMetric() {
        Metric metric = new Metric();
        metric.setScope(scope);
        metric.setDisplayName(getDisplayName(BUILD_STATUS_LABEL));
        metric.setMetric(BUILD_STATUS);
        // TODO: metric.setNamespace(projectName);

        metric.setTags(TagFactory.buildStatusTags(jenkinsBuildFormatter.getHostName(),
                jenkinsBuildFormatter.getProjectName()));
        Map<Long, Double> datapoints =
                ImmutableMap.<Long, Double>builder()
                        .put(metricTimestamp, BuildResultsResolver.translateResultToNumber(build.getResult()))
                        .build();
        metric.setDatapoints(datapoints);
        return metric;
    }

    List<Metric> getBuildTimeMetrics() {
        TimeInQueueAction timeInQueueAction = build.getAction(jenkins.metrics.impl.TimeInQueueAction.class);
        return ImmutableList.of(
                getBuildTimeMetric(BUILD_TIME_LABEL, BUILD_TIME_METRIC, timeInQueueAction.getBuildingDurationMillis()),
                getBuildTimeMetric(QUEUE_TIME_LABEL, QUEUE_TIME_METRIC, timeInQueueAction.getQueuingDurationMillis()),
                getBuildTimeMetric(TOTAL_BUILD_TIME_LABEL, TOTAL_BUILD_TIME_METRIC, timeInQueueAction.getTotalDurationMillis()));
    }

    String getDisplayName(String label) {
        return jenkinsBuildFormatter.getProjectName() + ": " + label;
    }

    private Metric getBuildTimeMetric(String labelForDisplayName, String metricString, long timeInMillis) {
        Metric metric = new Metric();
        metric.setScope(scope);
        metric.setDisplayName(getDisplayName(labelForDisplayName));
        metric.setMetric(metricString);
        metric.setUnits("seconds");
        // TODO: metric.setNamespace(projectName);
        double timeInSeconds = (double) timeInMillis / 1000.0;

        metric.setTags(TagFactory.buildStatusTags(jenkinsBuildFormatter.getHostName(),
                jenkinsBuildFormatter.getProjectName()));
        Map<Long, Double> datapoints =
                ImmutableMap.<Long, Double>builder()
                        .put(metricTimestamp, timeInSeconds)
                        .build();
        metric.setDatapoints(datapoints);
        return metric;
    }
}
