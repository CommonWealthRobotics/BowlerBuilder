/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace

import javafx.concurrent.Worker

/** Adapter to a [javafx.scene.web.WebEngine] since that class is final.  */
interface WebEngineAdapter {

    /**
     * Execute a script and return the result.
     *
     * @param script script code
     * @return result
     */
    fun executeScript(script: String): Any?

    /**
     * Get the load worker.
     *
     * @return load worker
     */
    fun getLoadWorker(): Worker<Void>
}
