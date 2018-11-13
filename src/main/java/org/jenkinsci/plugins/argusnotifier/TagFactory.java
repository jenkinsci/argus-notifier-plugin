package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableMap;
import jenkins.model.Jenkins;

import java.util.Map;

import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.*;

/**
 * Factory class for generating tags
 *
 * @author Justin Harringa
 */
class TagFactory {
    enum Tag {
        HOST,
        PROJECT;

        public String lower() {
            return name().toLowerCase();
        }
    }

    private TagFactory() {
        // no instance necessary
    }

    /**
     * Create immutable map of tags for a build_status metric
     *
     * @param jenkins
     * @param projectName
     * @return
     */
    static Map<String, String> buildStatusTags(Jenkins jenkins, String projectName) {
        return ImmutableMap.<String, String>builder()
                .putAll(hostTag(jenkins))
                .put(PROJECT.lower(), InvalidCharSwap.swapWithDash(projectName))
                .build();
    }

    static Map<String, String> hostTag(Jenkins jenkins) {
        return ImmutableMap.<String, String>builder()
                .put(HOST.lower(), JenkinsFormatter.getHostName(jenkins))
                .build();
    }
}
