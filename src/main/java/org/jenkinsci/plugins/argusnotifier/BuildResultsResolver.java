package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableMap;
import hudson.model.AbstractBuild;
import hudson.model.Result;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Justin Harringa
 */
public class BuildResultsResolver {

    private BuildResultsResolver() {
        // No need for instantiation
    }

    public static final String FIXED = "FIXED";
    public static final String STILL_FAILING = "STILL FAILING";
    public static final String UNKNOWN = "UNKNOWN";
    private static final Map<String, Double> BUILD_STATUS_MAPPING =
            ImmutableMap.<String, Double>builder()
                    .put(Result.ABORTED.toString(), -1.0)
                    .put(Result.NOT_BUILT.toString(), -0.5)
                    .put(Result.SUCCESS.toString(), 0.0)
                    .put(UNKNOWN, 0.50)
                    .put(Result.UNSTABLE.toString(), 1.0)
                    .put(Result.FAILURE.toString(), 2.0)
                    .build();

    public static String getContextualResult(@Nonnull AbstractBuild<?,?> build) {

        AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
        Result previousBuildResult = null;
        if (previousBuild != null) {
            previousBuildResult = previousBuild.getResult();
        }

        Result buildResult = build.getResult();
        if (previousBuildResult != null && previousBuildResult == Result.FAILURE) {
            if (buildResult == Result.SUCCESS) {
                return FIXED;
            } else if (buildResult == Result.FAILURE) {
                return STILL_FAILING;
            }
        }
        return getResultString(buildResult);
    }

    public static String getResultString(Result buildResult) {
        if (buildResult == null) {
            return UNKNOWN;
        } else {
            return buildResult.toString();
        }
    }

    public static Double translateResultToNumber(Result result) {
        return BUILD_STATUS_MAPPING.get(getResultString(result));
    }
}
