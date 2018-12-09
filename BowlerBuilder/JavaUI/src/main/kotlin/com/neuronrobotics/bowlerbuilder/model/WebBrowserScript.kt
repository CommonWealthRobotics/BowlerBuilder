package com.neuronrobotics.bowlerbuilder.model

data class WebBrowserScript(
    val pageUrl: String,
    val gitUrl: String,
    val filename: String
) {
    companion object {
        val empty = WebBrowserScript("", "", "")
    }
}
