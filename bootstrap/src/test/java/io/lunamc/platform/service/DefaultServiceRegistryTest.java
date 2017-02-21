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

package io.lunamc.platform.service;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Objects;

public class DefaultServiceRegistryTest {

    @Test
    public void testInstantiateConstructor() throws Throwable {
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        DemoClass demoClass = serviceRegistry.instantiate(DemoClass.class.getConstructor(ServiceRegistration.class));
        Assert.assertNotNull(demoClass);

        DemoService demoService = Mockito.mock(DemoService.class);
        serviceRegistry.setService(DemoService.class, demoService);
        serviceRegistry.setService(AnotherDemoService.class, Mockito.mock(AnotherDemoService.class));

        demoClass.test();
        Mockito.verify(demoService, Mockito.times(1)).test();
        Mockito.validateMockitoUsage();
    }

    @Test
    public void testInstantiateClass() throws Throwable {
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        DemoClass demoClass = serviceRegistry.instantiate(DemoClass.class);
        Assert.assertNotNull(demoClass);

        DemoService demoService = Mockito.mock(DemoService.class);
        serviceRegistry.setService(DemoService.class, demoService);
        serviceRegistry.setService(AnotherDemoService.class, Mockito.mock(AnotherDemoService.class));

        demoClass.test();
        Mockito.verify(demoService, Mockito.times(1)).test();
        Mockito.validateMockitoUsage();
    }

    private interface DemoService {

        void test();
    }

    private interface AnotherDemoService {
    }

    private static class DemoClass {

        private final ServiceRegistration<DemoService> demoService;

        public DemoClass(ServiceRegistration<DemoService> demoService) {
            this.demoService = Objects.requireNonNull(demoService, "demoService must not be null");
        }

        public void test() {
            demoService.requireInstance().test();
        }
    }
}
