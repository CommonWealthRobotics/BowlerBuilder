/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
