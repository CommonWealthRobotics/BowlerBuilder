/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model.preferences

import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import java.io.File

object PreferencesPath {
    val PATH by lazy {
        "${LoggerUtilities.getBowlerDirectory()}${File.separator}preferences${File.separator}"
    }
}
