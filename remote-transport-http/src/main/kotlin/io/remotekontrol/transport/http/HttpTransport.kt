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

package io.remotekontrol.transport.http

import io.remotekontrol.CommandChain
import io.remotekontrol.RemoteKontrolException
import io.remotekontrol.client.Transport
import io.remotekontrol.kotlin.ContentType
import io.remotekontrol.result.Result
import io.remotekontrol.result.ResultFactory
import io.remotekontrol.result.impl.DefaultResultFactory
import java.net.HttpURLConnection
import java.net.URL

/**
 * Transports commands over http to the given receiver address.
 *
 * @param receiverAddress the full address to the remotekontrol receiver
 * *
 * @param classLoader the class loader to use when unserialising the result
 */
open class HttpTransport(private val receiverAddress: String,
                         private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
                         private val resultFactory: ResultFactory = DefaultResultFactory()) : Transport {

    /**
     * Serialises the Command and sends it over HTTP, returning the Result.

     * @throws RemoteKontrolException if there is any issue with the receiver.
     */
    override fun send(commandChain: CommandChain<*>): Result {

        val urlConnection = openConnection()
        urlConnection.setRequestProperty("Content-Type", ContentType.COMMAND.value)
        urlConnection.setRequestProperty("Accept", ContentType.RESULT.value)
        urlConnection.instanceFollowRedirects = true
        urlConnection.doOutput = true

        configureConnection(urlConnection)

        urlConnection.outputStream.use { outputStream ->
            commandChain.writeTo(outputStream)
            urlConnection.inputStream.use { inputStream ->
                return resultFactory.deserialize(inputStream, classLoader)
            }
        }
    }

    /**
     * Subclass hook for configuring the connection object before the request is set.

     * This could be used to implement authentication.
     */
    @SuppressWarnings("EmptyMethod", "UnusedParameters")
    protected fun configureConnection(connection: HttpURLConnection) {

    }

    /**
     * Creates a HttpURLConnection to the remotekontrol receiver at the given receiverAddress.
     */
    protected fun openConnection(): HttpURLConnection {
        return URL(receiverAddress).openConnection() as HttpURLConnection
    }

}
/**
 * @param receiverAddress the full address to the remotekontrol receiver
 * *
 * @param classLoader the class loader to use when unserializing the result
 */
/**
 * @param receiverAddress the full address to the remotekontrol receiver
 */
