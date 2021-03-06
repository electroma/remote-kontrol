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

package io.remotekontrol.client

import io.remotekontrol.Command
import io.remotekontrol.CommandChain
import io.remotekontrol.result.*

class RemoteKontrolSupport<T : Command>(private val transport: Transport,
                                        private val unserializableResultStrategy: UnserializableResultStrategy,
                                        private val classLoader: ClassLoader) {

    fun send(commandChain: CommandChain<T>): Any? {
        val result = sendCommandChain(commandChain)
        return processResult(result)
    }

    private fun sendCommandChain(commandChain: CommandChain<*>): Result {
        return transport.send(commandChain)
    }

    private fun processResult(result: Result): Any? {

        if (result is NullResult) {
            return null
        } else if (result is ThrownResult) {
            throw RemoteException(result.deserialize(classLoader))
        } else if (result is UnserializableThrownResult) {
            throw RemoteException(result.deserializeWrapper(classLoader))
        } else if (result is UnserializableResult) {
            if (unserializableResultStrategy == UnserializableResultStrategy.NULL) {
                return null
            } else if (unserializableResultStrategy == UnserializableResultStrategy.STRING) {
                return result.stringRepresentation
            } else if (unserializableResultStrategy == UnserializableResultStrategy.THROW) {
                throw UnserializableReturnException(result)
            } else {
                throw IllegalStateException("Unhandled UnserializableResultStrategy: $unserializableResultStrategy")
            }
        } else if (result is SerializedResult) {
            return result.deserialize(classLoader)
        } else {
            throw IllegalArgumentException("Unknown result type: $result")
        }
    }

}
