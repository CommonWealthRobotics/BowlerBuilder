/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.cad

import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import eu.mihosoft.vrl.v3d.CSG
import java.util.Locale

class CsgParser {

    /**
     * Find CSG objects from the source code.
     *
     * @param scriptName name of CSG source script
     * @param lineNumber line number in script
     * @param csgs the possible CSG objects
     * @return CSG objects from the script
     */
    fun parseCsgFromSource(
        scriptName: String,
        lineNumber: Int,
        csgs: ImmutableCollection<CSG>
    ): ImmutableList<CSG> {
        val csgsFromScriptLine = mutableListOf<CSG>()

        csgs.forEach { testCSG ->
            testCSG.creationEventStackTraceList
                    .map { it.split(":") }
                    .filter {
                        it[0].trim()
                                .toLowerCase()
                                .contains(scriptName.toLowerCase(Locale.US).trim())
                    }
                    .forEach {
                        try {
                            val num = Integer.parseInt(it[1].trim())
                            if (num == lineNumber) {
                                csgsFromScriptLine.add(testCSG)
                            }
                        } catch (ignored: NumberFormatException) {
                        }
                    }
        }

        return ImmutableList.copyOf(csgsFromScriptLine)
    }
}
