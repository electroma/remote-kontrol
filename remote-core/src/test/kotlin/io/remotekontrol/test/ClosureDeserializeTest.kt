package io.remotekontrol.test/*
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

import io.remotekontrol.CommandChain
import io.remotekontrol.kotlin.ClosureCommand
import io.remotekontrol.kotlin.server.ClosureCommandRunner
import io.remotekontrol.result.impl.DefaultResultFactory
import io.remotekontrol.server.StorageContextFactory
import org.junit.Test
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths

class ClosureDeserializeTest {

    @Test
    fun deserializeTest() {
        val clazz = readAllBytes(Paths.get(classLoader().getResource("clazz.dmp").toURI()))
        val obj = readAllBytes(Paths.get(classLoader().getResource("obj.dmp").toURI()))
        val closureCommand = ClosureCommand(obj, clazz, emptyList())
        val classLoader = Thread.currentThread().contextClassLoader
        val closureCommandRunner = ClosureCommandRunner(classLoader, StorageContextFactory.Companion.withEmptyStorage(), DefaultResultFactory())
        closureCommandRunner.run(CommandChain(ClosureCommand::class.java, listOf(closureCommand)))
    }

    private fun classLoader() = this.javaClass.classLoader
}
