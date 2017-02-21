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

import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.platform.service.di.PreferredConstructor;

import java.lang.reflect.Constructor;

public class ServiceRegistryUtils {

    private ServiceRegistryUtils() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is a utility class and should not be constructed");
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findAppropriateInstantiableConstructor(Class<T> aClass) {
        Constructor<?>[] constructors = aClass.getConstructors();
        Constructor<?> candidate = null;

        // Check for @PreferredConstructor annotation
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(PreferredConstructor.class)) {
                candidate = constructor;
                break;
            }
        }
        if (candidate != null) {
            if (candidate.getParameterTypes().length > 0 && !hasOnlyServiceConstructorParameters(candidate))
                throw new UnsupportedOperationException("Constructor " + candidate + " is marked by @PreferredConstructor but declares invalid parameters");
            return (Constructor<T>) candidate;
        }

        // Search constructor with most ServiceRegistration<?> parameters
        for (Constructor<?> constructor : constructors) {
            if (!hasOnlyServiceConstructorParameters(constructor))
                continue;
            if (candidate == null || constructor.getParameterTypes().length > candidate.getParameterTypes().length)
                candidate = constructor;
        }
        if (candidate != null)
            return (Constructor<T>) candidate;

        // Search no-args constructor
        try {
            return aClass.getConstructor();
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    private static boolean hasOnlyServiceConstructorParameters(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length < 1)
            return false;
        for (Class<?> parameterType : parameterTypes) {
            if (!ServiceRegistration.class.isAssignableFrom(parameterType))
                return false;
        }
        return true;
    }
}
