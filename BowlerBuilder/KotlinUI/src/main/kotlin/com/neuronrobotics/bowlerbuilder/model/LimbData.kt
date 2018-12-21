/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class LimbData : Component(), ScopedInstance {
    val nameProperty = SimpleStringProperty()
    var name by nameProperty

    override fun toString() =
        """
        |Name: $name
        """.trimMargin()
}
