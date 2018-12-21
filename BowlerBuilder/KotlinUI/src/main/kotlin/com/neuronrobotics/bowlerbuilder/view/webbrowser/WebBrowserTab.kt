/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.webbrowser

import javafx.scene.control.Tab
import javax.inject.Inject

class WebBrowserTab(
    url: String?
) : Tab("Web") {

    @Inject
    constructor() : this(null)

    init {
        content = WebBrowserView.create(url).root
    }
}
