/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import javafx.scene.control.TextArea
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch

import java.io.OutputStream

// Simple stream to append input characters to a text area
internal class TextAreaPrintStream(private val textArea: TextArea) : OutputStream() {

    override fun write(character: Int) {
        launch(context = JavaFx) { textArea.appendText(character.toChar().toString()) }
    }
}
