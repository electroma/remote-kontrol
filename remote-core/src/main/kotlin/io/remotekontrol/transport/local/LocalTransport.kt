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

package io.remotekontrol.transport.local

import io.remotekontrol.CommandChain
import io.remotekontrol.client.Transport
import io.remotekontrol.result.Result
import io.remotekontrol.result.ResultFactory
import io.remotekontrol.result.impl.DefaultResultFactory
import io.remotekontrol.server.Receiver

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class LocalTransport @JvmOverloads constructor(private val receiver: Receiver, private val classLoader: ClassLoader, private val resultFactory: ResultFactory = DefaultResultFactory()) : Transport {

    @Throws(IOException::class)
    override fun send(commandChain: CommandChain<*>): Result {
        val commandBytes = ByteArrayOutputStream()
        commandChain.writeTo(commandBytes)

        val resultBytes = ByteArrayOutputStream()

        receiver.execute(ByteArrayInputStream(commandBytes.toByteArray()), resultBytes)

        return resultFactory.deserialize(ByteArrayInputStream(resultBytes.toByteArray()), classLoader)
    }

}
