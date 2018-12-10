package com.neuronrobotics.bowlerbuilder.view.webbrowser

import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import javafx.scene.control.Tab
import tornadofx.*

class WebBrowserTab : Tab("Web") {

    init {
        content = FxUtil.returnFX { find<WebBrowserView>() }.root
    }
}
