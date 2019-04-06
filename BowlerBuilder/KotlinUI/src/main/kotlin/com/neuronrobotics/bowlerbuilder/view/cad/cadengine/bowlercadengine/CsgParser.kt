/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

import arrow.core.Try
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
                    Try { it[1].trim().toInt() }.map {
                        if (it == lineNumber) {
                            csgsFromScriptLine.add(testCSG)
                        }
                    }
                }
        }

        return ImmutableList.copyOf(csgsFromScriptLine)
    }
}
