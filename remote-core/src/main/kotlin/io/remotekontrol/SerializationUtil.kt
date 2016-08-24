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

package io.remotekontrol

import io.remotekontrol.util.ClassLoaderConfigurableObjectInputStream
import io.remotekontrol.util.UnexpectedIOException
import java.io.*
import java.lang.reflect.InvocationTargetException

object SerializationUtil {

    @Throws(NotSerializableException::class)
    fun serialize(serializable: Serializable): ByteArray {
        val baos = ByteArrayOutputStream()
        try {
            serialize(serializable, baos)
        } catch (e: IOException) {
            if (e is NotSerializableException) {
                throw e
            }
            throw UnexpectedIOException("Unexpected exception while serializing object: $serializable", e)
        }

        return baos.toByteArray()
    }

    @Throws(IOException::class)
    fun serialize(serializable: Serializable, outputStream: OutputStream) {
        ObjectOutputStream(outputStream).use {
            it.writeObject(serializable)
        }

    }

    @Throws(ClassNotFoundException::class)
    fun <T> deserialize(type: Class<T>, bytes: ByteArray, classLoader: ClassLoader): T {
        try {
            return deserialize(type, ByteArrayInputStream(bytes), classLoader)
        } catch (e: IOException) {
            throw UnexpectedIOException("Unexpected exception while deserializing in memory", e)
        }

    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> deserialize(type: Class<T>, inputStream: InputStream, classLoader: ClassLoader): T {
        val o = ClassLoaderConfigurableObjectInputStream(classLoader, inputStream).readObject()
        if (type.isInstance(o)) {
            return type.cast(o)
        } else {
            throw IllegalArgumentException("Expecting to deserialize instance of ${type.name} but got ${o.javaClass.name}: $o")
        }
    }

    fun defineClass(classLoader: ClassLoader, bytes: ByteArray): Class<*> {
        try {
            val defineClass = ClassLoader::class.java
                    .getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Integer.TYPE, Integer.TYPE)
            defineClass.isAccessible = true
            val clazz = defineClass.invoke(classLoader, null, bytes, 0, bytes.size)
            return clazz as Class<*>
        } catch (e: NoSuchMethodException) {
            throw IllegalStateException(e)
        } catch (e: InvocationTargetException) {
            throw IllegalStateException(e)
        } catch (e: IllegalAccessException) {
            throw IllegalStateException(e)
        }

    }
}
