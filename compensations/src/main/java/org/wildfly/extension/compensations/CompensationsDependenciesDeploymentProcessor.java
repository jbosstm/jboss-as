/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.jandex.DotName;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.narayana.compensations.api.CancelOnFailure;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.CompensationScoped;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;
import org.jboss.narayana.compensations.api.TxLogged;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class CompensationsDependenciesDeploymentProcessor implements DeploymentUnitProcessor {

    private static final ModuleIdentifier COMPENSATIONS_MODULE = ModuleIdentifier.create("org.jboss.narayana.compensations");

    private static final List<Class<?>> ANNOTATIONS = Arrays.asList(Compensatable.class, CancelOnFailure.class,
            CompensationScoped.class, TxCompensate.class, TxConfirm.class, TxLogged.class);

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        DeploymentUnit unit = phaseContext.getDeploymentUnit();
        CompositeIndex compositeIndex = unit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (compositeIndex != null && isCompensationAnnotationPresent(compositeIndex)) {
            addCompensationsModuleDependency(unit);
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

    private boolean isCompensationAnnotationPresent(CompositeIndex compositeIndex) {
        return ANNOTATIONS.stream().map(annotation -> DotName.createSimple(annotation.getName()))
                .filter(name -> !compositeIndex.getAnnotations(name).isEmpty()).count() > 0;
    }

    private void addCompensationsModuleDependency(DeploymentUnit unit) {
        ModuleDependency dependency = new ModuleDependency(Module.getBootModuleLoader(), COMPENSATIONS_MODULE, false, false,
                true, false);
        ModuleSpecification moduleSpec = unit.getAttachment(Attachments.MODULE_SPECIFICATION);
        moduleSpec.addSystemDependency(dependency);
    }

}
