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
package com.neuronrobotics.bowlerbuilder.model

import arrow.optics.optics
import java.io.File

@optics
data class GistFileOnDisk(
    val gist: Gist,
    val file: File
) {
    companion object {
        // This is not a secondary constructor because of https://github.com/arrow-kt/arrow/issues/1211
        fun create(gitUrl: String, file: File) = GistFileOnDisk(Gist(gitUrl, "", ""), file)
    }
}
