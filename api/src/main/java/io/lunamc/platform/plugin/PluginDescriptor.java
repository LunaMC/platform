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

import com.github.zafarkhaja.semver.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PluginDescriptor {

    private final String id;
    private final Version version;
    private final List<PluginDependency> pluginDependencies;

    public PluginDescriptor(String id, Version version) {
        this(id, version, null);
    }

    public PluginDescriptor(String id, Version version, Collection<PluginDependency> pluginDependencies) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.pluginDependencies = pluginDependencies != null ? Collections.unmodifiableList(new ArrayList<>(pluginDependencies)) : Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public Version getVersion() {
        return version;
    }

    public List<PluginDependency> getPluginDependencies() {
        return pluginDependencies;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{id=\"" + getId() +
                "\", version=" + getVersion() +
                ", pluginDependencies=" + getPluginDependencies() + ')';
    }

    public static class PluginDependency {

        private final String id;
        private final String versionExpression;

        public PluginDependency(String id, String versionExpression) {
            this.id = Objects.requireNonNull(id, "id must not be null");
            this.versionExpression = Objects.requireNonNull(versionExpression, "versionExpression must not be null");
        }

        public String getId() {
            return id;
        }

        public String getVersionExpression() {
            return versionExpression;
        }

        @Override
        public String toString() {
            return getClass().getName() +
                    "{id=\"" + getId() +
                    "\", versionExpression=\"" + getVersionExpression() + "\"}";
        }
    }
}
