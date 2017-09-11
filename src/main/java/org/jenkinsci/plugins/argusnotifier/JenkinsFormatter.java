package org.jenkinsci.plugins.argusnotifier;

import jenkins.model.Jenkins;

/**
 * Formatter for general Jenkins objects
 */
class JenkinsFormatter {

    /**
     * Return only the hostname portion of a URL with no port, protocol, or URI
     *
     * @return only the hostname from url
     */
    static String getHostName(Jenkins jenkins) {
        String outputUrl = jenkins.getRootUrl();
        if (outputUrl != null) {
            outputUrl = outputUrl.replaceAll("https?://", "");
            outputUrl = substringUpToFirst(outputUrl, ':');
            outputUrl = substringUpToFirst(outputUrl, '/');
        }
        return outputUrl;
    }

    /**
     * Returns the string up to the first instance of character in the string or the string
     * if the character doesn't exist in the string.
     *
     * @param string string to analyze
     * @param character character to find in string
     * @return string up to the first instance of character in the string or the string if the character doesn't exist in the string
     */
    private static String substringUpToFirst(String string, char character) {
        int firstIndex = string.indexOf(character);
        if (firstIndex != -1) {
            return string.substring(0, firstIndex);
        }
        return string;
    }
}
