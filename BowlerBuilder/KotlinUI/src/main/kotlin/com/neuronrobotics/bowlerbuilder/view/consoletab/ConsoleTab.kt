/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.consoletab

import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.scene.control.Tab
import javafx.scene.control.TextArea
import tornadofx.*
import java.io.OutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsoleTab : Tab("Terminal") {

    init {
        graphic = AssetFactory.loadIcon("Command-Line.png")

        val textArea = textarea {
            text = SimpleDateFormat(
                "HH:mm:ss, MM dd, yyyy",
                Locale("en", "US")
            ).format(Date()) + "\n"
        }

        // Redirect output to console
        PrintStream(
            TextAreaPrintStream(
                textArea
            ), true, "UTF-8").let {
            System.setOut(it)
            System.setErr(it)
        }
    }

    // Simple stream to append input characters to a text area
    private class TextAreaPrintStream(private val textArea: TextArea) : OutputStream() {

        override fun write(character: Int) {
            runLater {
                textArea.appendText(character.toChar().toString())
            }
        }
    }
}
