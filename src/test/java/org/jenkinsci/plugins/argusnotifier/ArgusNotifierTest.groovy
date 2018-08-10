package org.jenkinsci.plugins.argusnotifier

import hudson.model.Item
import hudson.model.User
import hudson.security.ACL
import jenkins.model.Jenkins
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.MockAuthorizationStrategy
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class ArgusNotifierTest extends Specification {
    public static final ADMIN_USER = "admin"
    public static final DEV_USER = "dev"
    @Rule public JenkinsRule jenkinsRule = new JenkinsRule()

    def "check that #user isUserAdmin() resolves to #expectedResult"() {
        given:
        jenkinsRule.jenkins.setSecurityRealm(jenkinsRule.createDummySecurityRealm())
        jenkinsRule.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).everywhere().to(ADMIN_USER).
                grant(Jenkins.READ, Item.READ).everywhere().to(DEV_USER))
        ACL.as(User.get(user).impersonate())

        when:
        boolean result = jenkinsRule.jenkins.getDescriptorByType(ArgusNotifier.DescriptorImpl).isUserAdmin()

        then:
        result == expectedResult

        where:
        user       | expectedResult
        ADMIN_USER | true
        DEV_USER   | false
    }
}
