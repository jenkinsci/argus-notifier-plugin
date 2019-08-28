package org.jenkinsci.plugins.argusnotifier

import hudson.model.*
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.MockFolder
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JenkinsRunFormatterTest extends Specification {
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule()

    private static final String SOMEHOST = "somehost"
    private static final GString JENKINS_URL_NO_SLASH = "https://$SOMEHOST"
    private static final String TEST_BUILD_URL = "job/test/42/"
    private AbstractBuild build = Mock(AbstractBuild)
    private Jenkins jenkins = Mock(Jenkins)
    private LogTaskListener listener = Mock(LogTaskListener)

    def setup() {
        build.getResult() >> Result.SUCCESS
    }

    def "formatBuildUrl=#expectedUrl where jenkinsUrl=#jenkinsUrl"() {
        given:
        jenkins.getRootUrl() >> jenkinsUrl
        build.getUrl() >>  TEST_BUILD_URL
        JenkinsRunFormatter buildFormatter = new JenkinsRunFormatter(jenkins, build)

        when:
        String formattedBuildUrl = buildFormatter.getRunUrl()

        then:
        formattedBuildUrl == expectedUrl

        where:
        jenkinsUrl               | expectedUrl
        JENKINS_URL_NO_SLASH     | "$JENKINS_URL_NO_SLASH/$TEST_BUILD_URL"
        "$JENKINS_URL_NO_SLASH/" | "$JENKINS_URL_NO_SLASH/$TEST_BUILD_URL"
        null                     | TEST_BUILD_URL
    }

    def "test getProjectName substitutes slashes in full name properly"() {
        given:
        def folderName = "myfolder"
        MockFolder folder = jenkinsRule.createFolder(folderName)
        def projectName = "testproject"
        Item project = folder.createProject(FreeStyleProject, projectName)
        FreeStyleBuild freeStyleBuild = project.scheduleBuild2(0).get()
        JenkinsRunFormatter buildFormatter = new JenkinsRunFormatter(jenkins, freeStyleBuild)

        when:
        String actualProjectName = buildFormatter.getProjectName()

        then:
        actualProjectName == "$folderName.$projectName"
    }

    def "test getBuildNumberString"() {
        given:
        def buildNumber = 42
        build.getNumber() >> buildNumber
        JenkinsRunFormatter jenkinsBuildFormatter = new JenkinsRunFormatter(jenkins, build)

        when:
        String actualBuildNumberString = jenkinsBuildFormatter.getBuildNumberString()

        then:
        actualBuildNumberString == String.valueOf(buildNumber)
    }

    def "test getGitCommit for #commitId returns commit sha #expectedGitCommit"() {
        given:
        build.getEnvironment(_) >> [(TagFactory.Tag.GIT_COMMIT.toString()): commitId]
        JenkinsRunFormatter jenkinsBuildFormatter = new JenkinsRunFormatter(jenkins, build)

        when:
        String actualGitCommit = jenkinsBuildFormatter.getGitCommit()

        then:
        actualGitCommit == expectedGitCommit

        where:
        commitId                                   | expectedGitCommit
        "0fc255bb26fcc4a0548c3ca14caec6a7d6de7c25" | "0fc255bb26fcc4a0548c3ca14caec6a7d6de7c25"
        "f6fd56be96ccb7f445c6d8058b76b4c2482a50a6" | "f6fd56be96ccb7f445c6d8058b76b4c2482a50a6"
    }

    def "test getGitCommit no such env variable"() {
        given:
        build.getEnvironment(_) >> [:]
        JenkinsRunFormatter jenkinsBuildFormatter = new JenkinsRunFormatter(jenkins, build)

        when:
        String actualGitCommit = jenkinsBuildFormatter.getGitCommit()

        then:
        actualGitCommit == ""
    }

    def "test getContextualResult properly returns FIXED"() {
        // No need to test all of the corner cases again
        given:
        AbstractBuild previousBuild = Mock(AbstractBuild)
        previousBuild.getResult() >> Result.FAILURE
        build.getPreviousBuild() >> previousBuild
        JenkinsRunFormatter buildFormatter = new JenkinsRunFormatter(jenkins, build)

        when:
        String actualContextualResult = buildFormatter.getContextualResult()

        then:
        actualContextualResult == BuildResultsResolver.FIXED
    }
}
