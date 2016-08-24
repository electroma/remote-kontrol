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

import io.remotekontrol.CommandChain
import io.remotekontrol.client.CommandGenerator
import io.remotekontrol.client.RemoteKontrolSupport
import io.remotekontrol.client.Transport
import io.remotekontrol.client.UnserializableResultStrategy
import io.remotekontrol.kotlin.ClosureCommand
import kotlin.reflect.KClass

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

    operator fun invoke(vararg commands: Function0<Any?>, libClasses: List<Class<*>> = emptyList()): Any? {
        val commandChain = generateCommandChain(libClasses, commands)
        return support.send(commandChain)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> invoke(vararg libClasses: KClass<*>, command: () -> T): T? {
        val commandChain = generateCommandChain(libClasses.map { it.java }, arrayOf(command))
        return support.send(commandChain) as T
    }

    private fun <T> generateCommandChain(libClasses: List<Class<*>>, closures: Array<out Function0<T>>): CommandChain<ClosureCommand> {
        val commands = closures.map { commandGenerator.generate(RawClosureCommand(it, libClasses)) }
        return CommandChain.of(ClosureCommand::class.java, commands)
    }
}
