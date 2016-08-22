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
import org.springframework.web.context.WebApplicationContext
import java.lang.System.currentTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.locks.ReentrantLock

abstract class HeadlessHelper {

    /*
    public void clearNotifications() {
        getUI().page.notifications = newNotificationListSpy()
    }

    List<Map> getNotificationsWithDetails() {
        getUI().page.notifications.collect { [caption: it.caption, style: it.styleName] }
    }
*/

    /*
    List<Notification> newNotificationListSpy() {
        new LinkedList<Notification>() {
            @Override
            boolean add(Notification n) {
                switch (n.styleName) {
                    case [Notification.Type.ERROR_MESSAGE.style, Notification.Type.WARNING_MESSAGE.style]:
                    if (!isNotificationExpected(n)) {
                        throw new NotificationException(n.styleName + ": " + n.caption)
                    }
                    break
                    default: isNotificationExpected(n)
                }
                return super.add(n)
            }
        }
    }
*/

    /*
    private void runAndCheckNotifications(Closure operation) {
        clearNotifications()
        operation()
        if (!getExpectedNotifications().isEmpty()) {
            throw new NotificationException("Expected notification hasn't been received:\n${getExpectedNotifications()}\n" +
                    "but was ${getNotificationsWithDetails()}")
        }
    }
*/

    /*def expectNotification(String s, Closure operation) {
        getExpectedNotifications().add([caption: s])
        runAndCheckNotifications(operation)
    }

    def expectNotification(String s, Notification.Type type, Closure operation) {
        getExpectedNotifications().add([caption: s, type: type])
        runAndCheckNotifications(operation)
    }

    def expectNotification(List<Map> notifications, Closure operation) {
        notifications.each { getExpectedNotifications().add([caption: it.caption, type: it.type]) }
        runAndCheckNotifications(operation)
    }
*/
    private val expectedNotifications = LinkedList<Map<*, *>>()

    /**
     * Read all pending page notification and clean up them
     */

    /*
    List<String> getNotifications() {
        def notifications = getUI().page.notifications*.caption
        clearNotifications()
        notifications
    }
*/
    protected abstract val ui: UI

    /*
    private boolean isNotificationExpected(Notification n) {
        getExpectedNotifications().remove(getExpectedNotifications().find {
            (it.caption == n.caption || n.caption.matches((String) it.caption)) &&
                    (it.type == null || it.type.style == n.styleName)
        })
    }
*/

    /**
     * Navigate to specific page.
     */
    /*
    void go(String to, Map<String, ?> params = [:]) {
        getUI().go(to, params)
    }
*/

    /**
     * Assert currentView is the viewClass.
     * execute action and pass the view as a DashboardViewWrapper/RequestViewWrapper parameter
     */
    /*
    void at(Class<HasComponents> viewClass, Function0 actions) {
        assert getUI().currentView.class == viewClass
        actions(createWrapper(getUI().currentView))
    }
*/

    /**
     * Assert currentView is the viewClass.

     * This method will not fail if condition didn't met in time. Closure **must** return
     * something truthy

     * execute action and pass the view as a DashboardViewWrapper/RequestViewWrapper parameter
     */
    /*
    void waitEvent(long timeoutMilliseconds, Closure checker) {
        final long startTime = currentTimeMillis()
        while (!checker(createWrapper(getUI().currentView)) && currentTimeMillis() < startTime + timeoutMilliseconds) {
            logger.info('waited: ' + (currentTimeMillis() - startTime))
            Thread.sleep(50)
        }
        final long endTime = currentTimeMillis()
        logger.info('Totally waited: ' + (endTime - startTime))
    }
*/

    /*
    static
    def <T> T waitForCondition(long timeout = 10000L, long pollingInterval = 1000L, Closure<T> action) {
        def stopwatch = Stopwatch.createStarted()
        while (true) {
            try {
                def result = action()
                return result
            } catch (AssertionError assertionError) {
                if (stopwatch.elapsed(MILLISECONDS) >= timeout)
                    throw assertionError
                Thread.sleep(pollingInterval)
            }
        }
    }
*/

    /**
     * @return the last expected window
     */
    /*
    Windows getWindow() {
        def windows = getUI().windows as List
        assert windows: "No open windows found"
        def window = windows.last()
        assert isExpectedModalWindow(window.caption): "${window.caption} doesn't match expected caption"
        new Windows(window)
    }
*/

    val isAnyWindowOpen: Boolean
        get() = !ui.windows.isEmpty()

    /**
     * @return an expected window with the specified title
     */
    /*
    Windows getWindow(String title) {
        assert isExpectedModalWindow(title): "Window with '$title' is not expected"
        def window = getUI().windows.find { it.caption == title }
        assert window: "No open window with title $title found"
        new Windows(window)
    }
*/

    /**
     * Expected modal windows. Each element can be either a single caption to wait for.
     */
    var expectedModalWindowCaptionStack: Deque<String> = LinkedList()

    /*   */
    /**
     * No longer expecting a modal window (or windows), clearing appropriate data structures.
     */
    /*
    void popExpectedWindowCaption() {
        expectedModalWindowCaptionStack.pop()
    }

    */
    /**
     * Executed prior to wait for a model window to init appropriate data structures.
     * @param singleCaption caption for a single modal window to expectComponent (may be `null)
    ` */
    /*
    void pushExpectedWindowCaption(String singleCaption) {
        if (!singleCaption && (singleCaption != '')) {
            throw new IllegalArgumentException("A caption must be specified!");
        }
        expectedModalWindowCaptionStack.push(singleCaption)
    }

    */
    /**
     * Checks whether window with a given caption is among the expected one(s). Typically used as an argument to the `assert` statement.
     * @param caption window caption to check
     * *
     * @return `true` if window is among expected ones, `false` otherwise
     */
    /*
    boolean isExpectedModalWindow(String caption) {
        assert !expectedModalWindowCaptionStack.empty: "There're no expected windows at all, so \"$caption\" is definitely unexpected"
        def lastExpected = expectedModalWindowCaptionStack.last()
        lastExpected == caption
    }

    */

    /**
     * Expects a modal window with a certain caption to be shown as a result of a given operation execution.

     * @param caption      caption for a single modal window to expectComponent (may be `null)
     * *
     * @param mustBeClosed whether window must be closed after operation is executed
     * *
     * @param operation    operation that should result in an expected modal window(s)
    ` */
    /*
    def expectModalWindow(String caption = null, boolean mustBeClosed = true, Closure operation) {
        pushExpectedWindowCaption(caption)
        operation()
        if (mustBeClosed) {
            // fixme should this check be here?
            assert !getUI().windows.find {
                it.caption == caption
            }: "Window '${caption}' is not closed"
        }
        popExpectedWindowCaption()
    }
*/
    fun expectWindow(caption: String, operation: Function0<*>): Window {
        operation.invoke()
        return ui.windows.firstOrNull { it -> caption == it.getCaption() } ?:
                throw IllegalStateException("Window '$caption' not expected to be open!")
    }

    class NotificationException internal constructor(cause: String) : Exception(cause)

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

        fun setCtx(ctx: WebApplicationContext) {
            this.ctx = ctx
        }

        val EXPECT_TIMEOUT_MS = 60000

        /**
         * Temporary test data, mostly for configuring stubs in workflows
         */
        val testData: MutableMap<String, Any> = HashMap()

        /**
         * DO NOT TRY TO TAKE IT OUT FOR ANYTHING BUT CONCURRENT TESTS!

         * @param action
         * *
         * @return
         */
        fun withUnlock(action: Function0<*>) {
            val vaadinSession = VaadinSession.getCurrent()
            vaadinSession.service.runPendingAccessTasks(vaadinSession)
            vaadinSession.unlock()
            try {
                action.invoke()
            } finally {
                vaadinSession.lock()
            }
        }

        /**
         * DO NOT TRY TO TAKE IT OUT FOR ANYTHING BUT CONCURRENT TESTS!
         * Attempts to run a given closure until it's executed successfully (i.e. without an exception), or timeout.

         * @param closure what to execute
         * *
         * @return closure execution result
         */
        @Throws(Throwable::class)
        fun expect(closure: Function0<*>): Any? {
            var initialThrowable: Throwable? = null

            // Wait until EXPECT_TIMEOUT_MS has passed
            val startTime = System.nanoTime()
            var i = 0
            while (System.nanoTime() - startTime < MILLISECONDS.toNanos(EXPECT_TIMEOUT_MS.toLong())) {
                try {
                    return closure.invoke()
                } catch (t: Throwable) {
                    // The very first failure is important - we'll store and rethrow it in case of an eventual failure
                    initialThrowable = if (initialThrowable != null) initialThrowable else t
                }

                i++
                //            try {
                //TODOL: kotlinize
                //withUnlock({ Thread.sleep(50) })
                //            } catch (InterruptedException ignore) {
                //                break;
                //            }
            }

            try {
                return closure.invoke()
            } catch (t: Throwable) {
                if (initialThrowable != null) {
                    /*
                 * Damn, failed ultimately... Throw the very first exception to give developer a clue of what's actually happened. However,
                 * current exception might also be interesting - log it.
                 */
                    logger.error("Last closure execution has failed with exception (note that this one is probably not the root cause of the failure, so make sure you took a look at the very last stacktrace)", t)
                    throw initialThrowable
                }
                // We didn't have any throwables previously - just return what we got
                throw t
            }

        }

        @JvmStatic fun <T : UI> configureUI(uiClass: Class<T>): T {
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

            //TODO: fix

            //if (!isConcurrent()) {
            // fake lock to avoid constant lock / unlock issues
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

/*        fun setTestData(data: Map<String, Any>) {
            testData.clear()
            testData.putAll(data)
        }

        fun getTestData(): Map<String, *> {
            unmodifiableMap(testData)
        }*/
    }
}
