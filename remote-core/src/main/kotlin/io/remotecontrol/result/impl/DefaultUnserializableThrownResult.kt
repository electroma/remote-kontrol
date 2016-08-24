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
import io.remotecontrol.result.UnserializableThrownResult

class DefaultUnserializableThrownResult(stringRepresentation: String, private val notSerializableException: ByteArray) : DefaultUnserializableResult(stringRepresentation), UnserializableThrownResult {

    override fun deserializeWrapper(classLoader: ClassLoader): UnserializableExceptionException {
        try {
            return SerializationUtil.deserialize(UnserializableExceptionException::class.java, notSerializableException, classLoader)
        } catch (e: ClassNotFoundException) {
            throw RemoteControlException.classNotFoundOnClient(e)
        }

    }

}
