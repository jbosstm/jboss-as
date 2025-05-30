/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.ejb3.security;

import static java.security.AccessController.doPrivileged;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import jakarta.security.jacc.EJBMethodPermission;

import org.jboss.as.ee.component.Component;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.ejb3.component.EJBComponent;
import org.jboss.as.ejb3.logging.EjbLogger;
import org.jboss.invocation.Interceptor;
import org.jboss.invocation.InterceptorContext;
import org.jboss.metadata.ejb.spec.MethodInterfaceType;
import org.wildfly.common.Assert;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.authz.jacc.PolicyUtil;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class JaccInterceptor implements Interceptor {

    private static final Principal[] NO_PRINCIPALS = new Principal[0];

    private final String viewClassName;
    private final Method viewMethod;

    public JaccInterceptor(String viewClassName, Method viewMethod) {
        this.viewClassName = viewClassName;
        this.viewMethod = viewMethod;
    }

    @Override
    public Object processInvocation(InterceptorContext context) throws Exception {
        Component component = context.getPrivateData(Component.class);
        final SecurityDomain securityDomain = context.getPrivateData(SecurityDomain.class);
        Assert.checkNotNullParam("securityDomain", securityDomain);
        final SecurityIdentity currentIdentity = securityDomain.getCurrentSecurityIdentity();

        if (component instanceof EJBComponent == false) {
            throw EjbLogger.ROOT_LOGGER.unexpectedComponent(component, EJBComponent.class);
        }

        Method invokedMethod = context.getMethod();
        ComponentView componentView = context.getPrivateData(ComponentView.class);
        String viewClassOfInvokedMethod = componentView.getViewClass().getName();

        // shouldn't really happen if the interceptor was setup correctly. But let's be safe and do a check
        if (!viewClassName.equals(viewClassOfInvokedMethod) || !viewMethod.equals(invokedMethod)) {
            throw EjbLogger.ROOT_LOGGER.failProcessInvocation(getClass().getName(), invokedMethod, viewClassOfInvokedMethod, viewMethod, viewClassName);
        }

        EJBComponent ejbComponent = (EJBComponent) component;

        if(WildFlySecurityManager.isChecking()) {
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                    hasPermission(ejbComponent, componentView, invokedMethod, currentIdentity);
                    return null;
                });
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        } else {
            hasPermission(ejbComponent, componentView, invokedMethod, currentIdentity);
        }

        // successful authorization, let the invocation proceed
        return context.proceed();
    }

    private EJBMethodPermission createEjbMethodPermission(Method invokedMethod, EJBComponent ejbComponent, MethodInterfaceType methodIntfType) {
        return new EJBMethodPermission(ejbComponent.getComponentName(), methodIntfType.name(), invokedMethod);
    }

    private void hasPermission(EJBComponent ejbComponent, ComponentView componentView, Method method, SecurityIdentity securityIdentity) {
        MethodInterfaceType methodIntfType = componentView.getPrivateData(MethodInterfaceType.class);
        EJBMethodPermission permission = createEjbMethodPermission(method, ejbComponent, methodIntfType);
        ProtectionDomain domain = new ProtectionDomain (componentView.getProxyClass().getProtectionDomain().getCodeSource(), null, null, getGrantedRoles(securityIdentity));
        PolicyUtil policyUtil = WildFlySecurityManager.isChecking() ? doPrivileged((PrivilegedAction<PolicyUtil>) PolicyUtil::getPolicyUtil) : PolicyUtil.getPolicyUtil();
        if (!policyUtil.implies(domain, permission)) {
            throw EjbLogger.ROOT_LOGGER.invocationOfMethodNotAllowed(method,ejbComponent.getComponentName());
        }
    }

    /**
     * Returns an array of {@link Principal} representing the roles associated with the identity
     * invoking the Jakarta Enterprise Beans. This method will check performs checks against run as identities in order to
     * resolve the correct set of roles to be granted.
     *
     * @param securityIdentity the identity invoking the Jakarta Enterprise Beans
     * @return an array of {@link Principal} representing the roles associated with the identity
     */
    public static Principal[] getGrantedRoles(SecurityIdentity securityIdentity) {
        Set<String> roles = new HashSet<>();

        for (String s : securityIdentity.getRoles("ejb")) {
            roles.add(s);
        }
        List<Principal> list = new ArrayList<>();
        Function<String, Principal> mapper = roleName -> (Principal) () -> roleName;
        for (String role : roles) {
            Principal principal = mapper.apply(role);
            list.add(principal);
        }
        return list.toArray(NO_PRINCIPALS);
    }
}
