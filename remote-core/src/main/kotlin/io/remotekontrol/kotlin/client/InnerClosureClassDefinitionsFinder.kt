/*
 *  Copyright 2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.remotekontrol.kotlin.client

import java.io.File
import java.net.URISyntaxException
import java.net.URLClassLoader
import java.util.*
import java.util.zip.ZipFile

class InnerClosureClassDefinitionsFinder(classLoader: ClassLoader) {

    private val classLoader: URLClassLoader

    init {
        if (classLoader !is URLClassLoader) {
            throw IllegalArgumentException("Only URLClassLoaders are supported")
        }

        this.classLoader = classLoader
    }

    fun find(clazz: Class<*>): List<ByteArray> {
        val classes = ArrayList<ByteArray>()
        val innerClassPrefix = toInnerClassPrefix(clazz)
        val packageDirPath = toPackageDirPath(clazz)
        val innerClassPrefixWithPackage = "$packageDirPath/$innerClassPrefix"
        val ownerClassFileName = "$innerClassPrefix.class"

        for (loader in calculateEffectiveClassLoaderHierarchy()) {
            for (url in loader.urLs) {
                if (url.protocol != "file") {
                    // we only support searching the filesystem right not
                    continue
                }

                val root: File
                try {
                    root = File(url.toURI())
                } catch (e: URISyntaxException) {
                    throw RuntimeException(e)
                }

                if (!root.exists()) {
                    // may have been removed, just skip it
                    continue
                }

                if (root.isDirectory) {
                    val packageDir = if (packageDirPath != null) File(root, packageDirPath) else root
                    if (packageDir.exists()) {
                        for (classFileName in packageDir.list()!!) {
                            if (classFileName.startsWith(innerClassPrefix) && classFileName.endsWith(".class") && classFileName.length != ownerClassFileName.length) {
                                val file = File(packageDir, classFileName)
                                classes.add(file.readBytes())
                            }
                        }
                    }
                } else if (root.name.endsWith(".jar") || root.name.endsWith(".zip")) {
                    val jarFile = ZipFile(root)

                    try {
                        if (packageDirPath == null || jarFile.getEntry(packageDirPath) != null) {
                            val entries = jarFile.entries()
                            while (entries.hasMoreElements()) {
                                val entry = entries.nextElement()
                                val name = entry.name
                                if (name.startsWith(innerClassPrefixWithPackage) && name.endsWith(".class") && !name.endsWith(ownerClassFileName)) {
                                    val inputStream = jarFile.getInputStream(entry)
                                    classes.add(inputStream.readBytes())
                                }
                            }
                        }
                    } finally {
                        jarFile.close()
                    }
                }
            }
        }

        return classes
    }

    private fun calculateEffectiveClassLoaderHierarchy(): List<URLClassLoader> {
        val hierarchy = ArrayList<URLClassLoader>()
        var current: URLClassLoader? = classLoader
        while (current != null && current is URLClassLoader) {
            hierarchy.add(current)
            current = current.parent as URLClassLoader?
        }

        return hierarchy
    }

    private fun toPackageDirPath(clazz: Class<*>): String? {
        val pkg = clazz.`package`
        return if (pkg == null) "" else pkg.name.replace(".", "/")
    }

    private fun toInnerClassPrefix(clazz: Class<*>): String {
        val packageName = clazz.`package`?.name
        val fixedName = clazz.name.replace("\$_$", "\$_")
        if (packageName == null) {
            return fixedName
        } else {
            return fixedName.substring(packageName.length + 1)
        }
    }

}
