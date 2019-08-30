package org.jenkinsci.plugins.argusnotifier

import spock.lang.Specification
import spock.lang.Unroll

import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.BUILD_NUMBER
import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.BUILD_STATUS
import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.GIT_COMMIT
import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.HOST
import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.PROJECT

@Unroll
class TagFactoryTest extends Specification {

    public static final PROJECT_VALUE = "proj"
    public static final HOST_VALUE = "hosty"
    public static final String COMMIT_HASH_VALUE = "abcdef123"
    public static final String RESULT_VALUE = "SUCCESS"
    public static final String BUILD_NUMBER_VALUE = "42"
    private JenkinsRunFormatter jenkinsRunFormatter = Mock(JenkinsRunFormatter)

    def '#tag is lowercased with lower()'() {
        when:
        String actualResult = tag.lower()

        then:
        actualResult == expectedValue

        where:
        tag          | expectedValue
        HOST         | "host"
        PROJECT      | "project"
        BUILD_NUMBER | "build_number"
        BUILD_STATUS | "build_status"
        GIT_COMMIT   | "git_commit"
    }

    def '#tag tag == #value in buildStatusTags; GIT_COMMIT = "#commitHashValue"'() {

        given:
        jenkinsRunFormatter.getProjectName() >> PROJECT_VALUE
        jenkinsRunFormatter.getJenkinsHostName() >> HOST_VALUE
        jenkinsRunFormatter.getBuildNumberString() >> BUILD_NUMBER_VALUE
        jenkinsRunFormatter.getResult() >> RESULT_VALUE
        jenkinsRunFormatter.getGitCommitHash() >> commitHashValue

        when:
        def tags = TagFactory.buildStatusTags(jenkinsRunFormatter)

        then:
        tags[tag.lower()] == value

        where:
        tag          | value              | commitHashValue
        HOST         | HOST_VALUE         | ""
        PROJECT      | PROJECT_VALUE      | ""
        BUILD_NUMBER | BUILD_NUMBER_VALUE | ""
        BUILD_STATUS | RESULT_VALUE       | ""
        GIT_COMMIT   | COMMIT_HASH_VALUE  | COMMIT_HASH_VALUE
        GIT_COMMIT   | null               | ""
    }
}
