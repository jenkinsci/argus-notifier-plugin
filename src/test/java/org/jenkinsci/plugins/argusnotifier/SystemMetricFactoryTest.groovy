package org.jenkinsci.plugins.argusnotifier

import com.salesforce.dva.argus.sdk.entity.Metric
import jenkins.model.Jenkins
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime

@Unroll
class SystemMetricFactoryTest extends Specification {

    public static final String ROOT_URL = "testroot"
    public static final String METRIC_NAME = "mymetric"
    private Jenkins jenkins = Mock(Jenkins)
    private static final long time = OffsetDateTime.now().toEpochSecond()
    private static final String TEST_SCOPE = "test-scope"
    private SystemMetricFactory systemMetricFactory = new SystemMetricFactory(jenkins, time, TEST_SCOPE)

    def setup() {
        jenkins.getRootUrl() >> ROOT_URL
    }

    def "#type datapoint is set"() {
        when:
        Metric metric = systemMetricFactory.getMetric(METRIC_NAME, inputVal)

        then:
        metric.getDatapoints()[time] == 1.0

        where:
        inputVal       | type
        new Double(1)  | "Double"
        new Long(1)    | "Long"
        new Integer(1) | "Integer"
        new Float(1)   | "Float"
        1D             | "double"
        1L             | "long"
        1              | "int"
        1f             | "float"
    }

    def "host tag is set to root url"() {
        when:
        Metric metric = systemMetricFactory.getMetric(METRIC_NAME, 1L)

        then:
        metric.getTags()[TagFactory.Tag.HOST.lower()] == ROOT_URL
    }

    def "#field set in metric"() {
        when:
        Metric metric = systemMetricFactory.getMetric(METRIC_NAME, 1L)

        then:
        metric[field] == value

        where:
        field    | value
        "scope"  | TEST_SCOPE
        "metric" | METRIC_NAME
    }
}
