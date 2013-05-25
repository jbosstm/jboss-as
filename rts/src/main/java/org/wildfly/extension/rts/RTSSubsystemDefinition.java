package org.wildfly.extension.rts;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class RTSSubsystemDefinition extends SimpleResourceDefinition {
    public static final RTSSubsystemDefinition INSTANCE = new RTSSubsystemDefinition();

    private RTSSubsystemDefinition() {
        super(RTSSubsystemExtension.SUBSYSTEM_PATH,
                RTSSubsystemExtension.getResourceDescriptionResolver(null),
                RTSSubsystemAdd.INSTANCE,
                RTSSubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
    }
}
