package org.wildfly.extension.rts.service;

import io.undertow.servlet.api.DeploymentInfo;

import java.util.HashMap;
import java.util.Map;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.extension.rts.logging.RTSLogger;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class CoordinatorService extends AbstractRTSService implements Service<CoordinatorService> {

    public static final String CONTEXT_PATH = "/rest-at-coordinator";

    private static final String DEPLOYMENT_NAME = "REST-AT Coordinator";

    private DeploymentInfo coordinatorDeploymentInfo = null;

    @Override
    public CoordinatorService getValue() throws IllegalStateException, IllegalArgumentException {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("CoordinatorService.getValue");
        }

        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("CoordinatorService.start");
        }

        deployCoordinator();
    }

    @Override
    public void stop(StopContext context) {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("CoordinatorService.stop");
        }

        undeployServlet(coordinatorDeploymentInfo);
    }

    private void deployCoordinator() {
        undeployServlet(coordinatorDeploymentInfo);

        final Map<String, String> initialParameters = new HashMap<String, String>();
        initialParameters.put("javax.ws.rs.Application", "org.wildfly.extension.rts.jaxrs.CoordinatorApplication");

        coordinatorDeploymentInfo = getDeploymentInfo(DEPLOYMENT_NAME, CONTEXT_PATH, initialParameters);

        deployServlet(coordinatorDeploymentInfo);
    }

}
