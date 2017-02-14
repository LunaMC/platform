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

package io.lunamc.platform.config;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.List;

@XmlRootElement(namespace = "http://lunamc.io/plugin/1.0", name = "plugins")
@XmlAccessorType(XmlAccessType.FIELD)
public class PluginsConfiguration {

    @XmlElement(namespace = "http://lunamc.io/plugin/1.0", name = "plugin")
    private List<PluginConfiguration> plugins;

    public List<PluginConfiguration> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<PluginConfiguration> plugins) {
        this.plugins = plugins;
    }

    public static PluginsConfiguration load(InputStream input) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PluginsConfiguration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (PluginsConfiguration) unmarshaller.unmarshal(input);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PluginConfiguration {

        @XmlAttribute(name = "file")
        private String file;

        @XmlAttribute(name = "id")
        private String id;

        @XmlElement(namespace = "http://lunamc.io/plugin/1.0", name = "security")
        private PluginSecurity security;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public PluginSecurity getSecurity() {
            return security;
        }

        public void setSecurity(PluginSecurity security) {
            this.security = security;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PluginSecurity {

        @XmlElementWrapper(namespace = "http://lunamc.io/plugin/1.0", name = "permissions")
        @XmlElement(namespace = "http://lunamc.io/plugin/1.0", name = "permission")
        private List<PluginPermission> permissions;

        public List<PluginPermission> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<PluginPermission> permissions) {
            this.permissions = permissions;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PluginPermission {

        @XmlAttribute(name = "impl")
        private String impl;

        @XmlAttribute(name = "name")
        private String name;

        @XmlAttribute(name = "action")
        private String action;

        public String getImpl() {
            return impl;
        }

        public void setImpl(String impl) {
            this.impl = impl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
