/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.creatureeditor.configuration

import com.neuronrobotics.bowlerbuilder.model.LimbData
import tornadofx.*

class AddLimbWizard : Wizard("Add a limb", "Add a new limb to the device") {

    val limbData: LimbData by inject()

    init {
        add(
            NewLimbInput::class
        )

        add(
            NewLinkInput::class
        )
    }
}
