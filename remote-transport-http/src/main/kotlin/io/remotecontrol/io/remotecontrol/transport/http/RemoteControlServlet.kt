package io.remotecontrol.transport.http

import io.remotecontrol.groovy.ContentType
import io.remotecontrol.server.Receiver
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
abstract class RemoteControlServlet : HttpServlet() {

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
    @Throws(IOException::class)
    protected fun validateRequest(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        val contentType = request.getContentType()
        if (contentType == null || contentType != ContentType.COMMAND.value) {
            response.sendError(415, "Only remotecontrol control commands can be sent")
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
    @Throws(IOException::class)
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
