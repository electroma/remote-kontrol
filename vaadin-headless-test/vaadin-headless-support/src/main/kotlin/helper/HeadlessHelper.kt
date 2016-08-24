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

package helper

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.server.*
import com.vaadin.shared.VBrowserDetails
import com.vaadin.shared.communication.PushMode
import com.vaadin.ui.UI
import com.vaadin.ui.Window
import com.vaadin.util.CurrentInstance
import org.slf4j.LoggerFactory.getLogger
import org.springframework.context.ApplicationContext
import java.lang.System.currentTimeMillis
import java.util.*
import java.util.concurrent.locks.ReentrantLock

abstract class HeadlessHelper {

    protected abstract val ui: UI
    fun expectWindow(caption: String, operation: Function0<*>): Window {
        operation.invoke()
        return ui.windows.firstOrNull { it -> caption == it.getCaption() } ?:
                throw IllegalStateException("Window '$caption' not expected to be open!")
    }

    private class SimpleWrappedSession : WrappedSession {
        private val attrs = HashMap<String, Any>()

        private val created = currentTimeMillis()

        override fun getMaxInactiveInterval(): Int {
            return -1
        }

        override fun getAttribute(name: String): Any? {
            return attrs[name]
        }

        override fun setAttribute(name: String, value: Any) {
            attrs.put(name, value)
        }

        override fun getAttributeNames(): Set<String> {
            return attrs.keys
        }

        override fun invalidate() {
            attrs.clear()
        }

        override fun getId(): String {
            return "" + System.identityHashCode(this)
        }

        override fun getCreationTime(): Long {
            return created
        }

        override fun getLastAccessedTime(): Long {
            return currentTimeMillis()
        }

        override fun isNew(): Boolean {
            return false
        }

        override fun removeAttribute(name: String) {
            attrs.remove(name)
        }

        override fun setMaxInactiveInterval(interval: Int) {
        }
    }

    companion object {
        private val logger = getLogger(HeadlessHelper::class.java)

        var ctx: ApplicationContext? = null
            internal set
        fun <T : UI> configureUI(uiClass: Class<T>): T {
            resetThreadLocals()

            val request = mock<VaadinRequest>()
            val service = HeadlessVaadinService(DefaultDeploymentConfiguration(VaadinServlet::class.java, Properties()))
            val session = createSession(service)

            val userAgent = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36"
            val browserApplication = session.browser.javaClass.getDeclaredField("browserApplication")
            browserApplication.isAccessible = true
            browserApplication.set(session.browser, userAgent)

            val browserDetails = session.browser.javaClass.getDeclaredField("browserDetails")
            browserDetails.isAccessible = true
            browserDetails.set(session.browser, VBrowserDetails(userAgent))

            whenever(request.getParameter("v-loc")).thenReturn("http://localhost:8080/ui#!/?")

            VaadinSession.setCurrent(session)

            val ui = ctx!!.getBean(uiClass)
            CurrentInstance.setCurrent(ui)

            ui.locale = Locale.ENGLISH
            ui.session = session
            ui.pushConfiguration.pushMode = PushMode.MANUAL
            ui.doInit(request, -1, "test")

            val initMethod = ui.javaClass.getDeclaredMethod("init", VaadinRequest::class.java)
            initMethod.isAccessible = true
            initMethod.invoke(ui, request)
            return ui
        }

        fun resetThreadLocals() {
            CurrentInstance.clearAll()
        }

        private fun createSession(service: HeadlessVaadinService): VaadinSession {
            val session = VaadinSession(service)
            val wrappedSession = SimpleWrappedSession()
            val lock = mock<ReentrantLock>()
            whenever(lock.isHeldByCurrentThread()).thenReturn(true)
            wrappedSession.setAttribute(service.getServiceName() + ".lock", lock)
            //}
            service.prepare(wrappedSession)
            session.storeInSession(service, wrappedSession)
            val refreshLock = session.javaClass.getDeclaredMethod("refreshLock")
            refreshLock.isAccessible = true
            refreshLock.invoke(session)
            session.lock()
            return session
        }

    }
}
