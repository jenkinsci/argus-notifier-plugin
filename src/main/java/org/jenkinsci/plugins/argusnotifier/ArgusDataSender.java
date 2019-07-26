package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.exceptions.TokenExpiredException;
import hudson.util.Secret;

import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for sending Argus data and handling the connection each time.
 */
class ArgusDataSender {
    private static final Logger logger = Logger.getLogger(ArgusDataSender.class.getName());
    private static final String UNKNOWN_HOST_MESSAGE = "Host not found! Check your Argus server configuration in Manage Jenkins -> Configure " +
            "System or your network configuration";
    private static final String SEND_SUCCESS_MESSAGE = "Argus Notifier: Sent metrics/annotations to Argus successfully!";

    /**
     * Marked private since there is no need for an instance
     */
    private ArgusDataSender() {
        // no instance
    }

    /**
     * Send metrics and annotations to Argus via the provided argusUrl and credentials.
     *
     * @param argusConnectionInfo ArgusConnectionInfo to use to authenticate with the Argus web service
     * @param metrics Metrics to publish to Argus
     * @param annotations Annotations to publish to Argus (associated with the metrics)
     */
    static void sendArgusData(ArgusConnectionInfo argusConnectionInfo,
                              List<Metric> metrics,
                              List<Annotation> annotations) {
        try (
                ArgusService service = ArgusService.getInstance(argusConnectionInfo.argusUrl, 1)
        ) {
            UsernamePasswordCredentials credentials = argusConnectionInfo.credentials;
            service.getAuthService().login(credentials.getUsername(),
                    Secret.toString(credentials.getPassword()));

            service.getMetricService().putMetrics(metrics);

            if (annotations != null && !annotations.isEmpty()) {
                service.getAnnotationService().putAnnotations(annotations);
            }

            if (logger.isLoggable(Level.INFO)) {
                logger.info(SEND_SUCCESS_MESSAGE);
            }
            service.getAuthService().logout();
        } catch (TokenExpiredException tokenExpired) {
            //TODO: do something?
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Token expired.");
            }
        } catch (UnknownHostException unknownHostException) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, UNKNOWN_HOST_MESSAGE, unknownHostException);
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Argus Notifier: Error", e);
            }
        }
    }

    /**
     * Used to test the connection to the Argus server in the Jenkins config screen to validate configuration
     *
     * @param argusUrl - URL of the Argus web service
     * @param credentials - credentials to access the web service
     * @return successfully logged into Argus web service
     */
    static boolean testConnection(String argusUrl, UsernamePasswordCredentials credentials) {
        try (ArgusService service = ArgusService.getInstance(argusUrl, 1)) {
            service.getAuthService().login(credentials.getUsername(), credentials.getPassword().getPlainText());
            return true;
        } catch (UnknownHostException unknownHostException) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, UNKNOWN_HOST_MESSAGE, unknownHostException);
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, String.format("Argus Notifier: Error when testing connection '%s' p='%s'",credentials.getUsername(),credentials.getPassword().getPlainText()), e);
            }
        }
        return false;
    }
}
