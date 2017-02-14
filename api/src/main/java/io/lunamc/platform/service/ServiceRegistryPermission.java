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

import java.security.Permission;
import java.util.Objects;

public class ServiceRegistryPermission extends Permission {

    private static final String NAME = "service-registry";
    private static final int MASK_ACCESS = 0x01;
    private static final int MASK_START_OR_STOP = 0x02;
    public static final ServiceRegistryPermission PERMISSION_ACCESS = new ServiceRegistryPermission(MASK_ACCESS);
    public static final ServiceRegistryPermission PERMISSION_START_OR_STOP = new ServiceRegistryPermission(MASK_START_OR_STOP);

    private final int mask;

    public ServiceRegistryPermission(String action) {
        this(NAME, action);
    }

    @SuppressWarnings("unused")
    public ServiceRegistryPermission(String ignore, String action) {
        this(getMask(action));
    }

    private ServiceRegistryPermission(int mask) {
        super(NAME);

        this.mask = mask;
    }

    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof ServiceRegistryPermission))
            return false;
        if (permission == this)
            return true;
        ServiceRegistryPermission castedPermission = (ServiceRegistryPermission) permission;
        return (mask & castedPermission.mask) == castedPermission.mask;
    }

    @Override
    public String getActions() {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;

        if ((mask & MASK_ACCESS) == MASK_ACCESS) {
            comma = true;
            sb.append("access");
        }

        if ((mask & MASK_START_OR_STOP) == MASK_START_OR_STOP) {
            if (comma)
                sb.append(',');
            sb.append("startOrStop");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServiceRegistryPermission))
            return false;
        ServiceRegistryPermission that = (ServiceRegistryPermission) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getActions(), that.getActions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getActions());
    }

    private static int getMask(String action) {
        Objects.requireNonNull(action, "action must not be null");
        if (action.isEmpty())
            throw new IllegalArgumentException("action must not be empty");

        int mask = 0;
        for (String singleAction : action.split(",")) {
            switch (singleAction) {
                case "access":
                    mask |= MASK_ACCESS;
                    break;
                case "startOrStop":
                    mask |= MASK_START_OR_STOP;
                    break;
                default:
                    throw new IllegalArgumentException("invalid permission: " + action);
            }
        }
        return mask;
    }
}
