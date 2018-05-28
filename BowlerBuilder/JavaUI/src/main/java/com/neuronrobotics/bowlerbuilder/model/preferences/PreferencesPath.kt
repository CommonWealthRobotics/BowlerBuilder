package com.neuronrobotics.bowlerbuilder.model.preferences

import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import java.io.File

object PreferencesPath {
    val PATH by lazy {
        "${LoggerUtilities.getBowlerDirectory()}${File.separator}preferences${File.separator}"
    }
}
