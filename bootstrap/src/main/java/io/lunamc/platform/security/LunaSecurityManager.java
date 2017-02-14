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

package io.lunamc.platform.security;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

public class LunaSecurityManager extends SecurityManager {

    private final boolean skip;

    public LunaSecurityManager(boolean skip) {
        this.skip = skip;
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (!skip)
            super.checkPropertyAccess(key);
    }

    @Override
    public void checkPermission(Permission perm) {
        if (perm instanceof RuntimePermission && "setSecurityManager".equals(perm.getName()))
            throw new SecurityException("Security manager cannot be replaced");
        if (!skip)
            super.checkPermission(perm);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (!skip)
            super.checkPermission(perm, context);
    }

    @Override
    public void checkCreateClassLoader() {
        if (!skip)
            super.checkCreateClassLoader();
    }

    @Override
    public void checkAccess(Thread t) {
        if (!skip)
            super.checkAccess(t);
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (!skip)
            super.checkAccess(g);
    }

    @Override
    public void checkExit(int status) {
        if (!skip)
            super.checkExit(status);
    }

    @Override
    public void checkExec(String cmd) {
        if (!skip)
            super.checkExec(cmd);
    }

    @Override
    public void checkLink(String lib) {
        if (!skip)
            super.checkLink(lib);
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if (!skip)
            super.checkRead(fd);
    }

    @Override
    public void checkRead(String file) {
        if (!skip)
            super.checkRead(file);
    }

    @Override
    public void checkRead(String file, Object context) {
        if (!skip)
            super.checkRead(file, context);
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (!skip)
            super.checkWrite(fd);
    }

    @Override
    public void checkWrite(String file) {
        if (!skip)
            super.checkWrite(file);
    }

    @Override
    public void checkDelete(String file) {
        if (!skip)
            super.checkDelete(file);
    }

    @Override
    public void checkConnect(String host, int port) {
        if (!skip)
            super.checkConnect(host, port);
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (!skip)
            super.checkConnect(host, port, context);
    }

    @Override
    public void checkListen(int port) {
        if (!skip)
            super.checkListen(port);
    }

    @Override
    public void checkAccept(String host, int port) {
        if (!skip)
            super.checkAccept(host, port);
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (!skip)
            super.checkMulticast(maddr);
    }

    @Override
    public void checkPropertiesAccess() {
        if (!skip)
            super.checkPropertiesAccess();
    }

    @Override
    public void checkPrintJobAccess() {
        if (!skip)
            super.checkPrintJobAccess();
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (!skip)
            super.checkPackageAccess(pkg);
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        if (!skip)
            super.checkPackageDefinition(pkg);
    }

    @Override
    public void checkSetFactory() {
        if (!skip)
            super.checkSetFactory();
    }

    @Override
    public void checkSecurityAccess(String target) {
        if (!skip)
            super.checkSecurityAccess(target);
    }
}
