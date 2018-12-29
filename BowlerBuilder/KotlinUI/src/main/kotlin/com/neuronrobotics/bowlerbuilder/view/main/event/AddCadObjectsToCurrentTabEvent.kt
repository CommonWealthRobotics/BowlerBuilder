/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.main.event

import com.google.common.collect.ImmutableSet
import eu.mihosoft.vrl.v3d.CSG

/**
 * Adds [cad] to the current tab's CAD viewer. If there is no CAD viewer open, then a new one is
 * opened.
 */
data class AddCadObjectsToCurrentTabEvent(
    val cad: ImmutableSet<CSG>
)
