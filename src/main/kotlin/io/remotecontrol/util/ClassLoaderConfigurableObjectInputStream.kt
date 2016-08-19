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

import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass

/**
 * Allows us to hydrate objects with a custom classloader.
 */
class ClassLoaderConfigurableObjectInputStream @Throws(IOException::class)
constructor(val classLoader: ClassLoader, input: InputStream) : ObjectInputStream(input) {

    @Throws(IOException::class, ClassNotFoundException::class)
    public override fun resolveClass(desc: ObjectStreamClass): Class<*> {
        try {
            return classLoader.loadClass(desc.name)
        } catch (e: ClassNotFoundException) {
            return super.resolveClass(desc)
        }

    }

}