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

package io.lunamc.platform.security;

import io.lunamc.platform.plugin.PluginContextual;

import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;

public class LunaSecurityPolicy extends Policy {

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
        ClassLoader classLoader = domain.getClassLoader();
        if (classLoader instanceof PluginContextual) {
            PluginContextual contextual = (PluginContextual) classLoader;
            for (Permission pluginPermission : contextual.getPluginDescription().getPermissions()) {
                if (pluginPermission.implies(permission))
                    return true;
            }
            return false;
        }

        return true;
    }
}
