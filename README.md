# Argus Notifier
[![License](https://img.shields.io/github/license/justinharringa/argus-notifier.svg)](LICENSE)
[![wiki](https://img.shields.io/badge/Argus%20Notifier%20Plugin-WIKI-blue.svg?style=flat)](http://wiki.jenkins-ci.org/display/JENKINS/Argus+Notifier+Plugin)

This Jenkins plugin sends build status and build time (including queue time, build time, and total time) to 
an [Argus](https://github.com/salesforce/Argus) endpoint.

## Metric implementation details 
* Timings are currently sent in units of seconds for consistency
* `Result` statuses are mapped to numbers in 
[BuildResultsResolver](https://github.com/justinharringa/argus-notifier/blob/master/src/main/java/org/jenkinsci/plugins/argusnotifier/BuildResultsResolver.java#L22)
 

## Plugin Dependencies
* [Metrics](https://plugins.jenkins.io/metrics) - used to get the queue time
* [Credentials](https://plugins.jenkins.io/credentials) - securely store your [Argus](https://github.com/salesforce/Argus)
credentials 

## Build
To build the project with [Maven](https://maven.apache.org/), simply run `mvn clean package`

## Test
Run `mvn clean hpi:run` to start up a test version of Jenkins with the requisite plugins installed.
