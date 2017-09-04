package org.jenkinsci.plugins.argusnotifier;

import hudson.model.AbstractBuild;

/**
 * MetricInfo is a value holder for information that will go to Argus metrics
 */
public class MetricInfo {

    private String jenkinsUrl;
    private String hostName;
    private String buildUrl;
    private String projectName;
    private String contextualResult;
    private String result;
    private long metricTimestamp;
    private int buildNumber;

    public MetricInfo(String jenkinsUrl, AbstractBuild build, long metricTimestamp) {
        this.jenkinsUrl = jenkinsUrl;
        this.hostName = HostNameFormatter.getHostNameFromUrl(jenkinsUrl);
        this.buildUrl = formatBuildUrl(jenkinsUrl, build);
        this.projectName = build.getParent().getName();
        this.contextualResult = BuildResultsResolver.getContextualResult(build);
        this.result = BuildResultsResolver.getResultString(build.getResult());
        this.metricTimestamp = metricTimestamp;
        this.buildNumber = build.getNumber();
    }

    private String formatBuildUrl(String jenkinsUrl, AbstractBuild build) {
        if (jenkinsUrl == null) {
            return build.getUrl();
        }
        if (jenkinsUrl.substring(jenkinsUrl.length() - 1, jenkinsUrl.length()).equals("/")) {
            return jenkinsUrl + build.getUrl();
        }
        return jenkinsUrl + '/' + build.getUrl();
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    public String getHostName() {
        return hostName;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getContextualResult() {
        return contextualResult;
    }

    public String getResult() {
        return result;
    }

    public long getMetricTimestamp() {
        return metricTimestamp;
    }

    public String getMetricTimestampString() {
        return String.valueOf(metricTimestamp);
    }

    public int getBuildNumber() {
        return buildNumber;
    }

    public String getBuildNumberString() {
        return String.valueOf(buildNumber);
    }
}
