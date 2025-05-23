/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.persistence.jipijapa.hibernate7;

import static org.infinispan.hibernate.cache.spi.InfinispanProperties.COLLECTION_CACHE_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.DEF_ENTITY_RESOURCE;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.DEF_PENDING_PUTS_RESOURCE;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.DEF_QUERY_RESOURCE;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.DEF_TIMESTAMPS_RESOURCE;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.ENTITY_CACHE_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.IMMUTABLE_ENTITY_CACHE_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.INFINISPAN_CONFIG_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.NATURAL_ID_CACHE_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.PENDING_PUTS_CACHE_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.QUERY_CACHE_RESOURCE_PROP;
import static org.infinispan.hibernate.cache.spi.InfinispanProperties.TIMESTAMPS_CACHE_RESOURCE_PROP;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.hibernate.cfg.AvailableSettings;
import org.jipijapa.cache.spi.Classification;
import org.jipijapa.event.impl.internal.Notification;

/**
 * Second level cache setup.
 *
 * @author Scott Marlow
 */
public class HibernateSecondLevelCache {

    private static final String DEFAULT_REGION_FACTORY = "org.infinispan.hibernate.cache.v62.InfinispanRegionFactory";

    public static final String CACHE_TYPE = "cachetype";    // shared (Jakarta Persistence) or private (for native applications)
    public static final String CACHE_PRIVATE = "private";
    public static final String CONTAINER = "container";
    public static final String NAME = "name";
    public static final String CACHES = "caches";

    public static void addSecondLevelCacheDependencies(Properties mutableProperties, String scopedPersistenceUnitName) {

        if (mutableProperties.getProperty(AvailableSettings.CACHE_REGION_PREFIX) == null
                && scopedPersistenceUnitName != null) {
            // cache entries for this PU will be identified by scoped pu name + Entity class name
            mutableProperties.setProperty(AvailableSettings.CACHE_REGION_PREFIX, scopedPersistenceUnitName);
        }
        String regionFactory = mutableProperties.getProperty(AvailableSettings.CACHE_REGION_FACTORY);
        if (regionFactory == null) {
            regionFactory = DEFAULT_REGION_FACTORY;
            mutableProperties.setProperty(AvailableSettings.CACHE_REGION_FACTORY, regionFactory);
        }
        if (Boolean.parseBoolean(mutableProperties.getProperty(ManagedEmbeddedCacheManagerProvider.SHARED, ManagedEmbeddedCacheManagerProvider.DEFAULT_SHARED))) {
            // Set infinispan defaults
            String container = mutableProperties.getProperty(ManagedEmbeddedCacheManagerProvider.CACHE_CONTAINER);
            if (container == null) {
                container = ManagedEmbeddedCacheManagerProvider.DEFAULT_CACHE_CONTAINER;
                mutableProperties.setProperty(ManagedEmbeddedCacheManagerProvider.CACHE_CONTAINER, container);
            }

            /**
             * AS will need the ServiceBuilder<?> builder that used to be passed to PersistenceProviderAdaptor.addProviderDependencies
             */
            Properties cacheSettings = new Properties();
            cacheSettings.setProperty(CONTAINER, container);
            cacheSettings.setProperty(CACHES, String.join(" ", findCaches(mutableProperties)));

            Notification.addCacheDependencies(Classification.INFINISPAN, cacheSettings);
        }
    }

    public static Set<String> findCaches(Properties properties) {
        Set<String> caches = new HashSet<>();

        caches.add(properties.getProperty(ENTITY_CACHE_RESOURCE_PROP, DEF_ENTITY_RESOURCE));
        caches.add(properties.getProperty(IMMUTABLE_ENTITY_CACHE_RESOURCE_PROP, DEF_ENTITY_RESOURCE));
        caches.add(properties.getProperty(COLLECTION_CACHE_RESOURCE_PROP, DEF_ENTITY_RESOURCE));
        caches.add(properties.getProperty(NATURAL_ID_CACHE_RESOURCE_PROP, DEF_ENTITY_RESOURCE));
        caches.add(properties.getProperty(PENDING_PUTS_CACHE_RESOURCE_PROP, DEF_PENDING_PUTS_RESOURCE));

        if (Boolean.parseBoolean(properties.getProperty(AvailableSettings.USE_QUERY_CACHE))) {
            caches.add(properties.getProperty(QUERY_CACHE_RESOURCE_PROP, DEF_QUERY_RESOURCE));
            caches.add(properties.getProperty(TIMESTAMPS_CACHE_RESOURCE_PROP, DEF_TIMESTAMPS_RESOURCE));
        }

        int length = INFINISPAN_CONFIG_RESOURCE_PROP.length();
        String customRegionPrefix = INFINISPAN_CONFIG_RESOURCE_PROP.substring(0, length - 3) + properties.getProperty(AvailableSettings.CACHE_REGION_PREFIX, "");
        String customRegionSuffix = INFINISPAN_CONFIG_RESOURCE_PROP.substring(length - 4, length);

        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.startsWith(customRegionPrefix) && propertyName.endsWith(customRegionSuffix)) {
                caches.add(properties.getProperty(propertyName));
            }
        }

        return caches;
    }
}
