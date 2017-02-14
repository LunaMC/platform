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

import org.junit.Assert;
import org.junit.Test;

public class InstanceUtilsTest {

    @Test
    public void testCreateInstanceFromClassName() {
        Assert.assertNotNull(InstanceUtils.createInstance(DemoClass1.class.getName(), Runnable.class));
    }

    @Test(expected = InstantiationError.class)
    public void testCreateWrongSuperclassInstanceFromClassName() {
        InstanceUtils.createInstance(DemoClass2.class.getName(), Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateWrongConstructorInstanceFromClassName() {
        InstanceUtils.createInstance(DemoClass3.class.getName(), Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreatePrivateConstructorInstanceFromClassName() {
        InstanceUtils.createInstance(DemoClass4.class.getName(), Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateInterfaceInstanceFromClassName() {
        InstanceUtils.createInstance(DemoClass5.class.getName(), Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateAbstractInstanceFromClassName() {
        InstanceUtils.createInstance(DemoClass6.class.getName(), Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateEnumInstanceFromClassName() {
        InstanceUtils.createInstance(DemoClass7.class.getName(), Runnable.class);
    }

    @Test
    public void testCreateInstanceFromClass() {
        Assert.assertNotNull(InstanceUtils.createInstance(DemoClass1.class, Runnable.class));
    }

    @Test(expected = InstantiationError.class)
    public void testCreateWrongSuperclassInstanceFromClass() {
        InstanceUtils.createInstance(DemoClass2.class, Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateWrongConstructorInstanceFromClass() {
        InstanceUtils.createInstance(DemoClass3.class, Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreatePrivateConstructorInstanceFromClass() {
        InstanceUtils.createInstance(DemoClass4.class, Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateInterfaceInstanceFromClass() {
        InstanceUtils.createInstance(DemoClass5.class, Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateAbstractInstanceFromClass() {
        InstanceUtils.createInstance(DemoClass6.class, Runnable.class);
    }

    @Test(expected = InstantiationError.class)
    public void testCreateEnumInstanceFromClass() {
        InstanceUtils.createInstance(DemoClass7.class, Runnable.class);
    }

    public static class DemoClass1 implements Runnable {

        @Override
        public void run() {
        }
    }

    public static class DemoClass2 {
    }

    public static class DemoClass3 implements Runnable {

        public DemoClass3(String ignore) {
        }

        @Override
        public void run() {
        }
    }

    public static class DemoClass4 implements Runnable {

        private DemoClass4() {
        }

        @Override
        public void run() {
        }
    }

    public interface DemoClass5 extends Runnable {
    }

    public abstract class DemoClass6 implements Runnable {
    }

    public enum DemoClass7 implements Runnable {

        TEST;

        @Override
        public void run() {
        }
    }
}
