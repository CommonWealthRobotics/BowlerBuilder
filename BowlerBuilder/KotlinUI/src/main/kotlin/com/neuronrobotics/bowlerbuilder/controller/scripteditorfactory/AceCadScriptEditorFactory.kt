/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.main.event.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceEditorView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceScratchpadView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditor
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditorTab
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import java.io.File

class AceCadScriptEditorFactory : CadScriptEditorFactory {

    override fun createAndOpenScriptEditor(url: String, file: File): CadScriptEditor {
        return FxUtil.returnFX {
            val editor = AceEditorView.create(url, file)
            val cadEditor = CadScriptEditor(editor)
            MainWindowView.mainUIEventBus.post(
                AddTabEvent(
                    CadScriptEditorTab(file.name, editor)
                )
            )
            cadEditor to editor
        }.let {
            // Wait for the engine to finish loading the editor before returning
            it.second.engineInitializingLatch.await()
            it.first
        }
    }

    override fun createAndOpenScratchpad(): CadScriptEditor {
        return FxUtil.returnFX {
            val scratchpad = AceScratchpadView.create()
            val cadEditor = CadScriptEditor(scratchpad)
            MainWindowView.mainUIEventBus.post(
                AddTabEvent(
                    CadScriptEditorTab("Scratchpad", scratchpad)
                )
            )
            cadEditor to scratchpad
        }.let {
            // Wait for the engine to finish loading the editor before returning
            it.second.engineInitializingLatch.await()
            it.first
        }
    }
}
