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

package io.remotekontrol.server

import io.remotekontrol.Command
import io.remotekontrol.CommandChain
import io.remotekontrol.kotlin.server.ContextFactory
import java.util.*

/**
 * A context factory that returns Storage objects.

 * Note that this class is abstract, but provides static methods to produce context factory instances.

 * @see io.remotekontrol.kotlin.server.ContextFactory

 * @see io.remotekontrol.server.Storage

 * @see io.remotekontrol.server.Receiver
 */
abstract class StorageContextFactory : ContextFactory {

    private class WithEmptyStorage : StorageContextFactory() {
        override fun getContext(chain: CommandChain<out Command>): Storage {
            return Storage(LinkedHashMap<String, Any>())
        }

    }

    private class WithSeed(seed: Map<String, Any>) : StorageContextFactory() {
        val seed: Map<String, Any>

        init {
            this.seed = LinkedHashMap(seed)
        }

        override fun getContext(chain: CommandChain<out Command>): Storage {
            return Storage(LinkedHashMap(seed))
        }
    }

    private class WithGenerator(val generator: Function1<CommandChain<*>, MutableMap<String, Any>>) : StorageContextFactory() {

        override fun getContext(chain: CommandChain<out Command>): Storage {
            val storage = generator(chain)
            return Storage(storage)
        }
    }

    companion object {

        /**
         * Creates a factory that generates storage objects with no initial values.
         */
        fun withEmptyStorage(): StorageContextFactory {
            return WithEmptyStorage()
        }

        /**
         * Creates a factory that uses the given map as a seed. That is, the seed will be copied to a new map to be used as the storage for each request for a context.
         */
        fun withSeed(seed: Map<String, Any>): StorageContextFactory {
            return WithSeed(seed)
        }

        /**
         * Creates a factory that calls the given generator closure to produce a map to be used as the storage. The generator is invoked for each request for a context factory. Given generator
         * implementations should take care to implement their own thread safety as they may be invoked by different threads at any given time.
         */
        fun withGenerator(generator: Function1<CommandChain<*>, MutableMap<String, Any>>): StorageContextFactory {
            return WithGenerator(generator)
        }
    }
}
