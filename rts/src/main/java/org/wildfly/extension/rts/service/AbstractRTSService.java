package org.wildfly.extension.rts.service;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.jboss.jbossts.star.service.ContextListener;
import org.jboss.msc.value.InjectedValue;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.wildfly.extension.rts.logging.RTSLogger;
import org.wildfly.extension.rts.logging.RTSMessages;
import org.wildfly.extension.undertow.Host;
import org.wildfly.extension.undertow.Server;
import org.wildfly.extension.undertow.UndertowService;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public class AbstractRTSService {

    private static final String DEFAULT_SERVER_NAME = "default-server";

    private static final String DEFAULT_HOST_NAME = "default-host";

    private InjectedValue<UndertowService> injectedUndertowService = new InjectedValue<>();

    private Host defaulthost;

    private Server defaultServer;

    public InjectedValue<UndertowService> getInjectedUndertowService() {
        return injectedUndertowService;
    }

    protected DeploymentInfo getDeploymentInfo(final String name, final String contextPath, final Map<String, String> initialParameters) {
        final DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setClassLoader(ParticipantService.class.getClassLoader());
        deploymentInfo.setContextPath(contextPath);
        deploymentInfo.setDeploymentName(name);
        deploymentInfo.addServlets(getResteasyServlet());
        deploymentInfo.addListener(getResteasyListener());
        deploymentInfo.addListener(getRestATListener());

        for (Entry<String, String> entry : initialParameters.entrySet()) {
            deploymentInfo.addInitParameter(entry.getKey(), entry.getValue());
        }

        return deploymentInfo;
    }

    protected void deployServlet(final DeploymentInfo deploymentInfo) {
        DeploymentManager manager = ServletContainer.Factory.newInstance().addDeployment(deploymentInfo);

        manager.deploy();

        try {
            getHost().registerDeployment(deploymentInfo, manager.start());
        } catch (ServletException e) {
            RTSLogger.ROOT_LOGGER.warn(e.getMessage(), e);
        }
    }

    protected void undeployServlet(final DeploymentInfo deploymentInfo) {
        if (deploymentInfo != null) {
            getHost().unregisterDeployment(deploymentInfo);
        }
    }

    private Host getHost() {
        if (defaulthost != null) {
            return defaulthost;
        }

        final Server defaultServer = getServer();

        for (Host host : defaultServer.getHosts()) {
            if (host.getName().equals(DEFAULT_HOST_NAME)) {
                defaulthost = host;
                break;
            }
        }

        if (defaulthost == null) {
            throw RTSMessages.MESSAGES.defaultUndertowHostNotAvailable();
        }

        return defaulthost;
    }

    private Server getServer() {
        if (defaultServer != null) {
            return defaultServer;
        }

        for (Server server : injectedUndertowService.getValue().getServers()) {
            if (server.getName().equals(DEFAULT_SERVER_NAME)) {
                defaultServer = server;
                break;
            }
        }

        if (defaultServer == null) {
            throw RTSMessages.MESSAGES.defaultUndertowServerNotAvailable();
        }

        return defaultServer;
    }

    private ServletInfo getResteasyServlet() {
        final ServletInfo servletInfo = new ServletInfo("Resteasy", HttpServletDispatcher.class);
        servletInfo.addMapping("/*");

        return servletInfo;
    }

    private ListenerInfo getResteasyListener() {
        final ListenerInfo listenerInfo = new ListenerInfo(ResteasyBootstrap.class);

        return listenerInfo;
    }

    private ListenerInfo getRestATListener() {
        final ListenerInfo listenerInfo = new ListenerInfo(ContextListener.class);

        return listenerInfo;
    }

}
