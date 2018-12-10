/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model

import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR
import com.neuronrobotics.sdk.addons.kinematics.DHLink
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration

data class LinkData(
    val parentLimb: DHParameterKinematics,
    val index: Int,
    val dhLink: DHLink,
    val linkConfiguration: LinkConfiguration,
    val device: AbstractKinematicsNR
)
