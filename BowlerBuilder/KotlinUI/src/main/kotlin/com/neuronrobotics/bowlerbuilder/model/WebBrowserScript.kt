/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model

data class WebBrowserScript(
    val pageUrl: String,
    val gitUrl: String,
    val filename: String
) {
    companion object {
        val empty = WebBrowserScript("", "", "")
    }
}
