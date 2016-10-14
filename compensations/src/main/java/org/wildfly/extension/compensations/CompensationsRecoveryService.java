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

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import org.jboss.jbossts.xts.recovery.coordinator.CoordinatorRecoveryInitialisation;
import org.jboss.jbossts.xts.recovery.participant.ParticipantRecoveryInitialisation;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.narayana.compensations.internal.recovery.CompensationContextStateRecoveryModule;
import org.jboss.narayana.compensations.internal.recovery.local.LocalParticipantRecoveryModule;
import org.jboss.narayana.compensations.internal.recovery.remote.RemoteParticipantRecoveryModule;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class CompensationsRecoveryService implements Service<CompensationsRecoveryService> {

    private LocalParticipantRecoveryModule localParticipantRecoveryModule;

    private RemoteParticipantRecoveryModule remoteParticipantRecoveryModule;

    private CompensationContextStateRecoveryModule compensationContextStateRecoveryModule;

    @Override
    public void start(StartContext startContext) throws StartException {
        CoordinatorRecoveryInitialisation.startup();
        ParticipantRecoveryInitialisation.startup();
        localParticipantRecoveryModule = new LocalParticipantRecoveryModule();
        remoteParticipantRecoveryModule = new RemoteParticipantRecoveryModule();
        compensationContextStateRecoveryModule = new CompensationContextStateRecoveryModule();
        RecoveryManager.manager().addModule(localParticipantRecoveryModule);
        RecoveryManager.manager().addModule(compensationContextStateRecoveryModule);
        XTSBARecoveryManager.getRecoveryManager().registerRecoveryModule(remoteParticipantRecoveryModule);
    }

    @Override
    public void stop(StopContext stopContext) {
        RecoveryManager.manager().removeModule(localParticipantRecoveryModule, true);
        RecoveryManager.manager().removeModule(compensationContextStateRecoveryModule, true);
        XTSBARecoveryManager.getRecoveryManager().unregisterRecoveryModule(remoteParticipantRecoveryModule);
        CoordinatorRecoveryInitialisation.shutdown();
        ParticipantRecoveryInitialisation.shutdown();
    }

    @Override
    public CompensationsRecoveryService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

}
