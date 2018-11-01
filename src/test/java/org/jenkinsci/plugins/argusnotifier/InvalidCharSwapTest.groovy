package org.jenkinsci.plugins.argusnotifier

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class InvalidCharSwapTest extends Specification {

    def "#input should yield #expectedOutput"() {
        when:
        String output = InvalidCharSwap.swapWithDash(input)

        then:
        output == expectedOutput

        where:
        input     | expectedOutput
        "blah+"   | "blah-"
        "++++"    | "----"
        "blah%2F" | "blah-"
        "%2F%2F"  | "--"
        "-%2F+"   | "---"
        "-%2F-"   | "---"
        ".%2F-"   | ".--"
        "aA-_./"  | "aA-_./"
    }
}
