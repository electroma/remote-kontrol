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
 *
 *
 */

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
 *
 *
 */

import io.remotecontrol.CommandChain
import io.remotecontrol.groovy.ClosureCommand
import io.remotecontrol.groovy.server.ClosureCommandRunner
import io.remotecontrol.result.impl.DefaultResultFactory
import io.remotecontrol.server.StorageContextFactory
import org.junit.Test
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files
import java.nio.file.Paths

class ClosureDeserializeTest {

    fun defineClass(classLoader: ClassLoader, bytes: ByteArray): Class<*> {
        try {
            val defineClass = ClassLoader::class.java.getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Integer.TYPE, Integer.TYPE)
            defineClass.isAccessible = true
            val clazz = defineClass.invoke(classLoader, null, bytes, 0, bytes.size)
            return clazz as Class<*>
        } catch (e: NoSuchMethodException) {
            throw IllegalStateException(e)
        } catch (e: InvocationTargetException) {
            throw IllegalStateException(e)
        } catch (e: IllegalAccessException) {
            throw IllegalStateException(e)
        }

    }

    @Throws(ClassNotFoundException::class)
    fun <T> deserialize(type: Class<T>, bytes: ByteArray, classLoader: ClassLoader): T {
        try {
            return deserialize(type, ByteArrayInputStream(bytes), classLoader)
        } catch (e: IOException) {
            throw RuntimeException("Unexpected exception while deserializing in memory", e)
        }

    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> deserialize(type: Class<T>, inputStream: InputStream, classLoader: ClassLoader): T {
        val o = ClassLoaderConfigurableObjectInputStream(classLoader, inputStream).readObject()
        if (type.isInstance(o)) {
            return type.cast(o)
        } else {
            throw IllegalArgumentException("Expecting to deserialize instance of " + type.name + " but got " + o.javaClass.getName() + ": " + o.toString())
        }
    }

    @Test
    fun deserializeTest() {
        val clazz = Files.readAllBytes(Paths.get("clazz.dmp"))
        val obj = Files.readAllBytes(Paths.get("obj.dmp"))
        val closureCommand = ClosureCommand(obj, clazz, emptyList())
        val classLoader = Thread.currentThread().contextClassLoader
        val closureCommandRunner = ClosureCommandRunner(classLoader, StorageContextFactory.withEmptyStorage(), DefaultResultFactory())
        closureCommandRunner.run(CommandChain(ClosureCommand::class.java, listOf(closureCommand)))
    }
}

class ClassLoaderConfigurableObjectInputStream @Throws(IOException::class)
constructor(val classLoader: ClassLoader, input: InputStream) : ObjectInputStream(input) {

    @Throws(IOException::class, ClassNotFoundException::class)
    public override fun resolveClass(desc: ObjectStreamClass): Class<*> {
        try {
            return classLoader.loadClass(desc.name)
        } catch (e: ClassNotFoundException) {
            return super.resolveClass(desc)
        }

    }

}
