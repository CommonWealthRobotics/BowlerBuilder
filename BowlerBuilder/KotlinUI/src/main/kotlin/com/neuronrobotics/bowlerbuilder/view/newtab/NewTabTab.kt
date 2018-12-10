package com.neuronrobotics.bowlerbuilder.view.newtab

import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import javafx.scene.control.Tab
import org.controlsfx.glyphfont.Glyph
import tornadofx.*

class NewTabTab : Tab("New...") {

    init {
        graphic = Glyph("FontAwesome", "PLUS")
        content = FxUtil.returnFX { find<NewTabView>() }.root
    }
}
