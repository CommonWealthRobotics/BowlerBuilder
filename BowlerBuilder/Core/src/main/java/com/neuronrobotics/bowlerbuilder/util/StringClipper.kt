/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.util

class StringClipper {

    /**
     * Clip a string to a maximum number of lines.
     * @param input input string
     * @param lines max line count
     * @return clipped string
     */
    fun clipStringToLines(input: String, lines: Int): String {
        val allLines = input.split("\n", "\r")
        val upperBound = Math.min(lines, allLines.size)
        val out = StringBuilder()

        for (i in 0 until upperBound) {
            val line = allLines[i]
            out.append(line)
            if (i < upperBound - 1) {
                out.append('\n')
            }
        }

        return out.toString()
    }
}
