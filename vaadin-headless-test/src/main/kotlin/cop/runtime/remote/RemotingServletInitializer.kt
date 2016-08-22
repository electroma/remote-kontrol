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

package cop.runtime.remote

import helper.HeadlessHelper
import io.remotecontrol.groovy.server.ClosureReceiver
import io.remotecontrol.server.Receiver
import io.remotecontrol.transport.http.RemoteControlServlet
import org.springframework.web.WebApplicationInitializer
import org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext
import javax.servlet.ServletContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

open class RemotingServletInitializer : WebApplicationInitializer {

    override fun onStartup(container: ServletContext) {

        if (System.getProperty(REMOTING_PROP_KEY) != null) {

            val remotingServlet = container.addServlet("kotlin-remoting", object : RemoteControlServlet() {

                override fun createReceiver(): Receiver {
                    return ClosureReceiver()
                }
            })
            remotingServlet.addMapping("/remoting/")
            remotingServlet.setLoadOnStartup(1)

            container.addListener(object : ServletContextListener {
                override fun contextInitialized(sce: ServletContextEvent) {
                    HeadlessHelper.ctx = getWebApplicationContext(sce.servletContext)
                }

                override fun contextDestroyed(sce: ServletContextEvent) {
                    // do nothing
                }
            })
        }
    }

    companion object {

        val REMOTING_PROP_KEY = "remoting"
    }
}
