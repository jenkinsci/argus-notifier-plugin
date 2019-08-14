package org.jenkinsci.plugins.argusnotifier

import org.apache.commons.jelly.impl.TagFactory

import com.google.common.collect.ImmutableMap
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
        String jobFolderName = "a_jobFolderName"
        folder.fullName >> jobFolderName
        String commitTag = "GIT_COMMIT"
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.<String, String>builder().put(commitTag,commitId);
        Map<String, String> envVars = mapBuilder.build();
        run.getEnvVars() >> envVars
        run.getNumber() >> buildNumber

        BuildMetricFactory metricFactory = new BuildMetricFactory(jenkins, run, metricTimestamp, scope)

        when:
        Metric metricTotalTime = metricFactory.getBuildTimeMetric(BuildMetricFactory.TOTAL_BUILD_TIME_LABEL,BuildMetricFactory.TOTAL_BUILD_TIME_METRIC,totalDuration)

        then:

        metricTotalTime.tags[commitTag.toLowerCase()] == expectedCommit
        metricTotalTime.tags['build_number'] == commitId.isEmpty() ? null : String.valueOf(buildNumber)
        metricTotalTime.tags['project'].startsWith(jobFolderName)


        where:
        commitId                  | expectedCommit
        "32ds34frg4534ff34fdcdds" | "32ds34frg4534ff34fdcdds"
        ""                        | null
    }
}
