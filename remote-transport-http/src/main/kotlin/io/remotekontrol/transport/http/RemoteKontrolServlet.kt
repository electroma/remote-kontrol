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

import io.remotekontrol.kotlin.ContentType
import io.remotekontrol.server.Receiver
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * A servlet implementation for receiving commands.
 */
abstract class RemoteKontrolServlet : HttpServlet() {

    @Throws(ServletException::class)
    override fun init(config: ServletConfig) {
        super.init(config)
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        if (validateRequest(request, response)) {
            configureSuccessfulResponse(response)
            doExecute(request.getInputStream(), response.getOutputStream())
        }
    }

    /**
     * Validate that this request is valid.

     * Subclasses should call this implementation before any custom validation.

     * If the request is invalid, this is the place to send back the appropriate headers/body.

     * @return true if the request is valid and should proceed, false if otherwise.
     */
    protected fun validateRequest(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val contentType = request.getContentType()
        if (contentType == null || contentType != ContentType.COMMAND.value) {
            response.sendError(415, "Only remotekontrol control commands can be sent")
            return false
        }

        return true
    }

    /**
     * Called when a request has been validated.

     * Subclasses should call this implementation to set the status code and return content type.
     */
    protected fun configureSuccessfulResponse(response: HttpServletResponse) {
        response.setContentType(ContentType.RESULT.value)
    }

    /**
     * Hook for subclasses to wrap the actual execution.
     */
    protected fun doExecute(input: InputStream, output: OutputStream) {
        val receiver = createReceiver()
        receiver.execute(input, output)
    }

    /**
     * Hook for subclasses to provide a custom receiver. Will be called during init().

     * This implement returns a receiver created via the default constructor.
     */
    protected abstract fun createReceiver(): Receiver
}
