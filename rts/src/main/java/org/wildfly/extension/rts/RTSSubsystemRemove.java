package org.wildfly.extension.rts;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.rts.logging.RTSLogger;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
final class RTSSubsystemRemove extends AbstractRemoveStepHandler {

    static final RTSSubsystemRemove INSTANCE = new RTSSubsystemRemove();

    private RTSSubsystemRemove() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        if (RTSLogger.ROOT_LOGGER.isTraceEnabled()) {
            RTSLogger.ROOT_LOGGER.trace("RTSSubsystemRemove.performRuntime");
        }

        context.removeService(RTSSubsystemExtension.COORDINATOR);
        context.removeService(RTSSubsystemExtension.PARTICIPANT);
    }


}
