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

package io.remotekontrol.transport.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.remotekontrol.kotlin.ContentType
import io.remotekontrol.server.Receiver

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A HttpHandler implementation for the com.sun.net.httpserver package.
 */
class RemoteKontrolHttpHandler(private val receiver: Receiver) : HttpHandler {

    override fun handle(exchange: HttpExchange) {
        try {
            if (validateRequest(exchange)) {
                configureSuccessfulResponse(exchange)
                doExecute(exchange.requestBody, exchange.responseBody)
            }
        } catch (ignore: IOException) {

        } finally {
            exchange.close()
        }

    }

    /**
     * Validate that this request is valid.

     * Subclasses should call this implementation before any custom validation.

     * If the request is invalid, this is the place to send back the appropriate headers/body.

     * @return true if the request is valid and should proceed, false if otherwise.
     */
    @Throws(IOException::class)
    protected fun validateRequest(exchange: HttpExchange): Boolean {
        if (exchange.requestMethod != "POST") {
            exchange.sendResponseHeaders(415, 0)
            exchange.responseBody.write("request must be a POST".toByteArray(charset("UTF-8")))
            return false
        }

        if (exchange.requestHeaders.getFirst("Content-Type") != ContentType.COMMAND.value) {
            exchange.sendResponseHeaders(415, 0)
            exchange.responseBody.write(("Content type must be " + ContentType.COMMAND).toByteArray())
            return false
        }

        return true
    }

    /**
     * Called when a request has been validated.

     * Subclasses should call this implementation to set the status code and return content type.
     */
    @Throws(IOException::class)
    protected fun configureSuccessfulResponse(exchange: HttpExchange) {
        exchange.responseHeaders.set("Content-Type", ContentType.RESULT.value)
        exchange.sendResponseHeaders(200, 0)
    }

    /**
     * Does the actual command execution.

     * Subclasses can override this method to wrap the execution.
     */
    @Throws(IOException::class)
    protected fun doExecute(input: InputStream, output: OutputStream) {
        receiver.execute(input, output)
    }

}
