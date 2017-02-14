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

import io.lunamc.platform.security.LunaSecurityManager;
import io.lunamc.platform.security.LunaSecurityPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Policy;
import java.util.Properties;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        throw new UnsupportedOperationException("Application entry point should not be constructed");
    }

    public static void main(String[] args) {
        loadSystemProperties();
        setupSecurityManager();

        LunaPlatform instance = new LunaPlatform();
        Runtime.getRuntime().addShutdownHook(new Thread(instance::safeStop, "shutdown-thread"));
        instance.start();
        instance.waitForStop();
    }

    private static void loadSystemProperties() {
        File file = new File(System.getProperty("io.lunamc.platform.propertiesFile", "environment.properties"));
        if (file.isFile()) {
            Properties properties = new Properties(System.getProperties());
            try (InputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                LOGGER.error("Loading system properties file failed.", e);
            }
            System.setProperties(properties);
        }
    }

    private static void setupSecurityManager() {
        SecurityManager currentSecurityManager = System.getSecurityManager();
        if (currentSecurityManager == null) {
            boolean disableAdvanceSecurity = Boolean.getBoolean("io.lunamc.platform.disableAdvanceSecurity");
            if (disableAdvanceSecurity)
                LOGGER.warn("Luna security manager was disabled explicitly by command line argument -Dio.lunamc.platform.disableAdvanceSecurity=true");
            Policy.setPolicy(new LunaSecurityPolicy());
            System.setSecurityManager(new LunaSecurityManager(disableAdvanceSecurity));
        } else {
            LOGGER.warn("Luna security manager cannot be installed since another security manager (" +
                    currentSecurityManager + " class: " + currentSecurityManager.getClass().getName() + ") is installed already");
        }
    }
}
