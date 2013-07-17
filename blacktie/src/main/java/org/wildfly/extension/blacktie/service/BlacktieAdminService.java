/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.blacktie.service;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
//import org.jboss.narayana.blacktie.administration.BlacktieAdminServiceMBean;
import org.wildfly.extension.blacktie.logging.BlacktieLogger;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 *
 */
public class BlacktieAdminService implements Service<BlacktieAdminService> {
    //private BlacktieAdminServiceMBean mbean = null;

    @Override
    public BlacktieAdminService getValue() throws IllegalStateException, IllegalArgumentException {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieAdminService.getValue");
        }
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieAdminService.start");
        }

        /*
        try{
            BlacktieLogger.ROOT_LOGGER.info("Starting BlacktieAdminService");
            mbean = new org.jboss.narayana.blacktie.administration.BlacktieAdminService();
            mbean.start();
            BlacktieLogger.ROOT_LOGGER.info("Started BlacktieAdminService");
        }catch (Exception e) {
            throw new StartException(e.getMessage());
        }
        */
    }

    @Override
    public void stop(StopContext context) {
        if (BlacktieLogger.ROOT_LOGGER.isTraceEnabled()) {
            BlacktieLogger.ROOT_LOGGER.trace("BlacktieAdminService.stop");
        }
        /*
        try {
            BlacktieLogger.ROOT_LOGGER.info("Stopping BlacktieAdminService");
            if(mbean != null) {
                mbean.stop();
            }
            BlacktieLogger.ROOT_LOGGER.info("Stopped BlacktieAdminService");
        } catch (Exception e) {
            BlacktieLogger.ROOT_LOGGER.warn("Stop BlacktieAdminService failed with " + e);
        }
        */
    }
}
