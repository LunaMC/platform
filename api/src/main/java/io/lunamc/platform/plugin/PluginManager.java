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

import java.io.File;
import java.security.Permission;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public interface PluginManager {

    void initialize(ServiceRegistry serviceRegistry);

    Collection<? extends PluginDescription> getPlugins();

    Optional<? extends PluginDescription> getPlugin(String id);

    default void register(File file, String id) {
        register(file, id, (c) -> Collections.emptySet());
    }

    void register(File file, String id, Function<ClassLoader, Collection<Permission>> permissionsSupplier);

    void initializePlugins();

    void startPlugins();

    void shutdown();
}
