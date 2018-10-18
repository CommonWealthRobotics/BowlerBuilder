package com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.unprovisioned

import arrow.core.Either
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.DeviceResource
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.ProvisionError
import com.neuronrobotics.bowlerkernel.control.hardware.deviceresource.provisioned.ProvisionedDeviceResource

/**
 * A [DeviceResource] which has been registered but not provisioned.
 */
interface UnprovisionedDeviceResource : DeviceResource {

    /**
     * Provisions this [DeviceResource] by communicating with the parent device to set up any
     * hardware-local requirements. For example, this could initialize GPIO pins or register
     * interrupt handlers.
     *
     * @return A [ProvisionedDeviceResource] on success, a [ProvisionError] on failure.
     */
    fun provision(): Either<ProvisionError, ProvisionedDeviceResource>
}
