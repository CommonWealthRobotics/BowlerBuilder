/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.controller.util.LoggerUtilities
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.scene.control.Tab
import javafx.scene.control.TextArea
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*
import java.io.OutputStream
import java.io.PrintStream

/**
 * A [Tab] containing a console to view logging statements and errors.
 */
class ConsoleTab : Tab("Terminal") {

    init {
        graphic = loadImageAsset("Command-Line.png", FontAwesome.Glyph.TERMINAL)

        // Redirect output to console
        PrintStream(
            TextAreaPrintStream(
                textarea {
                    // Synchronize the output with the log file so far
                    text = LoggerUtilities.readCurrentLogFile()
                }
            ), true, "UTF-8"
        ).let {
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
