package org.wildfly.extension.rts.service;

import io.undertow.servlet.api.DeploymentInfo;

import java.util.HashMap;
import java.util.Map;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.narayana.rest.integration.ParticipantResource;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.wildfly.extension.rts.logging.RTSLogger;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantService extends AbstractRTSService implements Service<ParticipantService> {

    public static final String CONTEXT_PATH = ParticipantResource.BASE_PATH_SEGMENT;

    private static final String DEPLOYMENT_NAME = "REST-AT Participant";

    private DeploymentInfo participantDeploymentInfo = null;

    @Override
    public ParticipantService getValue() throws IllegalStateException, IllegalArgumentException {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("ParticipantService.getValue");
        }

        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("ParticipantService.start");
        }

        deployParticipant();
        ParticipantsManagerFactory.getInstance().setBaseUrl(getBaseUrl());
    }

    @Override
    public void stop(StopContext context) {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("ParticipantService.stop");
        }

        undeployServlet(participantDeploymentInfo);
    }

    private void deployParticipant() {
        undeployServlet(participantDeploymentInfo);

        final Map<String, String> initialParameters = new HashMap<String, String>();
        initialParameters.put("javax.ws.rs.Application", "org.wildfly.extension.rts.jaxrs.ParticipantApplication");

        participantDeploymentInfo = getDeploymentInfo(DEPLOYMENT_NAME, CONTEXT_PATH, initialParameters);

        deployServlet(participantDeploymentInfo);
    }

    private String getBaseUrl() {
        String baseAddress = System.getProperty("jboss.bind.address");
        String basePort = System.getProperty("jboss.bind.port");

        if (baseAddress == null) {
            baseAddress = "http://localhost";
        }

        if (basePort == null) {
            basePort = "8080";
        }

        return baseAddress + ":" + basePort;
    }

}
