package org.jenkinsci.plugins.argusnotifier;

import jenkins.model.Jenkins;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience class to provide optional objects and log a warning message if null
 */
class JenkinsOptionalConverter {

    /**
     * Logs if Jenkins.instance is null and returns an optional of Jenkins.instance
     *
     * @param logger logger to log to id the Jenkins instance is null
     * @return optional of Jenkins.instance
     */
    static Optional<Jenkins> getOptionalInstance(Logger logger) {
        Optional<Jenkins> jenkins = Optional.ofNullable(Jenkins.getInstance());
        if (!jenkins.isPresent()) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Jenkins.instance was null. Skipping...");
            }
        }
        return jenkins;
    }
}
