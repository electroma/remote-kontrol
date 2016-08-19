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

package io.remotecontrol

import io.remotecontrol.util.UnexpectedIOException

import java.io.IOException
import java.io.OutputStream
import java.io.Serializable
import java.util.Arrays

class CommandChain<T : Command>(val type: Class<T>, val commands: List<T>) : Serializable {

    fun writeTo(outputStream: OutputStream) {
        try {
            SerializationUtil.serialize(this, outputStream)
        } catch (e: IOException) {
            throw UnexpectedIOException("command chain should be serializable", e)
        }

    }

    companion object {

        private val serialVersionUID = 1L

        fun <T : Command> of(type: Class<T>, vararg commands: T): CommandChain<T> {
            return CommandChain(type, Arrays.asList(*commands))
        }

        fun <T : Command> of(type: Class<T>, commands: List<T>): CommandChain<T> {
            return CommandChain(type, commands)
        }
    }

}
