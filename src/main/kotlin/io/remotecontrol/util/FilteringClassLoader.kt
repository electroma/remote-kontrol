/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.remotecontrol.util

import java.util.*

class FilteringClassLoader(parent: ClassLoader, vararg blockedPackages: String) : ClassLoader(parent) {

    private val blockedPackages: Array<out String>

    init {
        this.blockedPackages = blockedPackages
    }

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*> {
        return super.loadClass(name)
    }

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        val cl: Class<*>
        try {
            cl = super.loadClass(name, false)
        } catch (e: NoClassDefFoundError) {
            if (classAllowed(name)) {
                throw e
            }
            // The class isn't visible
            throw ClassNotFoundException(String.format("%s not found.", name))
        }

        if (!allowed(cl)) {
            throw ClassNotFoundException(String.format("%s not found.", cl.name))
        }
        if (resolve) {
            resolveClass(cl)
        }

        return cl
    }

    override fun getPackage(name: String): Package? {
        val p = super.getPackage(name)
        if (p == null || !allowed(p)) {
            return null
        }
        return p
    }

    override fun getPackages(): Array<Package> {
        val packages = ArrayList<Package>()
        for (p in super.getPackages()) {
            if (allowed(p)) {
                packages.add(p)
            }
        }
        return packages.toTypedArray()
    }


    private fun allowed(pkg: Package): Boolean {
        for (packagePrefix in blockedPackages) {
            if (pkg.name.startsWith(packagePrefix)) {
                return false
            }
        }
        return true
    }

    private fun allowed(clazz: Class<*>): Boolean {
        return classAllowed(clazz.name)
    }

    private fun classAllowed(className: String): Boolean {
        for (packagePrefix in blockedPackages) {
            if (className.startsWith(packagePrefix)) {
                return false
            }
        }
        return true
    }

    private class EmptyEnumeration<T> : Enumeration<T> {
        override fun hasMoreElements(): Boolean {
            return false
        }

        override fun nextElement(): T {
            throw NoSuchElementException()
        }
    }

}
