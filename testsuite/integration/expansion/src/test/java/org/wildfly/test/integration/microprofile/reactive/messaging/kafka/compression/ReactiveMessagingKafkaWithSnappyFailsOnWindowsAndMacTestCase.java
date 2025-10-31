/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.test.integration.microprofile.reactive.messaging.kafka.compression;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.testcontainers.api.TestcontainersRequired;
import org.jboss.as.arquillian.api.ServerSetup;
import org.junit.runner.RunWith;
import org.wildfly.test.integration.microprofile.reactive.EnableReactiveExtensionsSetupTask;
import org.wildfly.test.integration.microprofile.reactive.RunKafkaSetupTask;

@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup({RunKafkaSetupTask.class, EnableReactiveExtensionsSetupTask.class})
@TestcontainersRequired
@org.junit.Ignore
public class ReactiveMessagingKafkaWithSnappyFailsOnWindowsAndMacTestCase extends AbstractReactiveMessagingKafkaWithNativeCompressionFailsOnWindowsAndMacTestCase {
    public ReactiveMessagingKafkaWithSnappyFailsOnWindowsAndMacTestCase() {
        super("microprofile-config-snappy.properties");
    }
}
