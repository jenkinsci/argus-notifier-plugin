package org.jenkinsci.plugins.argusnotifier

import com.salesforce.dva.argus.sdk.entity.Annotation
import com.salesforce.dva.argus.sdk.entity.Metric
import hudson.model.AbstractBuild
import hudson.model.Job
import hudson.model.Result
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class AnnotationFactoryTest extends Specification {

    public static final String JENKINS_ROOT_URL = "testurl"
    private Jenkins jenkins = Mock(Jenkins)
    private AbstractBuild build = Mock(AbstractBuild)
    private JenkinsBuildFormatter jenkinsBuildFormatter

    def setup() {
        build.getResult() >> Result.SUCCESS
        Job parentProject = Mock(Job)
        build.getParent() >> parentProject
        parentProject.getName() >> "projectName"
        jenkins.getRootUrl() >> JENKINS_ROOT_URL
        jenkinsBuildFormatter = new JenkinsBuildFormatter(jenkins, build)
    }

    def "source #actualSource = #expectedSource"() {
        given:
        AnnotationFactory annotationFactory =
                new AnnotationFactory(jenkins, build, 1L, "scope", actualSource)

        when:
        Annotation buildStatusAnnotation = annotationFactory.getAnnotationFor(new Metric())

        then:
        buildStatusAnnotation.getSource() == expectedSource

        where:
        actualSource | expectedSource
        "test"       | "test"
        null         | JENKINS_ROOT_URL
    }

    def "annotation has #field field"() {
        given:
        AnnotationFactory annotationFactory =
                new AnnotationFactory(jenkins, build, 1L, "scope", "source")

        when:
        Annotation buildStatusAnnotation = annotationFactory.getAnnotationFor(new Metric())

        then:
        buildStatusAnnotation.fields[field] == jenkinsBuildFormatter[formatterMethod]

        where:
        field                                 | formatterMethod
        BuildMetricFactory.BUILD_STATUS_LABEL | "contextualResult"
        AnnotationFactory.BUILD_NUMBER_LABEL  | "buildNumberString"
        AnnotationFactory.URL_LABEL           | "buildUrl"
    }
}
