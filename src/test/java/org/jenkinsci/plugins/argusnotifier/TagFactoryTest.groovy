package org.jenkinsci.plugins.argusnotifier

import spock.lang.Specification
import spock.lang.Unroll

import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.HOST
import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.PROJECT

@Unroll
class TagFactoryTest extends Specification {

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
        given:
        String rootUrl = "root"

        when:
        def tags = TagFactory.buildStatusTags(rootUrl, "")

        then:
        tags[HOST.lower()] == rootUrl
    }

    def 'project makes it in as project tag'() {
        given:
        String project = "myProject"

        when:
        def tags = TagFactory.buildStatusTags("", project)

        then:
        tags[PROJECT.lower()] == project
    }
}
