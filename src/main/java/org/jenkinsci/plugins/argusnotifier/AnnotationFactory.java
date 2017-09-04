package org.jenkinsci.plugins.argusnotifier;

import com.google.common.collect.ImmutableMap;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

import java.util.Map;
import java.util.UUID;

import static org.jenkinsci.plugins.argusnotifier.MetricFactory.BUILD_STATUS_LABEL;

class AnnotationFactory {
    static final String BUILD_ANNOTATION_TYPE = "BUILD";
    static final String BUILD_NUMBER_LABEL = "Build Number";
    static final String URL_LABEL = "URL";

    private final long metricTimestamp;
    private final String scope;
    private final JenkinsBuildFormatter jenkinsBuildFormatter;
    private final String source;

    AnnotationFactory(Jenkins jenkins, AbstractBuild build, long metricTimestamp, String scope, String source) {
        this.jenkinsBuildFormatter = new JenkinsBuildFormatter(jenkins, build);
        this.metricTimestamp = metricTimestamp;
        this.scope = scope;
        if (source == null || source.trim().equals("")) {
            this.source = jenkins.getRootUrl();
        } else {
            this.source = source;
        }
    }

    Annotation getBuildStatusAnnotation() {
        Annotation annotation = new Annotation();
        annotation.setScope(scope);
        annotation.setTimestamp(metricTimestamp);
        annotation.setId(UUID.randomUUID().toString());
        annotation.setSource(source);
        annotation.setType(BUILD_ANNOTATION_TYPE);
        annotation.setMetric(MetricFactory.BUILD_STATUS);
        annotation.setTags(TagFactory.buildStatusTags(jenkinsBuildFormatter.getHostName(),
                jenkinsBuildFormatter.getProjectName()));
        Map<String, String> fields =
                ImmutableMap.<String, String>builder()
                        .put(BUILD_STATUS_LABEL, jenkinsBuildFormatter.getContextualResult())
                        .put(BUILD_NUMBER_LABEL, jenkinsBuildFormatter.getBuildNumberString())
                        .put(URL_LABEL, jenkinsBuildFormatter.getBuildUrl())
                        .build();
        annotation.setFields(fields);
        return annotation;
    }
}
