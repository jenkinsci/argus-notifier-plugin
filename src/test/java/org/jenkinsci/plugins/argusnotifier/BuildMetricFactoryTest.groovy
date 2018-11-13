package org.jenkinsci.plugins.argusnotifier

import com.salesforce.dva.argus.sdk.entity.Metric
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
}
