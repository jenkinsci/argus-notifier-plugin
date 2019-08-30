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
        HOST, BUILD_NUMBER, GIT_COMMIT, BUILD_STATUS, PROJECT;

        public String lower() {
            return name().toLowerCase();
        }
    }

    private TagFactory() {
        // no instance necessary
    }

    /**
     * Create immutable map of tags for a build time metric
     * 
     * @param jenkinsRunFormatter a populated JenkinsRunFormatter
     * @return map with populated tags
     */
    static Map<String, String> buildStatusTags(JenkinsRunFormatter jenkinsRunFormatter) {

        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.<String, String>builder()
                .put(HOST.lower(), jenkinsRunFormatter.getJenkinsHostName())
                .put(PROJECT.lower(), InvalidCharSwap.swapWithDash(jenkinsRunFormatter.getProjectName()))
                .put(BUILD_NUMBER.lower(), jenkinsRunFormatter.getBuildNumberString())
                .put(BUILD_STATUS.lower(), jenkinsRunFormatter.getResult());

        String gitCommitHash = jenkinsRunFormatter.getGitCommitHash();
        if (!gitCommitHash.isEmpty()) {
            mapBuilder.put(GIT_COMMIT.lower(), gitCommitHash);
        }
            
        return mapBuilder.build();

    }

    static Map<String, String> hostTag(Jenkins jenkins) {
        return ImmutableMap.<String, String>builder().put(HOST.lower(), JenkinsFormatter.getHostName(jenkins)).build();
    }
}
