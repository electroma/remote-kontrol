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

import io.remotecontrol.RemoteControlException
import io.remotecontrol.SerializationUtil
import io.remotecontrol.groovy.ClosureCommand
import java.io.IOException
import kotlin.jvm.internal.FunctionImpl

class CommandInvoker(private val parentLoader: ClassLoader, private val command: ClosureCommand) {

    @Throws(Throwable::class)
    fun invokeAgainst(delegate: Any, argument: Any?): Any? {
        try {
            val instance = instantiate()

            return instance()
            //FIXME: shouldd add support for Function1,2,3
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

    @Throws(IOException::class)
    protected fun instantiate(): FunctionImpl {
        val classLoader = parentLoader
        SerializationUtil.defineClass(classLoader, command.root)

        for (bytes in command.supports) {
            SerializationUtil.defineClass(classLoader, bytes)
        }

        try {
            return SerializationUtil.deserialize(FunctionImpl::class.java, command.instance, classLoader)
        } catch (e: ClassNotFoundException) {
            throw RemoteControlException.classNotFoundOnServer(e)
        }

    }

}
