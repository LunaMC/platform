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

import io.lunamc.platform.service.ServiceRegistry;

import java.util.Objects;

public class DefaultPluginContext implements PluginContext {

    private final DefaultPluginDescription description;
    private final ServiceRegistry serviceRegistry;
    private final PluginManager pluginManager;

    public DefaultPluginContext(DefaultPluginDescription description, ServiceRegistry serviceRegistry, PluginManager pluginManager) {
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.serviceRegistry = Objects.requireNonNull(serviceRegistry, "serviceRegistry must not be null");
        this.pluginManager = Objects.requireNonNull(pluginManager, "pluginManager must not be null");
    }

    @Override
    public DefaultPluginDescription getDescription() {
        return description;
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
