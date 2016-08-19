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

package io.remotecontrol.result.impl

import io.remotecontrol.RemoteControlException
import io.remotecontrol.SerializationUtil
import io.remotecontrol.UnserializableExceptionException
import io.remotecontrol.result.Result
import io.remotecontrol.result.ResultFactory

import java.io.IOException
import java.io.InputStream
import java.io.NotSerializableException
import java.io.Serializable

class DefaultResultFactory : ResultFactory {

    override fun forValue(value: Any?): Result {
        if (value == null) {
            return DefaultNullResult()
        } else if (value is Serializable) {
            return forSerializable(value)
        } else {
            return forUnserializable(value)
        }
    }

    protected fun forSerializable(serializable: Serializable): Result {
        try {
            val bytes = SerializationUtil.serialize(serializable)
            return DefaultSerializedResult(bytes)
        } catch (e: NotSerializableException) {
            return forUnserializable(serializable)
        }

    }

    protected fun forUnserializable(unserializable: Any): Result {
        return DefaultUnserializableResult(unserializable.toString())
    }

    override fun forThrown(throwable: Throwable): Result {
        try {
            val bytes = SerializationUtil.serialize(throwable)
            return DefaultThrownResult(bytes)
        } catch (e: NotSerializableException) {
            try {
                return DefaultUnserializableThrownResult(throwable.toString(), SerializationUtil.serialize(UnserializableExceptionException(throwable)))
            } catch (e1: NotSerializableException) {
                throw IllegalStateException("Can't serialize NotSerializableException", e1)
            }

        }

    }

    @Throws(IOException::class)
    override fun deserialize(inputStream: InputStream, classLoader: ClassLoader): Result {
        try {
            return SerializationUtil.deserialize(Result::class.java, inputStream, classLoader)
        } catch (e: ClassNotFoundException) {
            throw RemoteControlException.classNotFoundOnClient(e)
        }

    }

}
