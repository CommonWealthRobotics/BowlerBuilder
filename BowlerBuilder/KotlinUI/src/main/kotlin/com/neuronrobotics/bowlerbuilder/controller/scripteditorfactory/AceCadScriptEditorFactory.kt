/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
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
            MainWindowController.mainUIEventBus.post(
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
            MainWindowController.mainUIEventBus.post(
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
