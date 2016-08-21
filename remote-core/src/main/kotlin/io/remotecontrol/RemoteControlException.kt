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

package io.remotecontrol

open class RemoteControlException : RuntimeException {

    constructor(message: Any) : super(message as String)

    constructor(message: Any, cause: Throwable? = null) : super(message.toString(), cause)

    companion object {

        val serialVersionUID = 1L

        fun classNotFoundOnServer(e: ClassNotFoundException): RemoteControlException {
            return RemoteControlException("Class not found on server (the command referenced a class that the server does not have)", e)
        }

        fun classNotFoundOnClient(e: ClassNotFoundException): RemoteControlException {
            return RemoteControlException("Class not found on client (the result referenced a class that the client does not have)", e)
        }
    }

}