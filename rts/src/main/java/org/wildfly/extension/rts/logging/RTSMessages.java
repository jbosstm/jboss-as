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

package org.wildfly.extension.rts.logging;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

/**
 *
 * Reserved logging id ranges from: http://community.jboss.org/wiki/LoggingIds: 18800 - 18899
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
@MessageBundle(projectCode = "JBAS")
public interface RTSMessages {

    RTSMessages MESSAGES = Messages.getBundle(RTSMessages.class);

    @Message(id = 18800, value = "Default Undertow host is missing. Please make sure that default Undertow server and host are available. See: https://issues.jboss.org/browse/JBTM-1706.")
    RuntimeException defaultUndertowHostNotAvailable();

    @Message(id = 18801, value = "Default Undertow server is missing. Please make sure that default Undertow server and host are available. See: https://issues.jboss.org/browse/JBTM-1706.")
    RuntimeException defaultUndertowServerNotAvailable();

}
