package org.jenkinsci.plugins.argusnotifier

import org.apache.commons.jelly.impl.TagFactory

import com.salesforce.dva.argus.sdk.entity.Metric

import groovy.json.StringEscapeUtils
import hudson.model.ItemGroup
import hudson.model.Job
import hudson.model.Run
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BuildMetricFactoryTest extends Specification {

    public static final String FULL_JOB_NAME = "somejob"
    private Jenkins jenkins = Mock(Jenkins)
    private Run run = Mock(Run)
    private ItemGroup folder = Mock(ItemGroup)

    def setup() {
        jenkins.rootUrl >> "jenkinsrooturl"
        Job folderParent = Mock(Job)
        folderParent.name >> FULL_JOB_NAME
        folderParent.getParent() >> folder
        run.parent >> folderParent
    }

    def '"#jobFolderName" folder - build status metric is valid'() {
        given:
        long metricTimestamp = 42L
        String scope = "testScope"
        folder.fullName >> jobFolderName
        BuildMetricFactory metricFactory = new BuildMetricFactory(jenkins, run, metricTimestamp, scope)

        when:
        List<Metric> metrics = metricFactory.buildStatusMetrics
        Metric mainMetric = metrics.get(0)

        then:
        mainMetric.displayName == expectedDisplayName

        where:
        jobFolderName | expectedDisplayName
        "myfolder"    | "myfolder." + FULL_JOB_NAME + ": " + BuildMetricFactory.NUMERIC_BUILD_STATUS_LABEL
        ""            | FULL_JOB_NAME + ": " + BuildMetricFactory.NUMERIC_BUILD_STATUS_LABEL
    }

    def 'Total build time metric has git commit tag'() {
        given:
        long metricTimestamp = 42L
        long totalDuration = 5L
        int buildNumber = 349506
        String scope = "testScope"
        String jobName = "jobFolderName"
        folder.fullName >> jobName
        Map<String, String> envVars = new Expando([get: {String  k -> commitId}]) as Map<String, String>
        run.getEnvVars() >> envVars
        run.getNumber() >> buildNumber

        BuildMetricFactory metricFactory = new BuildMetricFactory(jenkins, run, metricTimestamp, scope)

        when:
        Metric metricTotalTime = metricFactory.getBuildTimeMetric(BuildMetricFactory.TOTAL_BUILD_TIME_LABEL,BuildMetricFactory.TOTAL_BUILD_TIME_METRIC,totalDuration)

        then:
        if (commitId.isEmpty()) {
            metricTotalTime.tags['git_commit'] == expectedCommit
            metricTotalTime.tags['build_number'] == String.valueOf(buildNumber)
            metricTotalTime.tags['project'] == jobName
        }
        else {
            metricTotalTime.tags['git_commit'] == null
            metricTotalTime.tags['build_number'] == null
            metricTotalTime.tags['project'] == jobName
        }

        where:
        commitId                  | expectedCommit
        "32ds34frg4534ff34fdcdds" | "32ds34frg4534ff34fdcdds"
        ""                        | null
    }
}
