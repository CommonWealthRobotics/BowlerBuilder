/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.main.event

import com.google.common.collect.ImmutableSet
import eu.mihosoft.vrl.v3d.CSG

/**
 * Clears the CAD objects from the current tab's CAD viewer and add [cad]. If there is no CAD
 * viewer open, then a new one is opened.
 */
data class SetCadObjectsToCurrentTabEvent(
    val cad: ImmutableSet<CSG>
)
