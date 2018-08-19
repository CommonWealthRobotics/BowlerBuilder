package com.neuronrobotics.bowlerbuilder.controller;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.OutputStream;

// Simple stream to append input characters to a text area
@ParametersAreNonnullByDefault
class TextAreaPrintStream extends OutputStream {

    private final TextArea textArea;

    TextAreaPrintStream(final TextArea textArea) {
        super();
        this.textArea = textArea;
    }

    @Override
    public void write(final int character) {
        Platform.runLater(() -> textArea.appendText(String.valueOf((char) character)));
    }
}
