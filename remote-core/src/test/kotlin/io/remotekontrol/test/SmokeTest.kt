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

package io.remotekontrol.test

import io.remotekontrol.UnserializableCommandException
import io.remotekontrol.UnserializableException
import io.remotekontrol.UnserializableExceptionException
import io.remotekontrol.client.RemoteException
import io.remotekontrol.client.Transport
import io.remotekontrol.client.UnserializableResultStrategy
import io.remotekontrol.client.UnserializableReturnException
import io.remotekontrol.kotlin.client.RemoteKontrol
import io.remotekontrol.kotlin.server.ClosureReceiver
import io.remotekontrol.transport.local.LocalTransport
import io.remotekontrol.util.FilteringClassLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.net.URLClassLoader

class SmokeTest {

    // Used in a later test
    val anIvar = 2

    var remote: RemoteKontrol? = null


    var transport: Transport? = null
    var clientClassLoader: URLClassLoader = Thread.currentThread().contextClassLoader as URLClassLoader
    @Before
    fun setUp() {

/*
        val kotlinOnlyClasses = URLClassLoader(clientClassLoader.urLs.filter { it.file.toString().contains("kotlin") }.toTypedArray(),
                clientClassLoader.parent)
*/

        val serverClassLoader = FilteringClassLoader(clientClassLoader, "io.remotekontrol.test")
        val receiver = ClosureReceiver(serverClassLoader)

        transport = LocalTransport(receiver, clientClassLoader)
        remote = RemoteKontrol(transport!!, clientClassLoader)
    }

    /**
     * The result of the command run on the server is sent back and is returned
     */
    @Test
    fun testReturingValues() {
        assertEquals(2, remote!!({ val a = 1; a + 1 }))
    }

    /**
     * Commands can contain other closures
     */

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    @Test
    fun testWithInnerClosures() {
        assertEquals(listOf(2, 3, 4), remote!!({
            var transform: (Int) -> Int = { it + 2 }
            transform = { it + 1 }
            listOf(1, 2, 3).map(transform)
        }))
    }

    /**
     * If the command throwns an exception, we throw a RemoteException
     * client side with the actual exception instance that was thrown server
     * side as the cause
     */

    @Test
    fun testThrowingException() {
        var thrown: Throwable?
        try {
            remote!!({ throw Exception("bang!") })
        } catch (e: RemoteException) {
            thrown = e.cause
            assert(thrown!! is Exception)
            assert(thrown.message == "bang!")
        }
    }

    /**
     * If the command returns something that is unserialisable, we thrown an UnserializableReturnException
     */

    @Test(expected = UnserializableReturnException::class)
    fun testUnserialisableReturn() {
        remote!!({ System.out })
    }

    /**
     * If the command returns something that is unserialisable, we thrown an UnserializableReturnException
     */
    @Test(expected = UnserializableReturnException::class)
    fun testNestedUnserialisableReturn() {
        remote!!({ mapOf("m" to mapOf("out" to System.out)) })
    }

    @Ignore // TODO: need to raise bug Kotlin jira
    @Test
    fun testUnserializableExceptionWithCause() {
        try {
            remote!!({
                val e = UnserializableException(Exception("cause foo"))
                throw e
            })
        } catch (e: RemoteException) {
            assert(e.cause is UnserializableExceptionException) // also
            assertEquals("wrapped unserializable exception: class = UnserializableException, message = \"null\"",
                    (e.cause as UnserializableExceptionException).message)
        }
    }

    @Test
    fun testCanSpecifyToUseNullIfReturnWasUnserializable() {
        remote = RemoteKontrol(transport!!, UnserializableResultStrategy.NULL, clientClassLoader)
        assertNull(remote!!({ System.out }))
    }

    @Test
    fun testCanSpecifyToUseStringRepresentationIfReturnWasUnserializable() {
        remote = RemoteKontrol(transport!!, UnserializableResultStrategy.STRING, clientClassLoader)
        assert(remote!!({ System.out }).toString().contains("Stream"))
    }

    /**
     * If the command returns an exception but does not throw it, we just return the exception
     */
    @Test
    fun testReturningException() {
        assert(remote!!({ Exception() }) is Exception)
    }

    /**
     * We can access lexical scope (within limits)
     */

    @Test
    fun testAccessingLexicalScope() {
        val a = 1
        assertEquals(2, remote!!({ a + 1 }))
    }

    @Test(expected = UnserializableCommandException::class)
    fun testAccessingVariablesInLexicalScope() {
        var a = 1
        assertEquals(2, remote!!({ a + 1 }))
    }


    /**
     * Anything in lexical scope we access must be serialisable
     */
    @Test(expected = UnserializableCommandException::class)
    fun testAccessingNonSerializableLexicalScope() {
        val a = System.out
        remote!!({ a })
    }

    /**
     * Owner ivars can't be accessed because they aren't really lexical
     * so get treated as bean names from the app context
     */
    @Test(expected = UnserializableCommandException::class)
    fun testAccessingIvar() {
        assertEquals(4, remote!!({ anIvar * 2 }))
    }

    /**
     * Closures defined outside of the exec closures can be used inside of them if only the closures defined outside are passed as contextClosure option. Useful when creating DSLs.
     */

    @Test
    fun testPassingUsedClosures() {
        val contextClosure = { 1 }
        assertEquals(2, remote!!({ contextClosure() + 1 }))
    }

    @Test
    fun testPassingUsedClosuresFromFields() {
        assertEquals(2, remote!!(GlobalFun::class) { GlobalFun.globalContextClosure() + 1 })
    }

/*
        void testPassingUsedClosuresWithInnerClosures() {
            def contextClosure = { (1..3).inject(0) { sum, value -> sum + value } }
            assert remote.exec(usedClosures: [contextClosure]) { contextClosure() } == 6
        }

        void testPassingUsedClosuresThatAccessADelegate() {
            def contextClosure = { size() }
            assert remote.exec(usedClosures: [contextClosure]) {
                contextClosure.setProperty('delegate', 'some text')
                contextClosure()
            } == 9
        }

        void testPassingWrongTypeInUsedClosures() {
            assert shouldFail(IllegalArgumentException) {
                remote.exec(usedClosures: {}) {}
            } == "'usedClosures' argument must be iterable"
        }

        void testPassingUnknownOption() {
            assert shouldFail(IllegalArgumentException) {
                remote.exec(unknown: {}) {}
            } == "Unknown option 'unknown'"
        }

        */

    /**
     * Any classes referenced have to be available in the remotecontrol app,
     * and any classes defined in tests ARE NOT.
     */
    /*

        void testCannotReferToClassesNotInTheApp() {
            def a = new GroovyClassLoader().parseClass("class T implements Serializable {}").newInstance()
            shouldFailWithCause(ClassNotFoundException) {
                remote.exec {
                    a
                }
            }
        }

        */
    /**
     * Variation of above, but yields a different error.
     */
    /*

        @SuppressWarnings("EmptyClass")
        static class Inner {}

        void testCannotInstantiateClassesNotInTheApp() {
            shouldFailWithCause(NoClassDefFoundError) {
                remote.exec { new Inner() }
            }
        }

        */
    /**
     * Multiple commands can be sent, the return value of the previous
     * command is passed to the next command as it's single argument
     */
    /*

        void testCommandChaining() {
            remote.exec({ 1 }, { it + 1 }) { it + 1 } == 3
        }

        */
    /**
     * The delegate of commands is like a map and can store properties.
     */
    /*

        void testCanUseDelegateStorageAlongChain() {
            remote.exec(
                { num = 1 },
                { num = num + 1 }
            ) { num + 1 } == 3
        }

        */
    /**
     * Trying to access a property that is not existing in the delegate
     * causes a MissingPropertyException
     */
    /*

        void testAccessingNonExistantPropertyFromDelegateCausesMPE() {
            shouldFailWithCause(MissingPropertyException) {
                remote.exec { iDontExist == true }
            }
        }

        void testCanSetProperties() {
            remote.exec { new GregorianCalendar().time = new Date() }
        }

        void testCanCallMethodsDynamicaly() {
            def methodName = "setTime"
            remote.exec { new GregorianCalendar()."$methodName"(new Date()) } != null
        }

        void testCanUseSpreadOperator() {
            remote.exec { [1, 2, 3]*.toString() } == ["1", "2", "3"]
        }

        void testCanUseSpreadMapOperator() {
            remote.exec { new HashMap(*: [a: 1, b: 2]) }
        }

        void testExternalLibrariesExecutingCodeOnRemote() {
            assert new RemoteCallingClass(remote).multiplyBy2OnRemote(3) == 6
        }
    */
    /**
     * This test case shows how to use the remotecontrol control and some of it's limitations
     * with regard to serialisation and scope.
     *
     * The remotecontrol control object has an exec(Closure) method, and an alias for that as call(Closure).
     * The call(Closure) variant allows the use of the Groovy language feature where you can essentially
     * treat an object like a method, which is how “remotecontrol { … }” works below (i.e. it's really “remotecontrol.call { … }).
     * This doesn't always work though as you will see (due to Groovy), so sometimes you need to use .exec().
     *
     * Where we are passing a closure to the remotecontrol control object, that closure gets executed INSIDE the
     * application we are functionally testing, which may be in a different JVM on a completely different machine.
     * This works by sending the closure over HTTP to the application (which must have the remotecontrol-control plugin installed).
     *
     * An example use for this would be creating/deleting domain data inside your remotecontrol application for testing purposes.
     */
}

object GlobalFun {
    @JvmStatic fun globalContextClosure() = 1
}
