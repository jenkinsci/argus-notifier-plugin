package org.jenkinsci.plugins.argusnotifier

import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.HOST
import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.PROJECT

@Unroll
class TagFactoryTest extends Specification {

    public static final String ROOT_URL = "root"
    private Jenkins jenkins = Mock(Jenkins)

    def setup() {
        jenkins.getRootUrl() >> ROOT_URL
    }

    def '#tag is lowercased with lower()'() {
        when:
        String actualResult = tag.lower()

        then:
        actualResult == expectedValue

        where:
        tag     | expectedValue
        HOST    | "host"
        PROJECT | "project"
    }

    def 'rootUrl makes it in as host tag'() {
        when:
        def tags = TagFactory.buildStatusTags(jenkins, "")

        then:
        tags[HOST.lower()] == ROOT_URL
    }

    def 'project makes it in as project tag'() {
        given:
        String project = "myProject"

        when:
        def tags = TagFactory.buildStatusTags(jenkins, project)

        then:
        tags[PROJECT.lower()] == project
    }
}
