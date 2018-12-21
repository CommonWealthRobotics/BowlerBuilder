/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.main.event

import javafx.scene.Node
import javafx.scene.control.Tab

/**
 * Search the main tabs by their [Tab.content] and close any tabs that match [tabContent].
 */
data class CloseTabByContentEvent(
    val tabContent: Node
)
