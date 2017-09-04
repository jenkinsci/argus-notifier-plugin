package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

import java.util.Map;

class MetricFactory {

    static final String BUILD_STATUS = "build.status";
    static final String BUILD_STATUS_LABEL = "Build Status";

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
        metric.setDisplayName(BUILD_STATUS_LABEL);
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
}
