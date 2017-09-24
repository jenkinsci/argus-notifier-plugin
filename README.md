# Argus Notifier
[![License](https://img.shields.io/github/license/jenkinsci/argus-notifier.svg)](LICENSE)
[![wiki](https://img.shields.io/badge/Argus%20Notifier%20Plugin-WIKI-blue.svg?style=flat)](http://wiki.jenkins-ci.org/display/JENKINS/Argus+Notifier+Plugin)

This Jenkins plugin sends build status and build time (including queue time, build time, and total time) to 
an [Argus](https://github.com/salesforce/Argus) endpoint. It also sends system metrics from the 
[Metrics](https://plugins.jenkins.io/metrics) plugin. Administrators can configure whether system and build metrics
are automatically sent or not.

## Metric implementation details 
* Timings are currently sent in units of seconds for consistency
* `Result` statuses are mapped to numbers in 
[BuildResultsResolver](https://github.com/justinharringa/argus-notifier/blob/master/src/main/java/org/jenkinsci/plugins/argusnotifier/BuildResultsResolver.java#L22)
* Numeric gauge metrics from the [Metrics](https://plugins.jenkins.io/metrics) plugin are sent if configured.
 

## Plugin Dependencies
* [Metrics](https://plugins.jenkins.io/metrics) - used to get the queue time
* [Credentials](https://plugins.jenkins.io/credentials) - securely store your [Argus](https://github.com/salesforce/Argus)
credentials 

## Build
To build the project with [Maven](https://maven.apache.org/), simply run `mvn clean package`

## Test
Run `mvn clean hpi:run` to start up a test version of Jenkins with the requisite plugins installed.

## Releasing
Run `mvn release:prepare release:perform` but ensure that your Maven `settings.xml` has been 
[set up with your Artifactory password](https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins#HostingPlugins-Releasingtojenkins-ci.org)
