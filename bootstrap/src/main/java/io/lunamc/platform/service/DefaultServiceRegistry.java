/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.lunamc.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceRegistry.class);
    private static final Marker MARKER_SERVICES = MarkerFactory.getMarker("SERVICES");

    private final Map<Class<?>, ServiceRegistration<?>> serviceRegistrations = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> ServiceRegistration<T> getService(Class<T> serviceClass) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(ServiceRegistryPermission.PERMISSION_ACCESS);

        return (ServiceRegistration<T>) serviceRegistrations.computeIfAbsent(serviceClass, DefaultServiceRegistration::new);
    }

    @Override
    public Collection<ServiceRegistration<?>> getServices() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(ServiceRegistryPermission.PERMISSION_ACCESS);

        return Collections.unmodifiableCollection(serviceRegistrations.values());
    }

    @Override
    public void start() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(ServiceRegistryPermission.PERMISSION_START_OR_STOP);

        LOGGER.info(MARKER_SERVICES, "Starting services...");
        long timer = System.currentTimeMillis();
        getServices().stream()
                .filter(r -> {
                    Object instance = r.getInstance();
                    return instance != null && instance instanceof Startable;
                })
                .sorted((o1, o2) -> Integer.compare(((Startable) o2.getInstance()).getStartPriority(), ((Startable) o1.getInstance()).getStartPriority()))
                .forEach(i -> {
                    LOGGER.debug(MARKER_SERVICES, "Starting {}...", i.getService().getName());
                    try {
                        ((Startable) i.getInstance()).start();
                    } catch (Throwable throwable) {
                        LOGGER.warn(MARKER_SERVICES, "An exception ({}) occurred while starting {}", i, throwable.getClass().getName(), i.getService().getName(), throwable);
                    }
                });
        timer = System.currentTimeMillis() - timer;
        LOGGER.info(MARKER_SERVICES, "Services started (took {} ms)", timer);
    }

    @Override
    public void shutdown() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(ServiceRegistryPermission.PERMISSION_START_OR_STOP);

        LOGGER.info(MARKER_SERVICES, "Shutting down services...");
        long timer = System.currentTimeMillis();
        getServices().stream()
                .filter(r -> {
                    Object instance = r.getInstance();
                    return instance != null && instance instanceof Shutdownable;
                })
                .sorted((o1, o2) -> Integer.compare(((Shutdownable) o2.getInstance()).getShutdownPriority(), ((Shutdownable) o1.getInstance()).getShutdownPriority()))
                .forEach(i -> {
                    LOGGER.debug(MARKER_SERVICES, "Shutting down {}...", i.getService().getName());
                    try {
                        ((Shutdownable) i.getInstance()).shutdown();
                    } catch (Throwable throwable) {
                        LOGGER.warn(MARKER_SERVICES, "An exception ({}) occurred while shutting down {}", throwable.getClass().getName(), i.getService().getName(), throwable);
                    }
                });
        timer = System.currentTimeMillis() - timer;
        LOGGER.info(MARKER_SERVICES, "Services shut down (took {} ms)", timer);
    }

    private static class DefaultServiceRegistration<T> implements ServiceRegistration<T> {

        private final Class<T> service;
        private volatile T instance;

        private DefaultServiceRegistration(Class<T> service) {
            this.service = Objects.requireNonNull(service, "service must not be null");
        }

        @Override
        public Class<T> getService() {
            return service;
        }

        @Override
        public T getInstance() {
            return instance;
        }

        @Override
        public void setInstance(T instance) {
            this.instance = instance;
        }

        @Override
        public String toString() {
            return service.getName();
        }
    }
}
