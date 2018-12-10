package com.neuronrobotics.bowlerbuilder.controller.scripteditor

import tornadofx.*

class AceWebEditorController : Controller() {

    /**
     * Escape text so it gets inserted properly.
     *
     * @param text Text to escape
     * @return Escaped version
     */
    fun escape(text: String): String {
        var escaped = text
        escaped = escaped.replace("\"", "\\\"")
        escaped = escaped.replace("'", "\\'")
        escaped = escaped.replace(System.getProperty("line.separator"), "\\n")
        escaped = escaped.replace("\n", "\\n")
        escaped = escaped.replace("\r", "\\n")
        return escaped
    }
}
