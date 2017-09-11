package org.jenkinsci.plugins.argusnotifier;

import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

import javax.annotation.Nonnull;

/**
 * Formatter to consistently format Jenkins and Jenkins build info
 */
class JenkinsBuildFormatter {
    
    private final String jenkinsUrl;
    private final AbstractBuild build;

    public JenkinsBuildFormatter(@Nonnull Jenkins jenkins, @Nonnull AbstractBuild build) {
        this.jenkinsUrl = jenkins.getRootUrl();
        this.build = build;
    }

    /**
     * Return the project name associated with this build.
     *
     * @return project name associated with build
     */
    String getProjectName() {
        if (build.getParent().getFullName() == null) {
            return "null";
        }
        return build.getParent().getFullName().replaceAll("/", ".");
    }

    /**
     * Will return either just the build URL "job/test/42/" if the Jenkins URL is null or
     * a full build URL.
     *
     * @return full build URL if possible
     */
     String getBuildUrl() {
        if (jenkinsUrl == null) {
            return build.getUrl();
        }
        if (jenkinsUrl.substring(jenkinsUrl.length() - 1, jenkinsUrl.length()).equals("/")) {
            return jenkinsUrl + build.getUrl();
        }
        return jenkinsUrl + '/' + build.getUrl();
    }

    /**
     * Convenience method to format build.number as String
     *
     * @return build.number as String
     */
    public String getBuildNumberString() {
        return String.valueOf(build.getNumber());
    }

    /**
     * Convenience method to get contextual build result
     *
     * @return contextual build result (e.g. FIXED, STILL FAILING)
     */
    public String getContextualResult() {
        return BuildResultsResolver.getContextualResult(build);
    }
}
