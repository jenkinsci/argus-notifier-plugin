package org.jenkinsci.plugins.argusnotifier

import hudson.model.AbstractBuild
import hudson.model.Job
import hudson.model.Result
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JenkinsBuildFormatterTest extends Specification {

    private static final String SOMEHOST = "somehost"
    private static final GString JENKINS_URL_NO_SLASH = "https://$SOMEHOST"
    private static final String TEST_BUILD_URL = "job/test/42/"
    private static final String TEST_PROJECT_NAME = "test"
    private AbstractBuild build = Mock(AbstractBuild)
    private Jenkins jenkins = Mock(Jenkins)

    def setup() {
        build.getResult() >> Result.SUCCESS
    }


    def 'hostNameFromUrl: #inputUrl should be #hostName'() {
        given:
        jenkins.getRootUrl() >> inputUrl
        JenkinsBuildFormatter buildFormatter = new JenkinsBuildFormatter(jenkins, build)

        when:
        String result = buildFormatter.getHostName()

        then:
        result == hostName

        where:
        inputUrl                       | hostName
        "https://$SOMEHOST"            | SOMEHOST
        "http://$SOMEHOST"             | SOMEHOST
        "$SOMEHOST:80"                 | SOMEHOST
        "$SOMEHOST/jenkins"            | SOMEHOST
        "https://$SOMEHOST:80/jenkins" | SOMEHOST
    }

    def "formatBuildUrl=#expectedUrl where jenkinsUrl=#jenkinsUrl"() {
        given:
        jenkins.getRootUrl() >> jenkinsUrl
        build.getUrl() >>  TEST_BUILD_URL
        JenkinsBuildFormatter buildFormatter = new JenkinsBuildFormatter(jenkins, build)

        when:
        String formattedBuildUrl = buildFormatter.getBuildUrl()

        then:
        formattedBuildUrl == expectedUrl

        where:
        jenkinsUrl               | expectedUrl
        JENKINS_URL_NO_SLASH     | "$JENKINS_URL_NO_SLASH/$TEST_BUILD_URL"
        "$JENKINS_URL_NO_SLASH/" | "$JENKINS_URL_NO_SLASH/$TEST_BUILD_URL"
        null                     | TEST_BUILD_URL
    }

    def "test getProjectName"() {
        given:
        Job parentProject = Mock(Job)
        build.getParent() >> parentProject
        parentProject.getName() >> TEST_PROJECT_NAME
        JenkinsBuildFormatter buildFormatter = new JenkinsBuildFormatter(jenkins, build)

        when:
        String actualProjectName = buildFormatter.getProjectName()

        then:
        actualProjectName == TEST_PROJECT_NAME
    }

    def "test getBuildNumberString"() {
        given:
        def buildNumber = 42
        build.getNumber() >> buildNumber
        JenkinsBuildFormatter jenkinsBuildFormatter = new JenkinsBuildFormatter(jenkins, build)

        when:
        String actualBuildNumberString = jenkinsBuildFormatter.getBuildNumberString()

        then:
        actualBuildNumberString == String.valueOf(buildNumber)
    }

    def "test getContextualResult properly returns FIXED"() {
        // No need to test all of the corner cases again
        given:
        AbstractBuild previousBuild = Mock(AbstractBuild)
        previousBuild.getResult() >> Result.FAILURE
        build.getPreviousBuild() >> previousBuild
        JenkinsBuildFormatter buildFormatter = new JenkinsBuildFormatter(jenkins, build)

        when:
        String actualContextualResult = buildFormatter.getContextualResult()

        then:
        actualContextualResult == BuildResultsResolver.FIXED
    }
}
