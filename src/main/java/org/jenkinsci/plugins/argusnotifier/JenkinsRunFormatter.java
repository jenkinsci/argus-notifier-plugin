package org.jenkinsci.plugins.argusnotifier;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;


/**
 * Formatter to consistently format Jenkins and Jenkins build info
 */
class JenkinsRunFormatter {
    
	public static final String GIT_COMMIT = "BUILD_URL";
    private final String jenkinsUrl;
    private final Run run;

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
     * Will return either just the build URL "job/test/42/" if the Jenkins URL is null or
     * a full build URL.
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

    public String getGitCommit() {
    	Object commitId = run.getEnvVars().get(GIT_COMMIT);
    	if (commitId == null) {
    		return "";
    	}
    	else {
    		return (String)commitId;
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
