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

package io.remotekontrol.server

import io.remotekontrol.CommandChain
import io.remotekontrol.RemoteKontrolException
import io.remotekontrol.SerializationUtil
import io.remotekontrol.SerializationUtil.deserialize
import io.remotekontrol.kotlin.ClosureCommand
import io.remotekontrol.result.Result
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MultiTypeReceiver(private val classLoader: ClassLoader, vararg runners: CommandRunner<ClosureCommand>) : Receiver {
    private val runners: List<CommandRunner<ClosureCommand>>

    init {
        this.runners = Arrays.asList(*runners)
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(commandStream: InputStream, resultStream: OutputStream) {
        val commandChain: CommandChain<ClosureCommand>
        try {
            commandChain = deserialize(CommandChain::class.java, commandStream, classLoader) as CommandChain<ClosureCommand>
        } catch (e: ClassNotFoundException) {
            throw RemoteKontrolException.classNotFoundOnServer(e)
        }

        for (runner in runners) {
            val result = maybeInvoke(commandChain, runner)
            if (result != null) {
                SerializationUtil.serialize(result, resultStream)
                return
            }
        }

        throw RemoteKontrolException("Cannot handle commands of type: ${commandChain.type.name}")
    }

    private fun maybeInvoke(commandChain: CommandChain<ClosureCommand>, runner: CommandRunner<ClosureCommand>): Result? {
        if (commandChain.type.isAssignableFrom(runner.type)) {
            return runner.run(commandChain)
        } else {
            return null
        }
    }

}
