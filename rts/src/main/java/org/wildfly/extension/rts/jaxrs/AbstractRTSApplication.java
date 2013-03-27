package org.wildfly.extension.rts.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.jboss.resteasy.core.AcceptHeaderByFileSuffixFilter;
import org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPFilter;
import org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPInterceptor;
import org.jboss.resteasy.plugins.interceptors.encoding.GZIPDecodingInterceptor;
import org.jboss.resteasy.plugins.interceptors.encoding.GZIPEncodingInterceptor;
import org.jboss.resteasy.plugins.providers.DataSourceProvider;
import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import org.jboss.resteasy.plugins.providers.DocumentProvider;
import org.jboss.resteasy.plugins.providers.FileProvider;
import org.jboss.resteasy.plugins.providers.FormUrlEncodedProvider;
import org.jboss.resteasy.plugins.providers.IIOImageProvider;
import org.jboss.resteasy.plugins.providers.InputStreamProvider;
import org.jboss.resteasy.plugins.providers.JaxrsFormProvider;
import org.jboss.resteasy.plugins.providers.SerializableProvider;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.plugins.providers.jaxb.XmlJAXBContextFinder;
import org.jboss.resteasy.plugins.providers.jaxb.json.JettisonElementProvider;
import org.jboss.resteasy.plugins.providers.jaxb.json.JettisonXmlRootElementProvider;
import org.jboss.resteasy.plugins.providers.jaxb.json.JettisonXmlSeeAlsoProvider;
import org.jboss.resteasy.plugins.providers.jaxb.json.JettisonXmlTypeProvider;
import org.jboss.resteasy.plugins.providers.jaxb.json.JsonCollectionProvider;
import org.jboss.resteasy.plugins.providers.jaxb.json.JsonJAXBContextFinder;
import org.jboss.resteasy.plugins.providers.jaxb.json.JsonMapProvider;
import org.jboss.resteasy.plugins.providers.validation.ResteasyViolationExceptionMapper;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractRTSApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.addAll(getDefaultProviders());
        classes.addAll(getJaxbProviders());
        classes.addAll(getJettisonProviders());
        classes.addAll(getDefaultInterceptors());

        return classes;
    }

    private Set<Class<?>> getDefaultProviders() {
        Set<Class<?>> providers = new HashSet<>();

        // Message body writers / readers
        providers.add(DocumentProvider.class);
        providers.add(FormUrlEncodedProvider.class);
        providers.add(DefaultTextPlain.class);
        providers.add(SerializableProvider.class);
        providers.add(FileProvider.class);
        providers.add(InputStreamProvider.class);
        providers.add(JaxrsFormProvider.class);
        providers.add(StringTextStar.class);
        providers.add(IIOImageProvider.class);
        providers.add(DataSourceProvider.class);

        // Mappers
        providers.add(ResteasyViolationExceptionMapper.class);

        return providers;
    }

    private Set<Class<?>> getDefaultInterceptors() {
        Set<Class<?>> providers = new HashSet<>();

        providers.add(AcceptHeaderByFileSuffixFilter.class);
        providers.add(AcceptEncodingGZIPFilter.class);
        providers.add(GZIPEncodingInterceptor.class);
        providers.add(AcceptEncodingGZIPInterceptor.class);
        providers.add(GZIPDecodingInterceptor.class);

        return providers;
    }

    private Set<Class<?>> getJaxbProviders() {
        Set<Class<?>> providers = new HashSet<>();

        // Message body writers / readers
        providers.add(org.jboss.resteasy.plugins.providers.jaxb.CollectionProvider.class);
        providers.add(org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlRootElementProvider.class);
        providers.add(org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlSeeAlsoProvider.class);
        providers.add(org.jboss.resteasy.plugins.providers.jaxb.JAXBXmlTypeProvider.class);
        providers.add(org.jboss.resteasy.plugins.providers.jaxb.JAXBElementProvider.class);
        providers.add(org.jboss.resteasy.plugins.providers.jaxb.MapProvider.class);

        // Context resolvers
        providers.add(XmlJAXBContextFinder.class);

        return providers;
    }

    private Set<Class<?>> getJettisonProviders() {
        Set<Class<?>> providers = new HashSet<>();

        providers.add(JettisonElementProvider.class);
        providers.add(JettisonXmlTypeProvider.class);
        providers.add(JsonMapProvider.class);
        providers.add(JettisonXmlRootElementProvider.class);
        providers.add(JettisonXmlSeeAlsoProvider.class);
        providers.add(JsonCollectionProvider.class);
        providers.add(JsonJAXBContextFinder.class);

        return providers;
    }
}
