package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SendArgusMetricStep extends Builder implements SimpleBuildStep {
    // Optional
    public String scope;
    public String displayName;

    // Required
    // TODO: Field validation?
    public final String metricName;
    public final Double value;
    public final Map<String, String> tags;

    @DataBoundConstructor
    public SendArgusMetricStep(@Nonnull String metricName, @Nonnull Double value, @Nonnull Map<String, String> tags) {
        this.metricName = metricName;
        this.value = value;
        this.tags = tags;
    }

    @Nonnull
    public String getMetricName() {
        return metricName;
    }

    @Nonnull
    public Double getValue() {
        return value;
    }

    @Nonnull
    public Map<String, String> getTags() {
        return tags;
    }

    public String getScope() {
        return scope;
    }

    @DataBoundSetter
    public void setScope(@Nonnull String scope) {
        this.scope = scope;
    }

    public String getDisplayName() {
        return displayName;
    }

    @DataBoundSetter
    public void setDisplayName(@Nonnull String displayName) {
        this.displayName = displayName;
    }

    /**
     * Run this step.
     *
     * @param run       a build this is running as a part of
     * @param workspace a workspace to use for any file operations
     * @param launcher  a way to start processes
     * @param listener  a place to send output
     * @throws InterruptedException if the step is interrupted
     * @throws IOException          if something goes wrong; use {@link AbortException} for a polite error
     */
    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        Jenkins jenkins = Jenkins.getInstance();
        ArgusNotifier.DescriptorImpl descriptor = jenkins.getDescriptorByType(ArgusNotifier.DescriptorImpl.class);

        if (descriptor != null && descriptor.isNotifierConfigured()) {
            ArgusConnectionInfo argusConnectionInfo = descriptor.getArgusConnectionInfo();

            OffsetDateTime now = OffsetDateTime.now();
            long metricTimestamp = now.toEpochSecond();
            String descriptorScope = descriptor.getScope();
            List<Metric> metrics = new ArrayList<>();
            Metric metric = new Metric();
            if (this.scope != null) {
                metric.setScope(this.scope);
            } else {
                metric.setScope(descriptorScope);
            }
            if (displayName != null) {
                metric.setDisplayName(displayName);
            }
            metric.setMetric(metricName);
            metric.setDatapoints(ImmutableMap.<Long, Double>builder().put(metricTimestamp, value).build());

            JenkinsRunFormatter jenkinsRunFormatter = new JenkinsRunFormatter(jenkins, run);
            metric.setTags(ImmutableMap.<String, String>builder()
                    .putAll(tags)
                    .putAll(TagFactory.buildStatusTags(jenkins,
                            jenkinsRunFormatter.getProjectName())).build());

            listener.getLogger().println(
                    String.format("Sending metric '%s' to Argus at %d with value %.2f",
                            metricName,
                            metricTimestamp,
                            value));
            ArgusDataSender.sendArgusData(argusConnectionInfo, metrics, null);
        }
    }

    @Symbol("sendArgusMetric")
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * Returns true if this task is applicable to the given project.
         *
         * @param jobType
         * @return true to allow user to configure this post-promotion task for the given project.
         * @see AbstractProject.AbstractProjectDescriptor#isApplicable(Descriptor)
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Human readable name of this kind of configurable object.
         * Should be overridden for most descriptors, if the display name is visible somehow.
         * As a fallback it uses {@link Class#getSimpleName} on {@link #clazz}, so for example {@code MyThing} from {@code some.pkg.MyThing.DescriptorImpl}.
         * Historically some implementations returned null as a way of hiding the descriptor from the UI,
         * but this is generally managed by an explicit method such as {@code isEnabled} or {@code isApplicable}.
         */
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Send Argus Metric";
        }
    }
}