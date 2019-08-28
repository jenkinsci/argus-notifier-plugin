package org.jenkinsci.plugins.argusnotifier

import com.salesforce.dva.argus.sdk.entity.Metric
import hudson.model.ItemGroup
import hudson.model.Job
import hudson.model.Run
import hudson.model.Result
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

    def 'Total build time metric with #commitId has git commit tag #expectedCommit'() {
        given:
        long metricTimestamp = 42L
        long totalDuration = 5L
        int buildNumber = 349506
        String scope = "testScope"
        String jobFolderName = "a_jobFolderName"
        folder.fullName >> jobFolderName
        String commitTag = TagFactory.Tag.GIT_COMMIT.toString()        
        Map<String, String> envVars = [(commitTag): commitId]
        run.getEnvironment(_) >> envVars
        run.getNumber() >> buildNumber
        run.getResult() >> Result.SUCCESS

        BuildMetricFactory metricFactory = new BuildMetricFactory(jenkins, run, metricTimestamp, scope)

        when:
        Metric metricTotalTime = metricFactory.getBuildTimeMetric(BuildMetricFactory.TOTAL_BUILD_TIME_LABEL,
            BuildMetricFactory.TOTAL_BUILD_TIME_METRIC,totalDuration)

        then:

        metricTotalTime.tags[commitTag.toLowerCase()].equals(expectedCommit)
        metricTotalTime.tags[TagFactory.Tag.BUILD_NUMBER.lower()].equals(commitId.isEmpty() ? null : String.valueOf(buildNumber))
        metricTotalTime.tags[TagFactory.Tag.PROJECT.lower()].startsWith(jobFolderName)
        metricTotalTime.tags[TagFactory.Tag.BUILD_STATUS.lower()].equals(commitId.isEmpty() ? null : Result.SUCCESS.toString())


        where:
        commitId                  | expectedCommit
        "32ds34frg4534ff34fdcdds" | "32ds34frg4534ff34fdcdds"
        ""                        | null
    }
}
