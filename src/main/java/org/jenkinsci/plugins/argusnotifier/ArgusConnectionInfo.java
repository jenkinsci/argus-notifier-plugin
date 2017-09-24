package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;

/**
 * Simple value holder class
 */
class ArgusConnectionInfo {

    final String argusUrl;
    final UsernamePasswordCredentials credentials;

    ArgusConnectionInfo(String argusUrl, UsernamePasswordCredentials credentials) {
        this.argusUrl = argusUrl;
        this.credentials = credentials;
    }
}
