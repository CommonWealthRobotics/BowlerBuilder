/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.creatureeditor.configuration

import com.neuronrobotics.bowlerbuilder.model.LimbData
import tornadofx.*

class NewLimbInput : View("New Limb") {

    val limbData: LimbData by inject()

    override val root = form {
        fieldset {
            field("Limb Name") {
                textfield(limbData.nameProperty)
            }
        }
    }
}
