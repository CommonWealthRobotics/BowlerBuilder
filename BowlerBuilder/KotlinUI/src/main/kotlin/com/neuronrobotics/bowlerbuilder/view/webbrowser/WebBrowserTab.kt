/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.webbrowser

import javafx.scene.control.Tab
import tornadofx.*

class WebBrowserTab(
    url: String? = null
) : Tab("Web") {

    init {
        content = find<WebBrowserView>(
            params = mapOf(
                WebBrowserView.PAGE_TO_LOAD to url
            )
        ).root
    }
}
