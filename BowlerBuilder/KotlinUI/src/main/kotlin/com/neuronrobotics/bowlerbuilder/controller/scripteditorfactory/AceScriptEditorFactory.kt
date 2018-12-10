package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.view.scripteditor.AceEditorView
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import tornadofx.*

class AceScriptEditorFactory : ScriptEditorFactory {

    override fun createAndOpenScriptEditor(gitUrl: String, filename: String): ScriptEditor {
        return FxUtil.returnFX {
            find<AceEditorView>(
                params = mapOf("git_url" to gitUrl, "filename" to filename)
            ).also {
                it.openWindow()
            }
        }.also {
            it.engineInitializingLatch.await()
        }
    }
}
