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

import io.lunamc.platform.plugin.classloader.PluginClassLoader;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DefaultPluginDescription implements PluginDescription {

    private Plugin instance;
    private final PluginClassLoader classLoader;
    private final PluginDescriptor descriptor;
    private final List<PluginDescription> pluginDependencies;
    private final Set<Permission> permissions;
    private final boolean global;
    private final File dataDirectory;
    private volatile boolean active;

    public DefaultPluginDescription(PluginClassLoader classLoader,
                                    PluginDescriptor descriptor,
                                    List<PluginDescription> pluginDependencies,
                                    Set<Permission> permissions,
                                    boolean global,
                                    File dataDirectory) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
        this.pluginDependencies = pluginDependencies != null ? Collections.unmodifiableList(new ArrayList<>(pluginDependencies)) : Collections.emptyList();
        this.permissions = permissions != null ? Collections.unmodifiableSet(new HashSet<>(permissions)) : Collections.emptySet();
        this.global = global;
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory must not be null");
    }

    public Plugin getInstance() {
        return instance;
    }

    public void setInstance(Plugin instance) {
        this.instance = instance;
    }

    @Override
    public PluginClassLoader getClassLoader() {
        return classLoader;
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public List<PluginDescription> getPluginDependencies() {
        return pluginDependencies;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    @Override
    public File getDataDirectory() {
        return dataDirectory;
    }

    @Override
    public String toString() {
        return Objects.toString(getDescriptor());
    }
}
