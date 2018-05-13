/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model

import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.text.Font

interface Selection {

    /**
     * Return the widget that should show when this is current selection.
     *
     * @return widget
     */
    fun getWidget(): Node

    /**
     * Get a [Label] in a standard font.
     *
     * @param text label text
     * @return label
     */
    @JvmDefault
    fun getTitleLabel(text: String): Label = Label(text).also { it.font = Font.font(16.0) }
}
