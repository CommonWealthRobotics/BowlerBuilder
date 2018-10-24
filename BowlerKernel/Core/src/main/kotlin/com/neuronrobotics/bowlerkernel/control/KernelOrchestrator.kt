/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control

/**
 * The high-level orchestrator which controls various large lifecycle components.
 */
class KernelOrchestrator {

    /**
     * Starts a [ControlScript].
     */
    fun startControlScript(controlScript: ControlScript) {
        controlScript.start()
    }

    /**
     * Stops a [ControlScript].
     */
    fun stopControlScript(controlScript: ControlScript) {
        controlScript.stopAndCleanUp()
    }
}