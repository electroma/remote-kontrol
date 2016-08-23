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

package io.remotekontrol.kotlin.server

import io.remotekontrol.result.ResultFactory
import io.remotekontrol.result.impl.DefaultResultFactory
import io.remotekontrol.server.MultiTypeReceiver
import io.remotekontrol.server.Receiver
import io.remotekontrol.server.StorageContextFactory
import java.io.InputStream
import java.io.OutputStream

/**
 * Receives a serialised command chain as an input stream to be unserialised and executed, then writes the serialised result to an output stream.
 */
class ClosureReceiver
/**
 * @param classLoader the class loader that will be used when unserialising the command chain
 * *
 * @param contextFactory the context factory to use to create contexts for command chains
 * *
 * @see ContextFactory
 */
@JvmOverloads constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader, contextFactory: ContextFactory = StorageContextFactory.withEmptyStorage(), resultFactory: ResultFactory = DefaultResultFactory()) : Receiver {
    private val delegate: Receiver

    init {
        delegate = MultiTypeReceiver(classLoader, ClosureCommandRunner(classLoader, contextFactory, resultFactory))
    }

    /**
     * Creates a receiever that uses the current threads context class loader and the given contextFactory.

     * @param contextFactory the context factory to use to create contexts for command chains
     * *
     * @see io.remotecontrol.server.Receiver
     */
    constructor(contextFactory: ContextFactory) : this(Thread.currentThread().contextClassLoader, contextFactory) {
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given map as a seed.

     * @param classLoader the class loader that will be used when unserialising the command chain
     * *
     * @param contextStorageSeed the seed for the storage
     * *
     * @see io.remotecontrol.server.StorageContextFactory.withSeed
     */
    constructor(classLoader: ClassLoader, contextStorageSeed: Map<String, Any>) : this(classLoader, StorageContextFactory.withSeed(contextStorageSeed)) {
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory with the given map as the seed.

     * @param contextStorageSeed the seed for the storage
     * *
     * @see io.remotecontrol.server.Receiver
     */
    constructor(contextStorageSeed: Map<String, Any>) : this(Thread.currentThread().contextClassLoader, contextStorageSeed) {
    }

    override fun execute(commandStream: InputStream, resultStream: OutputStream) {
        delegate.execute(commandStream, resultStream)
    }
}
