/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.model.GistFile
import com.neuronrobotics.bowlerbuilder.view.AddTabEvent
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceEditorView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceScratchpadView
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditor
import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditorTab
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil

class AceCadScriptEditorFactory : CadScriptEditorFactory {

    override fun createAndOpenScriptEditor(gistFile: GistFile): CadScriptEditor {
        return FxUtil.returnFX {
            val editor = AceEditorView.create(gistFile)
            val cadEditor = CadScriptEditor(editor)
            MainWindowView.mainUIEventBus.post(AddTabEvent(CadScriptEditorTab(gistFile.filename, editor)))
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
            MainWindowView.mainUIEventBus.post(AddTabEvent(CadScriptEditorTab("Scratchpad", scratchpad)))
            cadEditor to scratchpad
        }.let {
            // Wait for the engine to finish loading the editor before returning
            it.second.engineInitializingLatch.await()
            it.first
        }
    }
}
