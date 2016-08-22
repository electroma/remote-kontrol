package io.remotecontrol.transport.http

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import io.remotecontrol.groovy.ContentType
import io.remotecontrol.server.Receiver

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A HttpHandler implementation for the com.sun.net.httpserver package.
 */
class RemoteControlHttpHandler(private val receiver: Receiver) : HttpHandler {

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
