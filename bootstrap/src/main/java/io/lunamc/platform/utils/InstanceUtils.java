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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class InstanceUtils {

    private InstanceUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a utility class and should not be constructed");
    }

    @SuppressWarnings("unchecked")
    public static <T> T createInstance(String implementation, Class<T> requiredClass) {
        Class<?> aClass;
        try {
            aClass = Class.forName(implementation);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(implementation);
        }
        return createInstance(aClass, requiredClass);
    }

    public static <T> T createInstance(Class<?> aClass, Class<T> castClass) {
        if (Modifier.isAbstract(aClass.getModifiers()))
            throw new InstantiationError(aClass.getName() + " must be non-abstract");
        else if (aClass.isInterface())
            throw new InstantiationError(aClass.getName() + " must be not an interface");
        else if (aClass.isEnum())
            throw new InstantiationError(aClass.getName() + " must be not an enum");
        else if (!Modifier.isPublic(aClass.getModifiers()))
            throw new InstantiationError(aClass.getName() + " must be public");
        else if (!castClass.isAssignableFrom(aClass))
            throw new InstantiationError(aClass.getName() + " must inherit from " + castClass.getName());
        Constructor<?> constructor;
        try {
            constructor = aClass.getConstructor();
        } catch (NoSuchMethodException ignore) {
            throw new InstantiationError(aClass.getName() + "#" + aClass.getSimpleName() + "() must be declared");
        }
        if (!Modifier.isPublic(constructor.getModifiers()))
            throw new InstantiationError(aClass.getName() + "#" + aClass.getSimpleName() + "() must be public");
        try {
            return castClass.cast(constructor.newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InstantiationError(aClass.getName() + " cannot initialized: " + e);
        }
    }
}
