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

package io.remotekontrol.kotlin.client

import io.remotekontrol.SerializationUtil
import io.remotekontrol.UnserializableCommandException
import io.remotekontrol.client.CommandGenerator
import io.remotekontrol.kotlin.ClosureCommand
import io.remotekontrol.util.UnexpectedIOException
import java.io.IOException
import java.io.NotSerializableException
import java.io.Serializable
import java.util.*

/**
 * Generates command objects from closures.
 */
class ClosureCommandGenerator @JvmOverloads constructor(private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : CommandGenerator<RawClosureCommand, ClosureCommand> {

    override val commandType: Class<ClosureCommand> = ClosureCommand::class.java

    /**
     * For the given closure, generate a command object.
     */
    override fun generate(command: RawClosureCommand): ClosureCommand {
        val bytes: ByteArray
        val classBytes: ByteArray
        val supports: MutableList<ByteArray>
        bytes = serializeInstance(command.root as Serializable)
        classBytes = getClassBytes(command.root.javaClass)

        supports = LinkedList(getSupportingClassesBytes(command.root.javaClass))

        val used = command.used
        if (!used.isEmpty()) {
            for (usedClosure in used) {
                supports.add(getClassBytes(usedClosure))
                supports.addAll(getSupportingClassesBytes(usedClosure.javaClass as Class<*>))
            }
        }

        return ClosureCommand(bytes, classBytes, supports)
    }

    /**
     * Gets the class definition bytes of any closures classes that are used by the given closure class.

     * @see InnerClosureClassDefinitionsFinder
     */
    private fun getSupportingClassesBytes(closureClass: Class<*>): List<ByteArray> {
        try {
            return InnerClosureClassDefinitionsFinder(classLoader).find(closureClass)
        } catch (e: IOException) {
            throw UnexpectedIOException("can't find inner closures of: ${closureClass.name}", e)
        }

    }

    /**
     * Gets the class definition bytes for the given closure class.
     */
    private fun getClassBytes(closureClass: Class<*>): ByteArray {
        val classFileName = getClassFileName(closureClass)
        val classFileResource = classLoader.getResource(classFileName) ?:
                throw IllegalStateException("Could not find class file for class $closureClass")

        try {
            return classFileResource.readBytes()
        } catch (e: IOException) {
            throw UnexpectedIOException("reading class files", e)
        }

    }

    private fun getClassFileName(closureClass: Class<*>): String {
        return closureClass.name.replace(".", "/") + ".class"
    }

    /**
     * Serialises the closure taking care to remove the owner, thisObject and delegate.

     * The given closure may be curried which is why we need the "root" closure because it has the owner etc.

     * closure and root will be the same object if closure is not curried.

     * @param closure the target closure to serialise
     * *
     * @param root the actual generated closure that contains the implementation.
     */
    private fun <T : Serializable> serializeInstance(closure: T): ByteArray {
        try {
            return SerializationUtil.serialize(closure)
        } catch (e: NotSerializableException) {
            throw UnserializableCommandException("Unable to serialize closure: " + closure, e)
        }

    }

}
