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

package io.remorekontrol

import com.vaadin.ui.UI
import helper.HeadlessHelper

class SampleHeadlessHelper(val sampleUI: SampleUI) : HeadlessHelper() {
    override val ui: UI
        get() = sampleUI

    companion object {
        fun login(scenario: Function1<SampleHeadlessHelper, Any>): Any? {
            val sampleUI = configureUI(SampleUI::class.java)
            val helper = SampleHeadlessHelper(sampleUI)

            try {
                return scenario(helper)
            } catch (t: Throwable) {
                throw t
            } finally {
                helper.logout()
            }
        }
    }

    fun logout() {
        ui.detach()
        //VaadinSession.getCurrent().unlock()
    }

}