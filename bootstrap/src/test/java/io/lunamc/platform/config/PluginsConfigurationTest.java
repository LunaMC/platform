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

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class PluginsConfigurationTest {

    @Test
    public void testLoad() throws Throwable {
        PluginsConfiguration configuration;
        try (InputStream inputStream = getClass().getResourceAsStream("/example-plugins.xml")) {
            configuration = PluginsConfiguration.load(inputStream);
        }
        Assert.assertEquals(2, configuration.getPlugins().size());

        PluginsConfiguration.PluginConfiguration plugin = configuration.getPlugins().get(0);
        Assert.assertEquals("test-1", plugin.getId());
        Assert.assertEquals("test1.jar", plugin.getFile());
        PluginsConfiguration.PluginSecurity security = plugin.getSecurity();
        Assert.assertEquals(2, security.getPermissions().size());
        PluginsConfiguration.PluginPermission permission = security.getPermissions().get(0);
        Assert.assertEquals("TestPermission1", permission.getImpl());
        Assert.assertEquals("access", permission.getAction());
        Assert.assertEquals("hello world", permission.getName());
        permission = security.getPermissions().get(1);
        Assert.assertEquals("TestPermission2", permission.getImpl());
        Assert.assertEquals("read", permission.getAction());
        Assert.assertEquals("*", permission.getName());

        plugin = configuration.getPlugins().get(1);
        Assert.assertEquals("test-2", plugin.getId());
        Assert.assertEquals("test2.jar", plugin.getFile());
    }
}
