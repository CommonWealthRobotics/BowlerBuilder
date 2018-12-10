/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceEditorView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceScratchpadView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import javafx.scene.control.Tab
import tornadofx.*

class AceScriptEditorFactory : ScriptEditorFactory {

    override fun createAndOpenScriptEditor(gitUrl: String, filename: String): ScriptEditor {
        return FxUtil.returnFX {
            val editor = find<AceEditorView>(
                params = mapOf("git_url" to gitUrl, "filename" to filename)
            )

            find<MainWindowView>().addTab(Tab(filename, editor.root))

            editor
        }.also {
            it.engineInitializingLatch.await()
        }
    }

    override fun createAndOpenScratchpad(): ScriptEditor {
        return FxUtil.returnFX {
            val editor = find<AceScratchpadView>()
            find<MainWindowView>().addTab(Tab("Scratchpad", editor.root))
            editor
        }.also {
            it.engineInitializingLatch.await()
        }
    }
}
