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

package io.lunamc.platform.internal.plugin.annotation.processor;

import com.github.zafarkhaja.semver.Version;
import io.lunamc.platform.plugin.PluginDescriptor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement(namespace = "http://lunamc.io/provided-plugin/1.0", name = "providedPlugins")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvidedPlugins {

    @XmlElement(namespace = "http://lunamc.io/provided-plugin/1.0", name = "providedPlugin")
    private List<ProvidedPlugin> providedPlugins;

    public List<ProvidedPlugin> getProvidedPlugins() {
        return providedPlugins;
    }

    public void setProvidedPlugins(List<ProvidedPlugin> providedPlugins) {
        this.providedPlugins = providedPlugins;
    }

    public Optional<ProvidedPlugin> getProvidedPlugin(String id) {
        Objects.requireNonNull(id, "id must not be null");
        for (ProvidedPlugin providedPlugin : getProvidedPlugins()) {
            if (id.equals(providedPlugin.getId()))
                return Optional.of(providedPlugin);
        }
        return Optional.empty();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProvidedPlugin {

        @XmlAttribute(name = "id")
        private String id;

        @XmlAttribute(name = "version")
        private String version;

        @XmlAttribute(name = "impl")
        private String impl;

        @XmlElementWrapper(namespace = "http://lunamc.io/provided-plugin/1.0", name = "pluginDependencies")
        @XmlElement(namespace = "http://lunamc.io/provided-plugin/1.0", name = "pluginDependency")
        private List<PluginDependency> pluginDependencies;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getImpl() {
            return impl;
        }

        public void setImpl(String impl) {
            this.impl = impl;
        }

        public List<PluginDependency> getPluginDependencies() {
            return pluginDependencies;
        }

        public void setPluginDependencies(List<PluginDependency> pluginDependencies) {
            this.pluginDependencies = pluginDependencies;
        }

        public PluginDescriptor toPluginDescriptor() {
            List<PluginDependency> pluginDependencies = getPluginDependencies();
            return new PluginDescriptor(
                    getId(),
                    Version.valueOf(getVersion()),
                    (pluginDependencies != null ? pluginDependencies.stream() : Stream.<PluginDependency>empty())
                            .map(PluginDependency::toPluginDependency)
                            .collect(Collectors.toList())
            );
        }

        @Override
        public String toString() {
            return getClass().getName() +
                    "{id=\"" + getId() +
                    "\", version=\"" + getVersion() +
                    "\", impl=\"" + getImpl() +
                    "\", pluginDependencies=" + getPluginDependencies() + '}';
        }

        public static ProvidedPlugin create(String id, String version, String impl, List<PluginDependency> pluginDependencies) {
            ProvidedPlugin providedPlugin = new ProvidedPlugin();
            providedPlugin.setId(id);
            providedPlugin.setVersion(version);
            providedPlugin.setImpl(impl);
            providedPlugin.setPluginDependencies(pluginDependencies);
            return providedPlugin;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PluginDependency {

        @XmlAttribute(name = "id")
        private String id;

        @XmlAttribute(name = "versionExpression")
        private String versionExpression;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVersionExpression() {
            return versionExpression;
        }

        public void setVersionExpression(String versionExpression) {
            this.versionExpression = versionExpression;
        }

        public PluginDescriptor.PluginDependency toPluginDependency() {
            return new PluginDescriptor.PluginDependency(getId(), getVersionExpression());
        }

        @Override
        public String toString() {
            return getClass().getName() +
                    "{id=\"" + getId() +
                    ", versionExpression=\"" + getVersionExpression() + "\"}";
        }

        public static PluginDependency create(String id, String versionExpression) {
            PluginDependency dependency = new PluginDependency();
            dependency.setId(id);
            dependency.setVersionExpression(versionExpression);
            return dependency;
        }
    }
}
