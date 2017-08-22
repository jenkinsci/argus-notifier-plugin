package org.jenkinsci.plugins.argusnotifier

import com.google.common.collect.ImmutableMap

import static org.jenkinsci.plugins.argusnotifier.TagFactory.Tag.*

/**
 * Factory class for generating tags
 *
 * @author Justin Harringa
 */
class TagFactory {
    enum Tag {
        TYPE,
        HOST,
        PROJECT,
        BUILD_STATUS

        String lower() {
            name().toLowerCase()
        }
    }

    /**
     * Create immutable map of tags for a build_status metric
     *
     * @param rootUrl
     * @param projectName
     * @param buildStatus
     * @return
     */
    static Map<String, String> buildStatusTags(String rootUrl, String projectName) {
        return ImmutableMap.<String, String>builder()
                .put(HOST.lower(), rootUrl)
                .put(PROJECT.lower(), projectName)
                .build()
    }
}
