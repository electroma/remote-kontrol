package io.remotecontrol.transport.http

import io.remotecontrol.CommandChain
import io.remotecontrol.RemoteControlException
import io.remotecontrol.client.Transport
import io.remotecontrol.groovy.ContentType
import io.remotecontrol.result.Result
import io.remotecontrol.result.ResultFactory
import io.remotecontrol.result.impl.DefaultResultFactory

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Transports commands over http to the given receiver address.
 */
class HttpTransport
/**
 * @param receiverAddress the full address to the remotecontrol receiver
 * *
 * @param classLoader the class loader to use when unserialising the result
 */
@JvmOverloads constructor(private val receiverAddress: String, private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader, private val resultFactory: ResultFactory = DefaultResultFactory()) : Transport {

    /**
     * Serialises the Command and sends it over HTTP, returning the Result.

     * @throws RemoteControlException if there is any issue with the receiver.
     */
    @Throws(RemoteControlException::class)
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
     * Creates a HttpURLConnection to the remotecontrol receiver at the given receiverAddress.
     */
    @Throws(IOException::class)
    protected fun openConnection(): HttpURLConnection {
        return URL(receiverAddress).openConnection() as HttpURLConnection
    }

}
/**
 * @param receiverAddress the full address to the remotecontrol receiver
 * *
 * @param classLoader the class loader to use when unserialising the result
 */
/**
 * @param receiverAddress the full address to the remotecontrol receiver
 */
