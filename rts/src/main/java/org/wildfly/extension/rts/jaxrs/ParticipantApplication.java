package org.wildfly.extension.rts.jaxrs;

import java.util.Set;

import org.jboss.narayana.rest.integration.HeuristicMapper;
import org.jboss.narayana.rest.integration.ParticipantResource;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public final class ParticipantApplication extends AbstractRTSApplication {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = super.getClasses();

        classes.add(ParticipantResource.class);
        classes.add(HeuristicMapper.class);

        return classes;
    }

}
