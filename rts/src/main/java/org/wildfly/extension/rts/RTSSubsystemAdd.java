package org.wildfly.extension.rts;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.wildfly.extension.rts.logging.RTSLogger;
import org.wildfly.extension.rts.service.CoordinatorService;
import org.wildfly.extension.rts.service.ParticipantService;
import org.wildfly.extension.undertow.Host;
import org.wildfly.extension.undertow.UndertowService;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
final class RTSSubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final RTSSubsystemAdd INSTANCE = new RTSSubsystemAdd();

    private static final String DEFAULT_SERVER_NAME = "default-server";

    private static final String DEFAULT_HOST_NAME = "default-host";

    private RTSSubsystemAdd() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("RTSSubsystemAdd.populateModel");
        }

        model.setEmptyObject();
    }

    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("RTSSubsystemAdd.performBoottime");
        }

        final CoordinatorService coordinatorService = new CoordinatorService();
        final ParticipantService participantService = new ParticipantService();

        final ServiceBuilder<CoordinatorService> coordinatorServiceBuilder = context
                .getServiceTarget()
                .addService(RTSSubsystemExtension.COORDINATOR, coordinatorService)
                .addDependency(UndertowService.virtualHostName(DEFAULT_SERVER_NAME, DEFAULT_HOST_NAME), Host.class,
                        coordinatorService.getInjectedHost());

        final ServiceBuilder<ParticipantService> participantServiceBuilder = context
                .getServiceTarget()
                .addService(RTSSubsystemExtension.PARTICIPANT, participantService)
                .addDependency(UndertowService.virtualHostName(DEFAULT_SERVER_NAME, DEFAULT_HOST_NAME), Host.class,
                        participantService.getInjectedHost());

        coordinatorServiceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);
        participantServiceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);

        final ServiceController<CoordinatorService> coordinatorServiceController = coordinatorServiceBuilder.install();
        final ServiceController<ParticipantService> participantServiceController = participantServiceBuilder.install();

        if (newControllers != null) {
            newControllers.add(coordinatorServiceController);
            newControllers.add(participantServiceController);
        }
    }

}