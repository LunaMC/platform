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

package io.lunamc.platform.plugin.classloader;

import io.lunamc.platform.plugin.PluginContextual;
import io.lunamc.platform.plugin.PluginDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

public class PluginClassLoader extends URLClassLoader implements PluginContextual {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginClassLoader.class);

    private final ClassLoader parent;
    private volatile boolean initialized;
    private PluginDescription pluginDescription;

    public PluginClassLoader(File pluginFile, ClassLoader parent) {
        this(toUrl(pluginFile), parent);
    }

    public PluginClassLoader(URL url, ClassLoader parent) {
        this(toUrls(url), parent);
    }

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, null);

        this.parent = Objects.requireNonNull(parent, "parent must not be null");
    }

    public synchronized void initialize(PluginDescription pluginDescription) {
        if (initialized)
            throw new IllegalStateException("Already initialized");
        initialized = true;
        try {
            this.pluginDescription = Objects.requireNonNull(pluginDescription, "pluginDescription must not be null");
        } catch (RuntimeException e) {
            initialized = false;
            throw e;
        }
    }

    @Override
    public PluginDescription getPluginDescription() {
        return pluginDescription;
    }

    public Class<?> loadClassFromSelf(String name) throws ClassNotFoundException {
        Class<?> aClass = loadSelf(name);
        if (aClass != null)
            return aClass;
        throw new ClassNotFoundException(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> aClass = loadSelf(name);
            if (aClass != null)
                return aClass;

            LOGGER.debug("Try loading class {} from parent class loader", name);
            aClass = loadParent(name);
            if (aClass != null) {
                LOGGER.debug("Found class {} on parent class loader", name);
                return aClass;
            }

            if (initialized) {
                for (PluginDescription pluginDescription : this.pluginDescription.getPluginDependencies()) {
                    LOGGER.debug("Try loading class {} from dependency plugin class loader of {}", name, pluginDescription.getDescriptor().getId());
                    aClass = load(pluginDescription.getClassLoader(), name);
                    if (aClass != null) {
                        LOGGER.debug("Found class {} on dependency plugin class loader of {}", name, pluginDescription.getDescriptor().getId());
                        return aClass;
                    }
                }
            }

            throw new ClassNotFoundException(name);
        }
    }

    private Class<?> loadSelf(String name) {
        LOGGER.debug("Try loading class {} from plugin source", name);
        Class<?> aClass;
        try {
            aClass = super.loadClass(name);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
        LOGGER.debug("Found class {} on plugin source of {}", name, pluginDescription != null ? pluginDescription.getDescriptor().getId() : "unknown");
        return aClass;
    }

    private Class<?> loadParent(String name) {
        return load(parent, name);
    }

    private static Class<?> load(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException ignore) {
            return null;
        }
    }

    private static URL[] toUrls(URL url) {
        if (url == null)
            return new URL[0];
        return new URL[] { url };
    }

    private static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
