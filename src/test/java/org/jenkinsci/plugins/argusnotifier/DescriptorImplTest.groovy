package org.jenkinsci.plugins.argusnotifier

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class DescriptorImplTest extends Specification {

    def "#inputUrl should be #expectedUrl after stripTrailingSlash"() {
        when:
        String actualUrl = ArgusNotifier.DescriptorImpl.stripTrailingSlash(inputUrl)

        then:
        actualUrl == expectedUrl

        where:
        inputUrl          | expectedUrl
        "http://test/ws/" | "http://test/ws"
        "http://test/ws"  | "http://test/ws"
    }

    def "#inputUrl should be returned by stripTrailingSlash"() {
        when:
        String actualUrl = ArgusNotifier.DescriptorImpl.stripTrailingSlash(inputUrl)

        then:
        actualUrl == expectedUrl

        where:
        inputUrl          | expectedUrl
        ""                | ""
        null              | null
    }

}
