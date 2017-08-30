package org.jenkinsci.plugins.argusnotifier;

class HostNameFormatter {

    private HostNameFormatter() {
        // static helper
    }

    /**
     * Return only the hostname portion of a URL with no port, protocol, or URI
     *
     * @param url URL with hostname
     * @return only the hostname from url
     */
    static String getHostNameFromUrl(String url) {
        String outputUrl = url;
        if (outputUrl != null) {
            outputUrl = outputUrl.replaceAll("https?://", "");
            outputUrl = getUpToFirst(outputUrl, ':');
            outputUrl = getUpToFirst(outputUrl, '/');
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
    private static String getUpToFirst(String string, char character) {
        int firstIndex = string.indexOf(character);
        if (firstIndex != -1) {
            return string.substring(0, firstIndex);
        }
        return string;
    }
}
