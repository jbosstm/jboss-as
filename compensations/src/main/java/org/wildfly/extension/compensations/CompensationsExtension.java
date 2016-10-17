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

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.msc.service.ServiceName;

/**
 * Deployment processor for the dependencies Recovery service
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationsExtension implements Extension {

    private static final String SUBSYSTEM_NAME = "compensations";

    private static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM,
            SUBSYSTEM_NAME);

    private static final ModelVersion CURRENT_MODEL_VERSION = ModelVersion.create(1, 0, 0);

    private static final String RESOURCE_NAME = CompensationsExtension.class.getPackage().getName() + ".LocalDescriptions";

    private static final String NAMESPACE = "urn:jboss:domain:compensations:1.0";

    static final ServiceName RECOVERY_SERVICE_NAME = ServiceName.JBOSS.append(SUBSYSTEM_NAME).append("recovery");

    @Override
    public void initialize(ExtensionContext context) {
        SubsystemRegistration subsystemRegistration = context.registerSubsystem(SUBSYSTEM_NAME, CURRENT_MODEL_VERSION);
        subsystemRegistration.registerSubsystemModel(getSubsystemDefinition());
        subsystemRegistration.registerXMLElementWriter(new CompensationsSubsystemParser(SUBSYSTEM_PATH, NAMESPACE));
    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, new CompensationsSubsystemParser(SUBSYSTEM_PATH, NAMESPACE));
    }

    private SimpleResourceDefinition getSubsystemDefinition() {
        return new SimpleResourceDefinition(SUBSYSTEM_PATH,
                new StandardResourceDescriptionResolver(null, RESOURCE_NAME, getClass().getClassLoader(), true, false),
                new CompensationsSubsystemAdd(SUBSYSTEM_NAME), null);
    }

}
