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

package io.remotekontrol

import java.lang.String.format

class UnserializableExceptionException(unserializable: Throwable) :
        RemoteKontrolException(format("wrapped unserializable exception: class = %s, message = \"%s\"",
                unserializable.javaClass.name, unserializable.message)) {

    init {
        (this as java.lang.Throwable).stackTrace = unserializable.stackTrace
        unserializable.cause?.let {
            initCause(UnserializableExceptionException(it))
        }
    }

    companion object {
        val serialVersionUID = 1L
    }

}