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

package io.lunamc.platform;

import io.lunamc.platform.config.PluginsConfiguration;
import io.lunamc.platform.plugin.DefaultPluginManager;
import io.lunamc.platform.plugin.PluginManager;
import io.lunamc.platform.service.DefaultServiceRegistry;
import io.lunamc.platform.service.ServiceRegistry;
import io.lunamc.platform.utils.PermissionUtils;
import io.lunamc.platform.utils.InstanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AllPermission;
import java.security.Permission;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LunaPlatform {

    private static final Logger LOGGER = LoggerFactory.getLogger(LunaPlatform.class);
    private static final int HANGING_THREAD_TIMEOUT = 3_000;
    private static final String PLUGIN_MANAGER_CLASS;
    private static final String SERVICE_REGISTRY_CLASS;
    private static final File PLUGINS_FILE;

    static {
        PLUGIN_MANAGER_CLASS = System.getProperty("io.lunamc.platform.pluginManagerImpl", DefaultPluginManager.class.getName());
        SERVICE_REGISTRY_CLASS = System.getProperty("io.lunamc.platform.serviceRegistryImpl", DefaultServiceRegistry.class.getName());
        PLUGINS_FILE = new File(System.getProperty("io.lunamc.platform.pluginsFile", "plugins.xml"));
    }

    private final PluginManager pluginManager = InstanceUtils.createInstance(PLUGIN_MANAGER_CLASS, DefaultPluginManager.class);
    private final ServiceRegistry serviceRegistry = InstanceUtils.createInstance(SERVICE_REGISTRY_CLASS, DefaultServiceRegistry.class);
    private final Object notifier = new Object();
    private final Set<Thread> threadKillExemptions;
    private volatile boolean started;

    public LunaPlatform() {
        LOGGER.info("Initializing Luna...");

        pluginManager.initialize(serviceRegistry);
        threadKillExemptions = new HashSet<>();
    }

    public synchronized void start() {
        if (started)
            throw new IllegalStateException("Already started");
        LOGGER.info("Starting Luna...");
        long timer = System.currentTimeMillis();
        threadKillExemptions.addAll(Thread.getAllStackTraces().keySet());
        loadPlugins();
        pluginManager.initializePlugins();
        serviceRegistry.start();
        pluginManager.startPlugins();
        started = true;
        timer = System.currentTimeMillis() - timer;
        LOGGER.info("Luna started (took {} ms)", timer);
    }

    public void waitForStop() {
        synchronized (notifier) {
            try {
                notifier.wait();
            } catch (InterruptedException ignore) {
            }
        }
    }

    public synchronized void stop() {
        if (!started)
            throw new IllegalStateException("Not started");
        started = false;
        LOGGER.info("Shutting down Luna...");
        long timer = System.currentTimeMillis();
        shutdown();
        timer = System.currentTimeMillis() - timer;
        LOGGER.info("Luna shut down (took {} ms)", timer);

        synchronized (notifier) {
            notifier.notifyAll();
        }

        killHangingThreads();
        threadKillExemptions.clear();
    }

    public synchronized void safeStop() {
        if (started)
            stop();
    }

    private void shutdown() {
        serviceRegistry.shutdown();
        pluginManager.shutdown();
    }

    private void loadPlugins() {
        PluginsConfiguration pluginsConfiguration;
        try {
            pluginsConfiguration = loadPluginsConfiguration();
        } catch (IOException e) {
            LOGGER.error("Failed to load plugins declaration file", e);
            return;
        }
        List<PluginsConfiguration.PluginConfiguration> plugins = pluginsConfiguration.getPlugins();
        if (plugins == null || plugins.isEmpty()) {
            LOGGER.warn("No plugins installed.");
            return;
        }
        for (PluginsConfiguration.PluginConfiguration pluginConfiguration : plugins) {
            File pluginFile = new File(pluginConfiguration.getFile());
            try {
                if (!pluginFile.isFile())
                    throw new FileNotFoundException(pluginFile.getAbsolutePath());
                pluginManager.register(
                        pluginFile,
                        pluginConfiguration.getId(),
                        (classLoader -> {
                            PluginsConfiguration.PluginSecurity pluginSecurity = pluginConfiguration.getSecurity();
                            if (pluginSecurity == null)
                                return Collections.emptySet();
                            List<PluginsConfiguration.PluginPermission> pluginPermissions = pluginSecurity.getPermissions();
                            if (pluginPermissions == null)
                                return Collections.emptySet();
                            Set<Permission> result = new HashSet<>(pluginPermissions.size());
                            boolean warned = false;
                            for (PluginsConfiguration.PluginPermission pluginPermission : pluginPermissions) {
                                Permission permission = PermissionUtils.createPermission(classLoader, pluginPermission.getImpl(), pluginPermission.getName(), pluginPermission.getAction());
                                if (permission instanceof AllPermission && !warned) {
                                    warned = true;
                                    LOGGER.warn("Plugin {} from {} will have all permissions!", pluginConfiguration.getId(), pluginFile.getAbsolutePath());
                                }
                                result.add(permission);
                            }
                            return result;
                        })
                );
            } catch (Throwable throwable) {
                LOGGER.error("Error while registering " + pluginConfiguration.getId() + " from " + pluginConfiguration.getFile(), throwable);
            }
        }
    }

    private PluginsConfiguration loadPluginsConfiguration() throws IOException {
        try (FileInputStream inputStream = new FileInputStream(PLUGINS_FILE)) {
            return PluginsConfiguration.load(inputStream);
        }
    }

    private void killHangingThreads() {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> entry : threads.entrySet()) {
            Thread thread = entry.getKey();
            if (Thread.currentThread() == thread || threadKillExemptions.contains(thread) || !thread.isAlive())
                break;

            LOGGER.warn("There is a hanging thread: {}", thread.getName());
            try {
                thread.join(HANGING_THREAD_TIMEOUT);
            } catch (InterruptedException ignore) {
            }
            if (!thread.isAlive()) {
                LOGGER.info("Thread \"{}\" killed successfully", thread.getName());
            } else {
                StringBuilder sb = new StringBuilder("Thread does not finished and will be interrupted: ")
                        .append(thread.getName())
                        .append(" (current state: ")
                        .append(thread.getState())
                        .append(')');
                for (StackTraceElement stackTraceElement : entry.getValue())
                    sb.append(System.lineSeparator()).append('\t').append(stackTraceElement);
                LOGGER.warn(sb.toString());

                thread.interrupt();
            }
        }
    }
}
