/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
