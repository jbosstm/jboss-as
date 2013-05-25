package org.wildfly.extension.rts.jaxrs;

import java.util.Set;

import org.jboss.jbossts.star.service.TMApplication;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class CoordinatorApplication extends AbstractRTSApplication {

    private final TMApplication tmApplication;

    public CoordinatorApplication() {
        tmApplication = new TMApplication();
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();
        classes.addAll(tmApplication.getClasses());

        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = super.getSingletons();
        singletons.addAll(tmApplication.getSingletons());

        return singletons;
    }
}
