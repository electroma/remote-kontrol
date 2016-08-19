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

package io.remotecontrol.server

import io.remotecontrol.Command
import io.remotecontrol.CommandChain
import io.remotecontrol.RemoteControlException
import io.remotecontrol.SerializationUtil
import io.remotecontrol.result.Result
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MultiTypeReceiver(private val classLoader: ClassLoader, vararg runners: CommandRunner<*>) : Receiver {
    private val runners: List<CommandRunner<*>>

    init {
        this.runners = Arrays.asList(*runners)
    }

    @Throws(IOException::class)
    override fun execute(commandStream: InputStream, resultStream: OutputStream) {
        val commandChain: CommandChain<*>
        try {
            commandChain = SerializationUtil.deserialize(CommandChain::class.java, commandStream, classLoader)
        } catch (e: ClassNotFoundException) {
            throw RemoteControlException.classNotFoundOnServer(e)
        }

        for (runner in runners) {
            val result = maybeInvoke(commandChain, runner)
            if (result != null) {
                SerializationUtil.serialize(result, resultStream)
                return
            }
        }

        throw RemoteControlException("Cannot handle commands of type:" + commandChain.type.name)
    }

    private fun <T : Command> maybeInvoke(commandChain: CommandChain<T>, runner: CommandRunner<*>): Result? {
        if (commandChain.type.isAssignableFrom(runner.type)) {
            @SuppressWarnings("unchecked")
            val cast = runner as CommandRunner<T>
            return cast.run(commandChain)
        } else {
            return null
        }
    }

}
