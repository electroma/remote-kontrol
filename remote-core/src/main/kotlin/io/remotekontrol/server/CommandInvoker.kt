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

package io.remotekontrol.server

import io.remotekontrol.RemoteKontrolException
import io.remotekontrol.SerializationUtil
import io.remotekontrol.kotlin.ClosureCommand
import kotlin.jvm.internal.FunctionImpl

class CommandInvoker(private val parentLoader: ClassLoader, private val command: ClosureCommand) {

    fun invokeAgainst(delegate: Any, argument: Any?): Any? {
        try {
            val instance = instantiate()

            return instance()
            //FIXME: should add support for Function1 to accept support chaining
            /*else {
                return instance(argument)
            }*/
        } catch (thrown: Throwable) {
            //FIXME
            /*// If the server and client do not share the groovy classes, we get this
            try {
                parentLoader.loadClass(InvokerInvocationException::class.java!!.getName()).isAssignableFrom(thrown.javaClass)
            } catch (throwable: ClassNotFoundException) {
                throw thrown.cause
            }*/

            throw thrown
        }

    }

    private fun instantiate(): FunctionImpl {
        SerializationUtil.defineClass(parentLoader, command.root)

        for (bytes in command.supports) {
            SerializationUtil.defineClass(parentLoader, bytes)
        }

        try {
            return SerializationUtil.deserialize(FunctionImpl::class.java, command.instance, parentLoader)
        } catch (e: ClassNotFoundException) {
            throw RemoteKontrolException.classNotFoundOnServer(e)
        }

    }

}
