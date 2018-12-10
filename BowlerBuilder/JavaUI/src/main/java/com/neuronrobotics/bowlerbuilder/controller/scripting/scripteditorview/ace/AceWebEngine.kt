/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace

import javafx.concurrent.Worker
import javafx.scene.web.WebEngine

/**
 * A simple passthrough to the real [WebEngine].
 */
class AceWebEngine(private val webEngine: WebEngine) : WebEngineAdapter {

    override fun executeScript(script: String): Any? =
            webEngine.executeScript(script)

    override fun getLoadWorker(): Worker<Void> =
            webEngine.loadWorker
}
