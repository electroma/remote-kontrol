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

package io.remotekontrol.kotlin.server

import io.remotekontrol.CommandChain
import io.remotekontrol.kotlin.ClosureCommand
import io.remotekontrol.result.Result
import io.remotekontrol.result.ResultFactory
import io.remotekontrol.server.CommandChainInvoker
import io.remotekontrol.server.CommandRunner

class ClosureCommandRunner(
        private val classLoader: ClassLoader,
        private val contextFactory: ContextFactory,
        private val resultFactory: ResultFactory) : CommandRunner<ClosureCommand> {

    override val type: Class<ClosureCommand>
        get() = ClosureCommand::class.java

    override fun run(commandChain: CommandChain<ClosureCommand>): Result {
        return invokeCommandChain(commandChain)
    }

    protected fun invokeCommandChain(commandChain: CommandChain<ClosureCommand>): Result {
        val invoker = createInvoker(classLoader, commandChain)
        return invoker.invokeAgainst(createContext(commandChain), null)
    }

    protected fun createInvoker(classLoader: ClassLoader, commandChain: CommandChain<ClosureCommand>): CommandChainInvoker {
        return CommandChainInvoker(classLoader, commandChain, resultFactory)
    }

    protected fun createContext(commandChain: CommandChain<ClosureCommand>): Any {
        return contextFactory.getContext(commandChain)
    }
}
