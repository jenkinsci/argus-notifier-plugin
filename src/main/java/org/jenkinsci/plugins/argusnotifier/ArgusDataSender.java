package org.jenkinsci.plugins.argusnotifier;

import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.salesforce.dva.argus.sdk.ArgusService;
import com.salesforce.dva.argus.sdk.entity.Annotation;
import com.salesforce.dva.argus.sdk.entity.Metric;
import com.salesforce.dva.argus.sdk.excpetions.TokenExpiredException;
import hudson.util.Secret;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Responsible for sending Argus data and handling the connection each time.
 */
class ArgusDataSender {
    private static final Logger logger = Logger.getLogger(ArgusDataSender.class.getName());

    /**
     * Marked private since there is no need for an instance
     */
    private ArgusDataSender() {
        // no instance
    }

    /**
     * Send metrics and annotations to Argus via the provided argusUrl and credentials.
     *
     * @param argusUrl URL to use to connect to the Argus Web Service
     * @param credentials UsernamePasswordCredentials to use to authenticate with the Argus web service
     * @param metrics Metrics to publish to Argus
     * @param annotations Annotations to publish to Argus (associated with the metrics)
     */
    static void sendArgusData(String argusUrl,
                              UsernamePasswordCredentials credentials,
                              List<Metric> metrics,
                              List<Annotation> annotations) {
        try (
                ArgusService service = ArgusService.getInstance(argusUrl, 1)
        ) {
            service.getAuthService().login(credentials.getUsername(), Secret.toString(credentials.getPassword()));

            service.getMetricService().putMetrics(metrics);

            service.getAnnotationService().putAnnotations(annotations);

            if (logger.isLoggable(Level.INFO)) {
                logger.info("Argus Notifier: Sent message to Argus successfully!");
            }
            service.getAuthService().logout();
        } catch (TokenExpiredException tokenExpired) {
            //TODO: do something?
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Token expired.");
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Argus Notifier: Error", e);
            }
        }
    }

    static boolean testConnection(String argusUrl, UsernamePasswordCredentials credentials) {
        try (ArgusService service = ArgusService.getInstance(argusUrl, 1)) {
            service.getAuthService().login(credentials.getUsername(), credentials.getPassword().getPlainText());
            return true;
        } catch (TokenExpiredException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Token expired when testing connection.");
            }
        } catch (Exception e) {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Argus Notifier: Error when testing connection", e);
            }
        }
        return false;
    }
}
