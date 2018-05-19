/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.tab

import javafx.scene.Node
import javafx.scene.control.Tab

/**
 * A generic tab.
 *
 * @param title the title of this tab
 * @param <T> controller type for the content
 */
abstract class AbstractTab<T>(title: String) : Tab(title) {

    /**
     * The visual content of this tab.
     */
    abstract val view: Node

    /**
     * The controller for the content of this tab.
     */
    abstract val controller: T
}
