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

import io.remotekontrol.kotlin.client.RemoteKontrol
import io.remotekontrol.kotlin.server.ClosureReceiver
import io.remotekontrol.server.Receiver
import io.remotekontrol.transport.http.HttpTransport
import io.remotekontrol.transport.http.RemoteKontrolServlet
import io.remotekontrol.util.FilteringClassLoader
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

class RemoteKontrolServletSpec {

    @Test
    fun `test the servlet`() {
        assertEquals(4, remote({ val a = 2; a + 2 }))
    }

    companion object {
        lateinit var server: Server

        lateinit var endpointUrl: String

        lateinit var remote: RemoteKontrol

        @BeforeClass @JvmStatic fun setUp() {
            // we need to create a classloader for the "server" side that cannot access
            // classes defined in this file.
            val serverClassLoader = FilteringClassLoader(javaClass.classLoader, javaClass.`package`.name)

            val servlet = object : RemoteKontrolServlet() {
                override fun createReceiver(): Receiver = ClosureReceiver(serverClassLoader)
            }

            val handler = ServletHandler()
            val port = 20000 + Random().nextInt(10000)
            server = Server(port)
            server.handler = handler
            handler.addServletWithMapping(ServletHolder(servlet), "/*")
            server.start()

            endpointUrl = "http://localhost:${port}"
            remote = RemoteKontrol(HttpTransport(endpointUrl))
        }

        @AfterClass @JvmStatic fun cleanupSpec() {
            server.stop()
        }

    }
}