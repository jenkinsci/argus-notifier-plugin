# Argus Notifier
[![License](https://img.shields.io/github/license/jenkinsci/argus-notifier.svg)](LICENSE)
[![wiki](https://img.shields.io/badge/Argus%20Notifier%20Plugin-WIKI-blue.svg?style=flat)](http://wiki.jenkins-ci.org/display/JENKINS/Argus+Notifier+Plugin)

This Jenkins plugin sends build status and build time (including queue time, build time, and total time) to 
an [Argus](https://github.com/salesforce/Argus) endpoint. It also sends system metrics from the 
[Metrics](https://plugins.jenkins.io/metrics) plugin. Administrators can configure whether system and build metrics
are automatically sent or not.

### Table of Contents
* [Demo](#demo)
  * [How do I get started?](#how-do-i-get-started)
* [Metric implementation details](#metric-implementation-details)
* [Plugin Dependencies](#plugin-dependencies)
* [Developer Stuff](#developer-stuff)
  * [Build](#build)
  * [Test](#test)
  * [Releasing](#releasing)
    
## Demo
So, you're using, or considering, the [Argus](https://github.com/salesforce/Argus) 
time-series monitoring and alerting platform and you also use Jenkins. Well, hey, 
you should check this plugin out to easily send Jenkins metrics to [Argus](https://github.com/salesforce/Argus).

### How do I get started?
First, you'll need to have a valid `Username with Password` credential set up in your Jenkins 
credentials. Next, you simply go to `Jenkins -> Manage Jenkins -> Configure System` and find the 
configuration for `Argus Notifier`. Then you'll configure the following values:

* `Credentials Id` - A `Username with Password` credential that has access to your 
[Argus](https://github.com/salesforce/Argus) instance.
* `Argus URL` - The URL to your [Argus](https://github.com/salesforce/Argus) web service endpoint. 
* `Scope` - The [Argus](https://github.com/salesforce/Argus) scope you'd like to use (typically we use a URL or 
conceptual name)
* `Source` - The [Argus](https://github.com/salesforce/Argus) source you'd like to use 
(the plugin will set this to `Scope` if you don't fill this in)
* `Send for all builds?` - Whether you'd like all builds to send build metrics (timings and status) upon build 
completion 
* `Send system metrics?` - Whether you'd like the plugin to send system metrics every minute

You can test that your connection works by hitting the `Test Connection` button as long as
you've selected a valid `Credentials Id` and filled in your `Argus URL`. See below:

![Argus Notifier configuration - Test Connection animation](https://s3-us-west-1.amazonaws.com/static.harringa.com/argus-notifier-plugin/connection-validation.gif)

Once you've saved or applied your Jenkins configuration, the plugin will go to work.

## Metric implementation details 
* Timings are currently sent in units of seconds for consistency
* `Result` statuses are mapped to numbers in 
[BuildResultsResolver](https://github.com/justinharringa/argus-notifier/blob/master/src/main/java/org/jenkinsci/plugins/argusnotifier/BuildResultsResolver.java#L22)
* Numeric gauge metrics from the [Metrics](https://plugins.jenkins.io/metrics) plugin are sent if configured.
 

## Plugin Dependencies
* [Metrics](https://plugins.jenkins.io/metrics) - used to get the queue time
* [Credentials](https://plugins.jenkins.io/credentials) - securely store your [Argus](https://github.com/salesforce/Argus)
credentials 

## Developer Stuff

### Build
To build the project with [Maven](https://maven.apache.org/), simply run `mvn clean package`

### Test
Run `mvn clean hpi:run` to start up a test version of Jenkins with the requisite plugins installed.

### Releasing
Run `mvn release:prepare release:perform` but ensure that your Maven `settings.xml` has been 
[set up with your Artifactory password](https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins#HostingPlugins-Releasingtojenkins-ci.org)
