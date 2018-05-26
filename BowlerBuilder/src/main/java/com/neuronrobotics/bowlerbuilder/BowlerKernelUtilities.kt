/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder

import com.google.common.collect.ImmutableSet
import com.neuronrobotics.sdk.addons.kinematics.MobileBase

object BowlerKernelUtilities {

    /**
     * Calculate the taken ("occupied") hardware channels on the device based on the config saved to
     * the device.
     *
     * @param device device to check
     * @return all taken channels
     */
    @JvmStatic
    fun getTakenHardwareChannels(device: MobileBase): ImmutableSet<Int> =
            ImmutableSet.copyOf(device
                    .allDHChains
                    .flatMap { it.linkConfigurations }
                    .map { it.hardwareIndex }
                    .toSet())
}
