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

package io.lunamc.platform.plugin;

import java.security.Permission;
import java.util.Objects;

public class PluginManagerPermission extends Permission {

    private static final String NAME = "plugin-manager";
    private static final int MASK_READ = 0x01;
    private static final int MASK_REGISTER = 0x02;
    private static final int MASK_MANAGE = 0x04;
    public static final PluginManagerPermission PERMISSION_READ = new PluginManagerPermission(MASK_READ);
    public static final PluginManagerPermission PERMISSION_REGISTER = new PluginManagerPermission(MASK_REGISTER);
    public static final PluginManagerPermission PERMISSION_MANAGE = new PluginManagerPermission(MASK_MANAGE);

    private final int mask;

    @SuppressWarnings("unused")
    public PluginManagerPermission(String ignore, String action) {
        this(getMask(action));
    }

    private PluginManagerPermission(int mask) {
        super(NAME);

        this.mask = mask;
    }

    @Override
    public boolean implies(Permission permission) {
        if (!(permission instanceof PluginManagerPermission))
            return false;
        if (permission == this)
            return true;
        PluginManagerPermission castedPermission = (PluginManagerPermission) permission;
        return (mask & castedPermission.mask) == castedPermission.mask;
    }

    @Override
    public String getActions() {
        StringBuilder sb = new StringBuilder();
        boolean comma = false;

        if ((mask & MASK_READ) == MASK_READ) {
            comma = true;
            sb.append("read");
        }

        if ((mask & MASK_REGISTER) == MASK_REGISTER) {
            if (comma)
                sb.append(',');
            else
                comma = true;
            sb.append("register");
        }

        if ((mask & MASK_MANAGE) == MASK_MANAGE) {
            if (comma)
                sb.append(',');
            sb.append("manage");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PluginManagerPermission))
            return false;
        PluginManagerPermission that = (PluginManagerPermission) o;
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
                case "read":
                    mask |= MASK_READ;
                    break;
                case "register":
                    mask |= MASK_REGISTER;
                    break;
                case "manage":
                    mask |= MASK_MANAGE;
                    break;
                default:
                    throw new IllegalArgumentException("invalid permission: " + action);
            }
        }
        return mask;
    }
}
