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
