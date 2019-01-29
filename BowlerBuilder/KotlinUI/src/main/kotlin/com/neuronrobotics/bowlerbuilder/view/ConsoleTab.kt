/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
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
