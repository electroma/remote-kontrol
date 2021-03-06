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

package io.remotekontrol.transport.http.test

import com.sun.net.httpserver.HttpServer
import io.remotekontrol.kotlin.client.RemoteKontrol
import io.remotekontrol.kotlin.server.ClosureReceiver
import io.remotekontrol.transport.http.HttpTransport
import io.remotekontrol.transport.http.RemoteKontrolHttpHandler
import io.remotekontrol.util.FilteringClassLoader
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class RemoteKontrolHttpHandlerTest {

    @Test
    fun `test the handler`() {
        assertEquals(4, remote({ val a = 2; a + 2 }))
    }

    companion object {
        val server: HttpServer

        val remote: RemoteKontrol

        init {
            // we need to create a classloader for the "server" side that cannot access
            // classes defined in this file.
            val thisClassLoader = javaClass.classLoader
            val serverClassLoader = FilteringClassLoader(thisClassLoader, javaClass.`package`.name)

            val receiver = ClosureReceiver(serverClassLoader)

            server = HttpServer.create(InetSocketAddress(0), 1)
            server.createContext("/", RemoteKontrolHttpHandler(receiver))
            server.executor = Executors.newSingleThreadExecutor()
            server.start()

            Thread.sleep(2000)

            remote = RemoteKontrol(HttpTransport("http://localhost:${server.address.port}" as String))
        }


        @AfterClass @JvmStatic fun teardown() {
            server.stop(0)
        }
    }
}