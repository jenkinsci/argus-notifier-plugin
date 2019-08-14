package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import jenkins.metrics.impl.TimeInQueueAction;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Map;

class BuildMetricFactory {

    static final String BUILD_STATUS = "build.status";
    static final String BUILD_TIME_METRIC = "build.time";
    static final String QUEUE_TIME_METRIC = "queue.time";
    static final String TOTAL_BUILD_TIME_METRIC = "total.build.time";
    static final String NUMERIC_BUILD_STATUS_LABEL = "Numeric Build Status";
    static final String BUILD_TIME_LABEL = "Build Time";
    static final String QUEUE_TIME_LABEL = "Queue Time";
    static final String TOTAL_BUILD_TIME_LABEL = "Total Build Time";

    private final Run run;
    private final long metricTimestamp;
    private final String scope;
    private final JenkinsRunFormatter jenkinsRunFormatter;
    private final Jenkins jenkins;

    BuildMetricFactory(Jenkins jenkins, Run run, long metricTimestamp, String scope) {
        this.jenkins = jenkins;
        this.run = run;
        this.jenkinsRunFormatter = new JenkinsRunFormatter(jenkins, run);
        this.metricTimestamp = metricTimestamp;
        this.scope = scope;
    }

    List<Metric> getBuildStatusMetrics() {
        Metric statusTranslatedToNumberMetric = new Metric();
        statusTranslatedToNumberMetric.setScope(scope);
        statusTranslatedToNumberMetric.setDisplayName(getDisplayName(NUMERIC_BUILD_STATUS_LABEL));
        statusTranslatedToNumberMetric.setMetric(BUILD_STATUS);
        // TODO: statusTranslatedToNumberMetric.setNamespace(projectName);

        statusTranslatedToNumberMetric
                .setTags(TagFactory.buildStatusTags(jenkins, jenkinsRunFormatter.getProjectName()));
        Map<Long, Double> numericStatusDatapoints = ImmutableMap.<Long, Double>builder()
                .put(metricTimestamp, BuildResultsResolver.translateResultToNumber(run.getResult())).build();
        statusTranslatedToNumberMetric.setDatapoints(numericStatusDatapoints);
        Metric buildStatusMetric = new Metric();
        buildStatusMetric.setScope(scope);
        buildStatusMetric.setDisplayName(getDisplayName(BuildResultsResolver.getResultString(run.getResult())));
        buildStatusMetric.setMetric(BuildResultsResolver.getMetricName(run.getResult()));
        buildStatusMetric.setTags(TagFactory.buildStatusTags(jenkins, jenkinsRunFormatter.getProjectName()));
        Map<Long, Double> buildStatusDatapoints = ImmutableMap.<Long, Double>builder().put(metricTimestamp, 1.0)
                .build();
        buildStatusMetric.setDatapoints(buildStatusDatapoints);
        return ImmutableList.of(statusTranslatedToNumberMetric, buildStatusMetric);
    }

    List<Metric> getBuildTimeMetrics() {
        TimeInQueueAction timeInQueueAction = run.getAction(jenkins.metrics.impl.TimeInQueueAction.class);
        long buildingDurationMillis;
        long totalDurationMillis;
        // Working around https://issues.jenkins-ci.org/browse/JENKINS-46945
        if (run instanceof AbstractBuild) {
            buildingDurationMillis = timeInQueueAction.getBuildingDurationMillis();
            totalDurationMillis = timeInQueueAction.getTotalDurationMillis();
        } else {
            buildingDurationMillis = Math.max(0L, System.currentTimeMillis() - run.getStartTimeInMillis());
            totalDurationMillis = timeInQueueAction.getQueuingDurationMillis() + buildingDurationMillis;
        }
        return ImmutableList.of(getBuildTimeMetric(BUILD_TIME_LABEL, BUILD_TIME_METRIC, buildingDurationMillis),
                getBuildTimeMetric(QUEUE_TIME_LABEL, QUEUE_TIME_METRIC, timeInQueueAction.getQueuingDurationMillis()),
                getBuildTimeMetric(TOTAL_BUILD_TIME_LABEL, TOTAL_BUILD_TIME_METRIC, totalDurationMillis));
    }

    private String getDisplayName(String label) {
        return jenkinsRunFormatter.getProjectName() + ": " + label;
    }

    private Metric getBuildTimeMetric(String labelForDisplayName, String metricString, long timeInMillis) {
        Metric metric = new Metric();
        metric.setScope(scope);
        metric.setDisplayName(getDisplayName(labelForDisplayName));
        metric.setMetric(metricString);
        metric.setUnits("seconds");
        // TODO: metric.setNamespace(projectName);
        double timeInSeconds = (double) timeInMillis / 1000.0;

        String gitCommitId = jenkinsRunFormatter.getGitCommit();
        if (labelForDisplayName == TOTAL_BUILD_TIME_LABEL && !gitCommitId.isEmpty()) {
            metric.setTags(TagFactory.buildExtendedStatusTags(jenkins, jenkinsRunFormatter.getProjectName(),
                    jenkinsRunFormatter.getBuildNumberString(), gitCommitId));
        } else {
            metric.setTags(TagFactory.buildStatusTags(jenkins, jenkinsRunFormatter.getProjectName()));
        }
        Map<Long, Double> datapoints = ImmutableMap.<Long, Double>builder().put(metricTimestamp, timeInSeconds).build();
        metric.setDatapoints(datapoints);
        return metric;
    }
}
