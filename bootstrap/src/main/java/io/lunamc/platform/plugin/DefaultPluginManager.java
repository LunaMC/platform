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

package io.lunamc.platform.plugin;

import io.lunamc.platform.internal.plugin.annotation.processor.ProvidedPlugins;
import io.lunamc.platform.plugin.classloader.PluginClassLoader;
import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.platform.service.ServiceRegistry;
import io.lunamc.platform.service.ServiceRegistryPermission;
import io.lunamc.platform.utils.InstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultPluginManager implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPluginManager.class);
    private static final Marker MARKER_PLUGIN = MarkerFactory.getMarker("PLUGIN");
    private static final Marker MARKER_SERVICES = MarkerFactory.getMarker("SERVICES");
    private static final File PLUGINS_DIRECTORY;

    static {
        PLUGINS_DIRECTORY = new File(System.getProperty("io.lunamc.platform.pluginsDataDirectory", "plugins"));
        LOGGER.info("Using plugins data directory {}", PLUGINS_DIRECTORY.getAbsolutePath());
    }

    private final ConcurrentMap<String, DefaultPluginContext> plugins = new ConcurrentHashMap<>();
    private final ConcurrentMap<File, ProvidedPlugins> providedPluginsMap = new ConcurrentHashMap<>();
    private volatile boolean initialized;
    private ServiceRegistry serviceRegistry;

    @Override
    public synchronized void initialize(ServiceRegistry serviceRegistry) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_MANAGE);

        if (initialized)
            throw new IllegalStateException("Already initialized");
        initialized = true;
        try {
            this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry must not be null");
        } catch (RuntimeException e) {
            initialized = false;
            throw e;
        }
    }

    @Override
    public Collection<DefaultPluginDescription> getPlugins() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_READ);

        checkState();
        return plugins.values().stream()
                .map(DefaultPluginContext::getDescription)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DefaultPluginDescription> getPlugin(String id) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_READ);

        DefaultPluginContext context = plugins.get(id);
        return context != null ? Optional.of(context.getDescription()) : Optional.empty();
    }

    @Override
    public void register(File file, String id, Function<ClassLoader, Collection<Permission>> permissionsSupplier) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_REGISTER);

        checkState();
        ClassLoader applicationClassLoader = getClass().getClassLoader();
        PluginClassLoader classLoader = new PluginClassLoader(file, applicationClassLoader);
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        ProvidedPlugins providedPlugins;
        try {
            providedPlugins = getProvidedPluginsForFile(classLoader, file);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        ProvidedPlugins.ProvidedPlugin providedPlugin = providedPlugins.getProvidedPlugin(id)
                .orElseThrow(() -> new PluginInitializationException("Unknown plugin \"" + id + "\" in file " + file.getAbsolutePath()));

        PluginDescriptor descriptor = providedPlugin.toPluginDescriptor();
        String entryClassName = providedPlugin.getImpl();
        try {
            Class<?> entryClass;
            try {
                entryClass = classLoader.loadClassFromSelf(entryClassName);
            } catch (ClassNotFoundException e) {
                throw new PluginInitializationException("Plugin entry class " + entryClassName + " not found", e);
            }

            if (plugins.containsKey(descriptor.getId()))
                throw new PluginInitializationException("Plugin already registered: " + descriptor.getId());

            List<PluginDescriptor.PluginDependency> dependencies = descriptor.getPluginDependencies();
            List<PluginDescription> dependencyDescriptions = new ArrayList<>(dependencies.size());
            for (PluginDescriptor.PluginDependency dependency : dependencies) {
                String dependencyId = dependency.getId();
                DefaultPluginContext context = plugins.get(dependencyId);
                if (context == null)
                    throw new PluginInitializationException("Dependency \"" + dependencyId + "\" not found");
                DefaultPluginDescription dependencyDescription = context.getDescription();
                String dependencyVersionExpression = dependency.getVersionExpression();
                if (!dependencyDescription.getDescriptor().getVersion().satisfies(dependencyVersionExpression))
                    throw new PluginInitializationException("Dependency \"" + dependencyId + "\" not matching required version expression " + dependencyVersionExpression);

                dependencyDescriptions.add(dependencyDescription);
            }

            boolean global = (file == null);
            File dataDirectory = new File(PLUGINS_DIRECTORY, descriptor.getId());
            Collection<Permission> additionalPermissions = permissionsSupplier.apply(classLoader);
            Set<Permission> permissions = new HashSet<>(additionalPermissions.size() + 2);
            permissions.addAll(additionalPermissions);
            permissions.add(new FilePermission(dataDirectory.getAbsolutePath() + File.separator + '-', "read,write,delete"));
            permissions.add(new ServiceRegistryPermission("access"));
            DefaultPluginDescription pluginDescription = new DefaultPluginDescription(
                    classLoader,
                    descriptor,
                    dependencyDescriptions,
                    permissions,
                    global,
                    dataDirectory
            );

            //noinspection ResultOfMethodCallIgnored
            dataDirectory.mkdirs();
            classLoader.initialize(pluginDescription);

            pluginDescription.setInstance(InstanceUtils.createInstance(entryClass, Plugin.class));

            DefaultPluginContext context = new DefaultPluginContext(
                    pluginDescription,
                    serviceRegistry,
                    this
            );

            DefaultPluginContext previous = plugins.putIfAbsent(descriptor.getId(), context);
            if (previous != null)
                throw new PluginInitializationException("Plugin already registered: " + descriptor.getId());

            if (!additionalPermissions.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                boolean comma = false;
                for (Permission permission : additionalPermissions) {
                    if (!comma)
                        comma = true;
                    else
                        sb.append(", ");
                    sb.append(permission);
                }
                LOGGER.info(MARKER_PLUGIN, "Plugin {} has additional permissions: {}", descriptor, sb.toString());
            }
            if (global)
                LOGGER.info(MARKER_PLUGIN, "Plugin {} was registered without any file context", descriptor);
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
    }

    @Override
    public void initializePlugins() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_MANAGE);

        checkState();
        listPlugins();
        triggerInitialization();
    }

    @Override
    public void startPlugins() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_MANAGE);

        triggerStart();
    }

    @Override
    public void shutdown() {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null)
            securityManager.checkPermission(PluginManagerPermission.PERMISSION_MANAGE);

        checkState();
        plugins.clear();
    }

    private void triggerInitialization() {
        LOGGER.info(MARKER_PLUGIN, "Initializing plugins...");
        long timer = System.currentTimeMillis();
        int counter = 0;
        Collection<DefaultPluginContext> plugins = this.plugins.values();
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            for (DefaultPluginContext context : plugins) {
                DefaultPluginDescription description = context.getDescription();
                PluginDescriptor descriptor = description.getDescriptor();
                LOGGER.debug(MARKER_PLUGIN, "Initializing {}...", descriptor);
                Thread.currentThread().setContextClassLoader(description.getClassLoader());
                boolean erroneous = false;
                try {
                    description.getInstance().initialize(context);
                } catch (Throwable throwable) {
                    LOGGER.error(MARKER_PLUGIN, "Plugin {} will not be loaded because an exception occurred while initialize plugin", descriptor, throwable);
                    erroneous = true;
                }
                description.setActive(!erroneous);
                if (!erroneous) {
                    counter++;
                    LOGGER.debug(MARKER_PLUGIN, "Plugin {} initialized", descriptor);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
        timer = System.currentTimeMillis() - timer;
        LOGGER.info(MARKER_PLUGIN, "{} of {} plugins initialized (took {} ms)", counter, plugins.size(), timer);
        debugServiceRegistrations(serviceRegistry);
    }

    private void triggerStart() {
        LOGGER.info(MARKER_PLUGIN, "Starting plugins...");
        long timer = System.currentTimeMillis();
        Collection<DefaultPluginContext> plugins = this.plugins.values();
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            for (DefaultPluginContext context : plugins) {
                DefaultPluginDescription description = context.getDescription();
                PluginDescriptor descriptor = description.getDescriptor();
                LOGGER.debug(MARKER_PLUGIN, "Starting {}...", descriptor);
                Thread.currentThread().setContextClassLoader(description.getClassLoader());
                try {
                    description.getInstance().start(context);
                } catch (Throwable throwable) {
                    LOGGER.error(MARKER_PLUGIN, "Plugin {} has thrown an exception while starting.", descriptor, throwable);
                }
                LOGGER.debug(MARKER_PLUGIN, "Plugin {} started", descriptor);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
        timer = System.currentTimeMillis() - timer;
        LOGGER.info(MARKER_PLUGIN, "{} plugins started (took {} ms)", plugins.size(), timer);
    }

    private void checkState() {
        if (!initialized)
            throw new IllegalStateException("Not initialized");
    }

    private void listPlugins() {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;
        for (DefaultPluginContext context : plugins.values()) {
            if (!comma)
                comma = true;
            else
                sb.append(", ");
            sb.append(context.getDescription().getDescriptor().getId());
        }
        LOGGER.info(MARKER_PLUGIN, "Currently registered plugins: {}", sb.toString());
    }

    private void debugServiceRegistrations(ServiceRegistry serviceRegistry) {
        for (ServiceRegistration<?> service : serviceRegistry.getServices()) {
            Object instance = service.getInstance();
            if (instance != null)
                LOGGER.debug(MARKER_SERVICES, "{} is implemented by {}", service.getService().getName(), instance.getClass().getName());
            else
                LOGGER.debug(MARKER_SERVICES, "{} has no implementation", service.getService().getName());
        }
    }

    private ProvidedPlugins getProvidedPluginsForFile(ClassLoader classLoader, File file) {
        return providedPluginsMap.computeIfAbsent(file, f -> {
            try (InputStream in = classLoader.getResourceAsStream("LUNAMC-RESOURCES/providedPlugins.xml")) {
                JAXBContext jaxbContext = JAXBContext.newInstance(ProvidedPlugins.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                return (ProvidedPlugins) unmarshaller.unmarshal(in);
            } catch (IOException | JAXBException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
