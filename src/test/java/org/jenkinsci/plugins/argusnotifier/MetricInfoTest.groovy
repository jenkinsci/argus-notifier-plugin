package org.jenkinsci.plugins.argusnotifier

import hudson.model.AbstractBuild
import hudson.model.Job
import hudson.model.Result
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime

@Unroll
class MetricInfoTest extends Specification {

    private static final String TEST_PROJECT_NAME = "test"
    private static final String TEST_JENKINS_URL = "https://myjenkins"
    private AbstractBuild build

    def setup() {
        build = Mock(AbstractBuild)
        build.getResult() >> Result.SUCCESS
        Job parentProject = Mock(Job)
        build.getParent() >> parentProject
        parentProject.getName() >> TEST_PROJECT_NAME
    }

    def "test getJenkinsUrl"() {
        given:
        String jenkinsUrl = "testurl"

        when:
        MetricInfo argusMetricInfo = new MetricInfo(jenkinsUrl, build, 1L)

        then:
        argusMetricInfo.getJenkinsUrl() == jenkinsUrl
    }

    def "getHostName()=#expectedHostName when jenkinsUrl = #jenkinsUrl"() {
        when:
        MetricInfo argusMetricInfo = new MetricInfo(jenkinsUrl, build, 1L)

        then:
        argusMetricInfo.getHostName() == expectedHostName

        where:
        // Run through some checks but not all corner cases
        jenkinsUrl                  | expectedHostName
        "https://httpsjenkins"      | "httpsjenkins"
        "http://httpjenkins"        | "httpjenkins"
        "https://portjenkins:8080"  | "portjenkins"
        "http://urijenkins/jenkins" | "urijenkins"
    }

    def "getBuildUrl w/jenkinsUrl=#jenkinsUrl formatted properly"() {
        given:
        String BUILD_URL = "job/test/42/"
        build.getUrl() >>  BUILD_URL

        when:
        MetricInfo argusMetricInfo = new MetricInfo(TEST_JENKINS_URL, build, 1L)

        then:
        argusMetricInfo.getBuildUrl() == "$TEST_JENKINS_URL/$BUILD_URL"

        where:
        jenkinsUrl << [TEST_JENKINS_URL, "$TEST_JENKINS_URL/", null]
    }

    def "test getProjectName"() {
        when:
        MetricInfo argusMetricInfo = new MetricInfo(TEST_JENKINS_URL, build, 1L)

        then:
        argusMetricInfo.getProjectName() == TEST_PROJECT_NAME
    }

    def "test getContextualResult properly returns FIXED"() {
        // No need to test all of the corner cases again
        given:
        AbstractBuild previousBuild = Mock(AbstractBuild)
        previousBuild.getResult() >> Result.FAILURE
        build.getPreviousBuild() >> previousBuild

        when:
        MetricInfo argusMetricInfo = new MetricInfo(TEST_JENKINS_URL, build, 1L)

        then:
        argusMetricInfo.getContextualResult() == BuildResultsResolver.FIXED
    }

    def "test getResult"() {
        when:
        MetricInfo argusMetricInfo = new MetricInfo(TEST_JENKINS_URL, build, 1L)

        then:
        argusMetricInfo.getResult() == Result.SUCCESS.toString()
    }

    def "test getMetricTimestamp & getMetricTimestampString"() {
        given:
        OffsetDateTime now = OffsetDateTime.now()

        when:
        MetricInfo argusMetricInfo = new MetricInfo(TEST_JENKINS_URL, build, now.toEpochSecond())

        then:
        argusMetricInfo.getMetricTimestamp() == now.toEpochSecond()
        argusMetricInfo.getMetricTimestampString() == String.valueOf(now.toEpochSecond())
    }

    def "test getBuildNumber"() {
        given:
        def buildNumber = 42
        build.getNumber() >> buildNumber

        when:
        MetricInfo argusMetricInfo = new MetricInfo(TEST_JENKINS_URL, build, 1L)

        then:
        argusMetricInfo.getBuildNumber() == buildNumber
        argusMetricInfo.getBuildNumberString() == String.valueOf(buildNumber)
    }
}
