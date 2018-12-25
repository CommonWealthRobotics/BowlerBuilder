/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.newtab

import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.getFontAwesomeGlyph
import javafx.scene.control.Tab
import org.controlsfx.glyphfont.FontAwesome

class NewTabTab : Tab("New...") {

    init {
        graphic = getFontAwesomeGlyph(FontAwesome.Glyph.PLUS)
        content = FxUtil.returnFX { NewTabView.create() }.root
    }
}
