package org.jenkinsci.plugins.argusnotifier

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class HostNameFormatterTest extends Specification {

    private static final String SOMEHOST = "somehost"

    def '#inputUrl should be #hostName'() {
        when:
        String result = HostNameFormatter.getHostNameFromUrl(inputUrl)

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
}
