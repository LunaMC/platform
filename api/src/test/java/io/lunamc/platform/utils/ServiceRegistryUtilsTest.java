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
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;

public class ServiceRegistryUtilsTest {

    @Test
    public void testFindAppropriateInstantiableConstructorByAnnotation() throws Throwable {
        Constructor<TestClass1> constructor = ServiceRegistryUtils.findAppropriateInstantiableConstructor(TestClass1.class);
        Assert.assertEquals(TestClass1.class.getConstructor(ServiceRegistration.class), constructor);
    }

    @Test
    public void testFindAppropriateInstantiableConstructorByMostArgs() throws Throwable {
        Constructor<TestClass2> constructor = ServiceRegistryUtils.findAppropriateInstantiableConstructor(TestClass2.class);
        Assert.assertEquals(TestClass2.class.getConstructor(ServiceRegistration.class, ServiceRegistration.class), constructor);
    }

    @Test
    public void testFindAppropriateInstantiableConstructorByNoArgs() throws Throwable {
        Constructor<TestClass3> constructor = ServiceRegistryUtils.findAppropriateInstantiableConstructor(TestClass3.class);
        Assert.assertEquals(TestClass3.class.getConstructor(), constructor);
    }

    @Test
    public void testFindAppropriateInstantiableConstructorByImplicitNoArgs() throws Throwable {
        Constructor<TestClass4> constructor = ServiceRegistryUtils.findAppropriateInstantiableConstructor(TestClass4.class);
        Assert.assertEquals(TestClass4.class.getConstructor(), constructor);
    }

    @Test
    public void testFindAppropriateInstantiableConstructorNoCandidate() throws Throwable {
        Assert.assertNull(ServiceRegistryUtils.findAppropriateInstantiableConstructor(TestClass5.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFindAppropriateInstantiableConstructorInvalidAnnotation() throws Throwable {
        ServiceRegistryUtils.findAppropriateInstantiableConstructor(TestClass6.class);
    }

    interface TestService1 {
    }

    interface TestService2 {
    }

    // For testFindAppropriateInstantiableConstructorByAnnotation
    public static class TestClass1 {

        public TestClass1() {
        }

        @PreferredConstructor
        public TestClass1(ServiceRegistration<TestService1> testService1) {
        }

        public TestClass1(ServiceRegistration<TestService1> testService1, ServiceRegistration<TestService2> testService2) {
        }

        public TestClass1(String str) {
        }
    }

    // For testFindAppropriateInstantiableConstructorByMostArgs
    public static class TestClass2 {

        public TestClass2() {
        }

        public TestClass2(ServiceRegistration<TestService1> testService1) {
        }

        public TestClass2(ServiceRegistration<TestService1> testService1, ServiceRegistration<TestService2> testService2) {
        }

        public TestClass2(String str) {
        }

        public TestClass2(String str1, String str2, String str3) {
        }
    }

    // For testFindAppropriateInstantiableConstructorByNoArgs
    public static class TestClass3 {

        public TestClass3() {
        }

        public TestClass3(String str) {
        }
    }

    // For testFindAppropriateInstantiableConstructorByImplicitNoArgs
    public static class TestClass4 {
    }

    // For testFindAppropriateInstantiableConstructorNoCandidate
    public static class TestClass5 {

        public TestClass5(String str) {
        }
    }

    // For testFindAppropriateInstantiableConstructorInvalidAnnotation
    public static class TestClass6 {

        public TestClass6(ServiceRegistration<TestService1> testService1) {
        }

        @PreferredConstructor
        public TestClass6(String str) {
        }
    }
}
