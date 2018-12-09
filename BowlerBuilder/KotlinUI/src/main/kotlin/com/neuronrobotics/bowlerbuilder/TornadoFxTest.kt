package com.neuronrobotics.bowlerbuilder

import com.neuronrobotics.bowlerbuilder.view.WebBrowserView
import tornadofx.*

fun main(args: Array<String>) {
    launch<TornadoFxTest>(args)
}

class TornadoFxTest : App(WebBrowserView::class)
