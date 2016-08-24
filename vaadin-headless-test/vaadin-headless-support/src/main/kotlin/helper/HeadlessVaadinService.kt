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

package helper

import com.vaadin.server.*
import com.vaadin.ui.UI
import java.io.File
import java.io.InputStream

class HeadlessVaadinService
/**
 * Creates a new vaadin service based on a deployment configuration

 * @param deploymentConfiguration the deployment configuration for the service
 */
(deploymentConfiguration: DeploymentConfiguration) : VaadinService(deploymentConfiguration) {

    override fun getStaticFileLocation(request: VaadinRequest): String? {
        return null
    }

    override fun getConfiguredWidgetset(request: VaadinRequest): String? {
        return null
    }

    override fun getConfiguredTheme(request: VaadinRequest): String? {
        return null
    }

    override fun isStandalone(request: VaadinRequest): Boolean {
        return false
    }

    override fun getMimeType(resourceName: String): String? {
        return null
    }

    override fun getBaseDirectory(): File? {
        return null
    }

    override fun requestCanCreateSession(request: VaadinRequest): Boolean {
        return false
    }

    override fun getServiceName(): String? {
        return null
    }

    override fun getThemeResourceAsStream(uI: UI, themeName: String, resource: String): InputStream? {
        return null
    }

    override fun getMainDivId(session: VaadinSession, request: VaadinRequest, uiClass: Class<out UI>): String? {
        return null
    }

    fun prepare(session: WrappedSession) {
        lockSession(session)
        unlockSession(session)
    }

    override fun ensurePushAvailable(): Boolean {
        return true
    }
}
