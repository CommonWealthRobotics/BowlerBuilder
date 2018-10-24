/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.hardware.device

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.device.deviceid.DeviceId
import com.neuronrobotics.bowlerkernel.control.hardware.protocol.BowlerRPCProtocol
import com.neuronrobotics.bowlerkernel.control.hardware.registry.HardwareRegistry
import com.neuronrobotics.bowlerkernel.control.hardware.registry.RegisterError
import org.jlleitschuh.guice.module
import javax.inject.Inject

/**
 * A facade for making any type of device.
 */
class DeviceFactory
@Inject internal constructor(
    private val registry: HardwareRegistry
) : BowlerDeviceFactory {

    private fun registerDevice(deviceId: DeviceId) = registry.registerDevice(deviceId)

    override fun makeBowlerDevice(
        deviceId: DeviceId,
        bowlerRPCProtocol: BowlerRPCProtocol
    ): Either<RegisterError, BowlerDevice> =
        registerDevice(deviceId).toEither { BowlerDevice(deviceId, bowlerRPCProtocol) }.swap()

    companion object {

        internal fun deviceFactoryModule() = module {
            bind<BowlerDeviceFactory>().to<DeviceFactory>()
        }
    }
}