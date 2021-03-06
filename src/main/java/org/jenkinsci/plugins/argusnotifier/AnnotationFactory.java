package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.jenkinsci.plugins.argusnotifier.BuildMetricFactory.NUMERIC_BUILD_STATUS_LABEL;

class AnnotationFactory {
    static final String BUILD_ANNOTATION_TYPE = "BUILD";
    static final String BUILD_NUMBER_LABEL = "Build Number";
    static final String URL_LABEL = "URL";

    private final long metricTimestamp;
    private final String scope;
    private final JenkinsRunFormatter jenkinsRunFormatter;
    private final String source;

    AnnotationFactory(Jenkins jenkins, Run run, long metricTimestamp, String scope, String source) {
        this.jenkinsRunFormatter = new JenkinsRunFormatter(jenkins, run);
        this.metricTimestamp = metricTimestamp;
        this.scope = scope;
        if (source == null || source.trim().equals("")) {
            this.source = JenkinsFormatter.getHostName(jenkins);
        } else {
            this.source = source;
        }
    }

    Annotation getAnnotationFor(Metric metric) {
        Annotation annotation = new Annotation();
        annotation.setScope(scope);
        annotation.setTimestamp(metricTimestamp);
        annotation.setId(UUID.randomUUID().toString());
        annotation.setSource(source);
        annotation.setType(BUILD_ANNOTATION_TYPE);
        annotation.setMetric(metric.getMetric());
        annotation.setTags(metric.getTags());
        Map<String, String> fields =
                ImmutableMap.<String, String>builder()
                        .put(NUMERIC_BUILD_STATUS_LABEL, jenkinsRunFormatter.getContextualResult())
                        .put(BUILD_NUMBER_LABEL, jenkinsRunFormatter.getBuildNumberString())
                        .put(URL_LABEL, jenkinsRunFormatter.getRunUrl())
                        .build();
        annotation.setFields(fields);
        return annotation;
    }

    List<Annotation> getAnnotationsFor(List<Metric> metrics) {
        ImmutableList.Builder<Annotation> annotations = ImmutableList.builder();
        for (Metric metric : metrics) {
            annotations.add(getAnnotationFor(metric));
        }
        return annotations.build();

    }
}
