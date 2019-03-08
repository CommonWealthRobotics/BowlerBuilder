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
package com.neuronrobotics.bowlerbuilder.view.webbrowser

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.webbrowser.WebBrowserController
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerbuilder.view.util.getFontAwesomeGlyph
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*
import kotlin.collections.set

/**
 * A view which has a web browser and script run/clone/edit controls.
 */
class WebBrowserView(
    private val controller: WebBrowserController,
    urlToLoad: String?
) : Fragment() {

    private val currentUrlProperty = SimpleStringProperty(
        urlToLoad ?: config.string(HOME_PAGE, DEFAULT_HOME_PAGE)
    )
    private var currentUrl by currentUrlProperty
    private val currentScriptProperty = SimpleObjectProperty<WebBrowserScript>(
        WebBrowserScript.empty
    )
    private var currentScript by currentScriptProperty
    private var webview: WebView by singleAssign()
    private val userOwnsCurrentScriptProperty = SimpleBooleanProperty()
    private var userOwnsCurrentScript by userOwnsCurrentScriptProperty

    override val root = borderpane {
        webview = webview {
            engine.locationProperty().addListener { _, _, new -> currentUrl = new }

            engine.loadWorker.stateProperty().addListener { _, _, new ->
                if (new == Worker.State.SUCCEEDED) {
                    runAsync {
                        controller.loadItemsOnPage(currentUrl, engine)
                    }
                }
            }
        }

        top = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            useMaxWidth = true

            hbox {
                spacing = 5.0

                button(graphic = getFontAwesomeGlyph(FontAwesome.Glyph.ARROW_LEFT)) {
                    action { webview.engine.executeScript("history.back();") }
                }

                button(graphic = getFontAwesomeGlyph(FontAwesome.Glyph.ARROW_RIGHT)) {
                    action { webview.engine.executeScript("history.forward();") }
                }

                button(graphic = getFontAwesomeGlyph(FontAwesome.Glyph.REFRESH)) {
                    action { webview.engine.reload() }
                }

                button(graphic = getFontAwesomeGlyph(FontAwesome.Glyph.HOME)) {
                    action { webview.engine.load(config.string(HOME_PAGE)) }
                }
            }

            textfield {
                textProperty().bindBidirectional(currentUrlProperty)

                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }

                action {
                    currentUrl = controller.formatUrl(text)
                    webview.engine.load(currentUrl)
                }
            }

            button(graphic = getFontAwesomeGlyph(FontAwesome.Glyph.STAR)) {
                tooltip("Set Home Page")

                action {
                    config[HOME_PAGE] = currentUrl
                    config.save()
                }
            }
        }

        center = webview

        bottom = hbox {
            padding = Insets(5.0)
            spacing = 5.0
            alignment = Pos.CENTER_LEFT

            add(
                ThreadMonitoringButton(
                    "Run" to loadImageAsset("Run.png", FontAwesome.Glyph.PLAY),
                    "Stop" to loadImageAsset("Stop.png", FontAwesome.Glyph.STOP),
                    { controller.runScript(currentScript) },
                    { controller.stopScript() }
                )
            )

            button(
                "Fork",
                loadImageAsset("Edit-Script.png", FontAwesome.Glyph.EDIT)
            ) {
                action { runAsync { controller.editScript(currentScript) } }
            }

            combobox<WebBrowserScript> {
                cellFormat {
                    text = it.gistFile.file.name
                }

                valueProperty().addListener { _, _, new ->
                    currentScript = new ?: WebBrowserScript.empty
                    userOwnsCurrentScript = getInstanceOf<MainWindowController>().gitFS.fold(
                        { false },
                        { gitFS ->
                            gitFS.isOwner(currentScript.gistFile.gist.gitUrl).fold(
                                { false },
                                { it }
                            )
                        }
                    )
                }

                controller.itemsOnPageProperty.addListener(ListChangeListener {
                    runLater {
                        items.setAll(it.list)
                        if (items.size > 0) {
                            value = items[0]
                        }
                    }
                })
            }
        }
    }

    init {
        webview.engine.load(currentUrl)
    }

    companion object {
        const val HOME_PAGE = "webview_home_page"
        const val DEFAULT_HOME_PAGE =
            "http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/"

        fun create(urlToLoad: String? = null) = WebBrowserView(
            getInstanceOf(),
            urlToLoad
        )
    }
}
