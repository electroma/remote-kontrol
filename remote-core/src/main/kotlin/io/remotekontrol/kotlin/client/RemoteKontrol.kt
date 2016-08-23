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

package io.remotekontrol.kotlin.client

import io.remotekontrol.CommandChain
import io.remotekontrol.client.CommandGenerator
import io.remotekontrol.client.RemoteKontrolSupport
import io.remotekontrol.client.Transport
import io.remotekontrol.client.UnserializableResultStrategy
import io.remotekontrol.kotlin.ClosureCommand
import java.io.IOException
import java.util.*
import kotlin.jvm.internal.Lambda

class RemoteKontrol constructor(transport: Transport, unserializableResultStrategy: UnserializableResultStrategy, classLoader: ClassLoader) {

    private val commandGenerator: CommandGenerator<RawClosureCommand, ClosureCommand>
    private val support: RemoteKontrolSupport<ClosureCommand>

    constructor(transport: Transport) : this(transport, UnserializableResultStrategy.THROW, Thread.currentThread().contextClassLoader) {
    }

    constructor(transport: Transport, classLoader: ClassLoader) : this(transport, UnserializableResultStrategy.THROW, classLoader) {
    }

    init {
        this.support = RemoteKontrolSupport<ClosureCommand>(transport, unserializableResultStrategy, classLoader)
        this.commandGenerator = ClosureCommandGenerator(classLoader)
    }

    @Throws(IOException::class)
    fun exec(commands: Array<out () -> Any?>): Any? {
        return exec(LinkedHashMap<String, Any>(), commands)
    }

    @Throws(IOException::class)
    fun exec(params: Map<String, *>, commands: Array<out () -> Any?>): Any? {
        val copy = LinkedHashMap<String, Any>(params)
        val commandChain = generateCommandChain(copy, commands)
        return support.send(commandChain)
    }

    @Throws(IOException::class)
    operator fun invoke(vararg commands: Function0<Any?>): Any? {
        return exec(commands)
    }

    @Throws(IOException::class)
    operator fun invoke(params: Map<String, *>, commands: Array<Function0<Any>>): Any? {
        return exec(params, commands)
    }

    @Throws(IOException::class)
    protected fun generateCommandChain(params: Map<String, Any>, closures: Array<out () -> Any?>): CommandChain<ClosureCommand> {
        val commands = ArrayList<ClosureCommand>(closures.size)
        for (closure in closures) {
            var uses = emptyList<Class<*>>()
            if (params.containsKey("usedClosures")) {
                @SuppressWarnings("unchecked")
                val usedClosures = params["usedClosures"] as List<Class<*>>
                uses = usedClosures
            }
            val command = commandGenerator.generate(RawClosureCommand(closure as Lambda, uses))
            commands.add(command)
        }
        return CommandChain.of(ClosureCommand::class.java, commands)
    }

}
