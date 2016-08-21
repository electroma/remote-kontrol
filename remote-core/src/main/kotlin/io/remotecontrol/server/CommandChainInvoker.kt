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

import io.remotecontrol.CommandChain
import io.remotecontrol.groovy.ClosureCommand
import io.remotecontrol.result.Result
import io.remotecontrol.result.ResultFactory

class CommandChainInvoker(private val parentLoader: ClassLoader, private val commandChain: CommandChain<ClosureCommand>, private val resultFactory: ResultFactory) {

    fun invokeAgainst(delegate: Any, firstArg: Any?): Result {
        var arg = firstArg

        for (command in commandChain.commands) {
            val invoker = createInvoker(parentLoader, command)
            try {
                arg = invoker.invokeAgainst(delegate, arg)
            } catch (throwable: Throwable) {
                return resultFactory.forThrown(throwable)
            }

        }

        return resultFactory.forValue(arg)
    }

    protected fun createInvoker(loader: ClassLoader, command: ClosureCommand): CommandInvoker {
        return CommandInvoker(loader, command)
    }

}
