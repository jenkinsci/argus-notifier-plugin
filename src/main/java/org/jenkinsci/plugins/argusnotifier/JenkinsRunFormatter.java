package org.jenkinsci.plugins.argusnotifier;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import hudson.EnvVars;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Formatter to consistently format Jenkins and Jenkins build info
 */
class JenkinsRunFormatter {

    private final String jenkinsUrl;
    private final Run run;
    private static final Logger logger = Logger.getLogger(JenkinsRunFormatter.class.getName());

    public JenkinsRunFormatter(@Nonnull Jenkins jenkins, @Nonnull Run run) {
        this.jenkinsUrl = jenkins.getRootUrl();
        this.run = run;
    }

    /**
     * Return the project name associated with this build.
     *
     * @return project name associated with build
     */
    String getProjectName() {
        if (run.getParent().getFullName() == null) {
            return "null";
        }
        return run.getParent().getFullName().replaceAll("/", ".");
    }

    /**
     * Will return either just the build URL "job/test/42/" if the Jenkins URL is
     * null or a full build URL.
     *
     * @return full run URL if possible
     */
    String getRunUrl() {
        if (jenkinsUrl == null) {
            return run.getUrl();
        }
        if (jenkinsUrl.substring(jenkinsUrl.length() - 1, jenkinsUrl.length()).equals("/")) {
            return jenkinsUrl + run.getUrl();
        }
        return jenkinsUrl + '/' + run.getUrl();
    }

    /**
     * Convenience method to format build.number as String
     *
     * @return build.number as String
     */
    public String getBuildNumberString() {
        return String.valueOf(run.getNumber());
    }
    
    
    /**
     * Get GIT_COMMIT environment variable if available.
     * 
     * 
     * @return commit sha as String, return empty String if not available.
     */
    public String getGitCommit() {
        
        LogTaskListener listener = new LogTaskListener(logger, Level.INFO);
        
        String commitVar = TagFactory.Tag.GIT_COMMIT.toString();
        try {
            EnvVars envVars = run.getEnvironment(listener);
            Object commitId = envVars.get(commitVar);
            if (commitId == null) {
                return "";
            } else {
                return (String) commitId;
            }
        }
        catch (IOException | InterruptedException e) {
            return "";
        }
        

    }

    /**
     * Convenience method to get contextual build result
     *
     * @return contextual build result (e.g. FIXED, STILL FAILING)
     */
    public String getContextualResult() {
        return BuildResultsResolver.getContextualResult(run);
    }
}
