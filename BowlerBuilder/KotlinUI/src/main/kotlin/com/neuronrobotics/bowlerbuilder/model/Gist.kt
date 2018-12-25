/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model

import arrow.optics.optics

@optics
data class Gist(
    val gitUrl: String,
    val id: Long,
    val description: String
) {
    companion object
}
