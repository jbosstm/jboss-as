/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.compensations;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.txn.service.TxnServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class CompensationsSubsystemAdd extends AbstractBoottimeAddStepHandler {

    private final String subsystemName;

    CompensationsSubsystemAdd(String subsystemName) {
        this.subsystemName = subsystemName;
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) {
        registerRecoveryService(context);
        registerDeploymentProcessor(context);
    }

    private void registerRecoveryService(OperationContext context) {
        context.getServiceTarget().addService(CompensationsExtension.RECOVERY_SERVICE_NAME, new CompensationsRecoveryService())
                .addDependency(TxnServices.JBOSS_TXN_ARJUNA_RECOVERY_MANAGER).setInitialMode(ServiceController.Mode.ACTIVE)
                .install();
    }

    private void registerDeploymentProcessor(OperationContext context) {
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget target) {
                target.addDeploymentProcessor(subsystemName, Phase.DEPENDENCIES, Phase.DEPENDENCIES_TRANSACTIONS,
                        new CompensationsDependenciesDeploymentProcessor());
            }
        }, OperationContext.Stage.RUNTIME);
    }

}
