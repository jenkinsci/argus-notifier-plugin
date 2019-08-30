package org.jenkinsci.plugins.argusnotifier

import hudson.model.AbstractBuild
import hudson.model.Result
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class BuildResultsResolverTest extends Specification {
    private AbstractBuild build

    void setup() {
        build = Mock(AbstractBuild)
    }

    def 'null BuildResult Is Unknown'() {

        when:
        String result = BuildResultsResolver.getContextualResult(build)

        then:
        result == BuildResultsResolver.UNKNOWN
    }

    def 'build=#buildStatus (its own status) w/ no previous build'() {
        given:
        build.getResult() >> expectedResult

        when:
        String result = BuildResultsResolver.getContextualResult(build)

        then:
        result == expectedResult.toString()

        where:
        buildStatus | expectedResult
        "success"   | Result.SUCCESS
        "unstable"  | Result.UNSTABLE
        "failure"   | Result.FAILURE
        "not built" | Result.NOT_BUILT
        "aborted"   | Result.ABORTED
    }

    def '#expectedResult when build=#buildStatus & previousBuild=FAILURE'() {
        given:
        build.getResult() >> buildStatus
        AbstractBuild previousBuild = Mock(AbstractBuild)
        previousBuild.getResult() >> Result.FAILURE
        build.getPreviousBuild() >> previousBuild

        when:
        String result = BuildResultsResolver.getContextualResult(build)

        then:
        result == expectedResult

        where:
        buildStatus    | expectedResult
        Result.SUCCESS | BuildResultsResolver.FIXED
        Result.FAILURE | BuildResultsResolver.STILL_FAILING
    }

    def '#input status translates into #expectedNumber'() {
        when:
        Double result = BuildResultsResolver.translateResultToNumber(input)

        then:
        result == expectedNumber

        where:
        input            | expectedNumber
        Result.FAILURE   | 2.0
        Result.UNSTABLE  | 1.0
        null             | 0.5
        Result.SUCCESS   | 0.0
        Result.NOT_BUILT | -0.5
        Result.ABORTED   | -1.0
    }

    def 'getResultString: #input generates #expected resultsString'() {
        when:
        String resultsString = BuildResultsResolver.getBuildResult(input)

        then:
        resultsString == expected

        where:
        input            | expected
        Result.FAILURE   | input.toString()
        Result.UNSTABLE  | input.toString()
        null             | BuildResultsResolver.UNKNOWN
        Result.SUCCESS   | input.toString()
        Result.NOT_BUILT | input.toString()
        Result.ABORTED   | input.toString()
    }

    def 'getMetricName: #input generates #expected metricName'() {
        when:
        String resultsString = BuildResultsResolver.getMetricName(input)

        then:
        resultsString == expected

        where:
        input << [Result.FAILURE, Result.UNSTABLE, Result.SUCCESS, Result.NOT_BUILT, Result.ABORTED]
        expected = "build." + input.toString().toLowerCase()
    }

    def 'getMetricName: null generates build.unknown metricName'() {
        when:
        String resultsString = BuildResultsResolver.getMetricName(null)

        then:
        resultsString ==  "build." + BuildResultsResolver.UNKNOWN.toLowerCase()
    }
}
