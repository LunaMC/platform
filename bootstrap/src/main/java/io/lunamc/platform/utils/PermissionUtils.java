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

package io.lunamc.platform.utils;

import io.lunamc.platform.security.SecurityInitializationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;

public class PermissionUtils {

    private PermissionUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a utility class and should not be constructed");
    }

    public static Permission createPermission(ClassLoader classLoader, String permissionClassName, String name, String actions) {
        if (name == null || actions == null)
            throw new SecurityInitializationException("name or actions not set");

        Class<?> permissionClass;
        try {
            permissionClass = classLoader.loadClass(permissionClassName);
        } catch (ClassNotFoundException e) {
            throw new SecurityInitializationException("Permission class " + permissionClassName + " not found", e);
        }
        if (!Permission.class.isAssignableFrom(permissionClass))
            throw new SecurityInitializationException("Permission class " + permissionClassName + " must declare " + Permission.class.getName() + " as a superclass");

        Constructor<?> constructor;
        try {
            constructor = permissionClass.getConstructor(String.class, String.class);
        } catch (ReflectiveOperationException e) {
            throw new SecurityInitializationException("Permission class " + permissionClassName + " must declare a constructor #" + permissionClass.getSimpleName() + "(java.lang.String, java.lang.String)", e);
        }

        name = SystemPropertyUtils.replacePlaceholders(name);
        if (name.isEmpty())
            return null;

        try {
            return Permission.class.cast(constructor.newInstance(name, actions));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new SecurityInitializationException("Permission class " + permissionClassName + " cannot initialized", e);
        }
    }
}
