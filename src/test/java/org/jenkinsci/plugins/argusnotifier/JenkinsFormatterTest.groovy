package org.jenkinsci.plugins.argusnotifier

import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class JenkinsFormatterTest extends Specification {

    private static final String SOMEHOST = "somehost"
    private Jenkins jenkins = Mock(Jenkins)

    def 'hostNameFromUrl: #inputUrl should be #hostName'() {
        given:
        jenkins.getRootUrl() >> inputUrl

        when:
        String result = JenkinsFormatter.getHostName(jenkins)

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
