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

package io.remotecontrol.server

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

interface Receiver {

    /**
     * Executes a serialised command chain.

     * @param commandStream A stream containing a serialised [io.remotecontrol.CommandChain] object.
     * *
     * @param resultStream The stream that the [io.remotecontrol.result.Result] object shall be written to.
     */
    @Throws(IOException::class)
    fun execute(commandStream: InputStream, resultStream: OutputStream)

}
