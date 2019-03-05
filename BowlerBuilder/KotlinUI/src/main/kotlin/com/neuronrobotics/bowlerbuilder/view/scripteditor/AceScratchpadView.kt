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
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.TextScriptRunner
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.VisualScriptEditor
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.CadScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.view.gitmenu.PublishNewGistView
import com.neuronrobotics.bowlerbuilder.view.main.event.CloseTabByContentEvent
import com.neuronrobotics.bowlerbuilder.view.util.FxUtil
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import org.controlsfx.glyphfont.FontAwesome
import org.octogonapus.guavautil.collections.immutableListOf
import tornadofx.*
import javax.inject.Inject

/**
 * An editor which operates entirely in memory and contains the controls to run the script and
 * create a new gist with the editor contents.
 */
class AceScratchpadView
@Inject constructor(
    private val editor: AceWebEditorView,
    private val controller: TextScriptRunner,
    private val cadScriptEditorFactory: CadScriptEditorFactory
) : Fragment(), ScriptEditor by editor, VisualScriptEditor {

    private val languageChoiceProperty = SimpleStringProperty("Groovy")
    private var languageChoice by languageChoiceProperty

    val engineInitializingLatch
        get() = editor.engineInitializingLatch

    @SuppressWarnings("LabeledExpression")
    override val root = borderpane {
        id = "AceScratchpadView"
        center = editor.root

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true
            alignment = Pos.CENTER_LEFT

            add(
                ThreadMonitoringButton.create(
                    "Run" to loadImageAsset("Run.png", FontAwesome.Glyph.PLAY),
                    "Stop" to loadImageAsset("Stop.png", FontAwesome.Glyph.STOP)
                ) {
                    controller.runScript(FxUtil.returnFX { getFullText() }, languageChoice)
                }
            )

            choicebox(languageChoiceProperty, immutableListOf("Groovy", "Kotlin"))

            button("Publish", loadImageAsset("Publish.png", FontAwesome.Glyph.CLOUD_UPLOAD)) {
                action {
                    val view = PublishNewGistView(getFullText()).apply {
                        openModal(block = true)
                    }

                    if (view.publishSuccessful) {
                        runAsync {
                            cadScriptEditorFactory.createAndOpenScriptEditor(
                                view.gitUrl, view.publishedFile
                            )
                        }

                        MainWindowController.mainUIEventBus.post(
                            CloseTabByContentEvent(this@borderpane)
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun create() = AceScratchpadView(
            FxUtil.returnFX { AceWebEditorView() },
            getInstanceOf(),
            getInstanceOf()
        )
    }
}
