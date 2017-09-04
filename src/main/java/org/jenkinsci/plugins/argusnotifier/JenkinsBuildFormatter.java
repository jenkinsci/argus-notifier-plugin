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
     * Return only the hostname portion of a URL with no port, protocol, or URI
     *
     * @return only the hostname from url
     */
    String getHostName() {
        String outputUrl = jenkinsUrl;
        if (outputUrl != null) {
            outputUrl = outputUrl.replaceAll("https?://", "");
            outputUrl = substringUpToFirst(outputUrl, ':');
            outputUrl = substringUpToFirst(outputUrl, '/');
        }
        return outputUrl;
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

    /**
     * Returns the string up to the first instance of character in the string or the string
     * if the character doesn't exist in the string.
     *
     * @param string string to analyze
     * @param character character to find in string
     * @return string up to the first instance of character in the string or the string if the character doesn't exist in the string
     */
    private String substringUpToFirst(String string, char character) {
        int firstIndex = string.indexOf(character);
        if (firstIndex != -1) {
            return string.substring(0, firstIndex);
        }
        return string;
    }
}
