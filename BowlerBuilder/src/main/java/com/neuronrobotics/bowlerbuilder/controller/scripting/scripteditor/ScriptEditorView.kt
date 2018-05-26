/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor

import javafx.scene.Node

interface ScriptEditorView {

    /**
     * Get the view for editing the script.
     *
     * @return editor view
     */
    fun getView(): Node

    /**
     * Get the [ScriptEditor] this view interacts with.
     *
     * @return the script editor
     */
    fun getScriptEditor(): ScriptEditor
}
