/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.webbrowser

import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import com.neuronrobotics.bowlerbuilder.controller.webbrowser.WebBrowserController
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerbuilder.view.util.getFontAwesomeGlyph
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*

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
    private var cloneButton: Button by singleAssign()

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

            add(
                ThreadMonitoringButton.create(
                    "Run" to loadImageAsset("Run.png", FontAwesome.Glyph.PLAY),
                    "Stop" to loadImageAsset("Stop.png", FontAwesome.Glyph.STOP)
                ) {
                    controller.runScript(currentScript)
                }
            )

            cloneButton = button()
                .modifyIntoCloneButton(WebBrowserScript.empty)
                .apply {
                    isDisable = true
                }

            combobox<WebBrowserScript> {
                cellFormat {
                    text = it.gistFile.file.name
                }

                valueProperty().addListener { _, _, new ->
                    refreshCloneButton(new)
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
        refreshCloneButton(controller.itemsOnPageProperty.firstOrNull())
    }

    private fun refreshCloneButton(new: WebBrowserScript?) {
        val nonNullValue = new ?: WebBrowserScript.empty
        currentScript = nonNullValue

        runAsync {
            controller.doesUserOwnScript(nonNullValue)
        } success {
            it.map {
                if (it) {
                    cloneButton.modifyIntoEditButton(nonNullValue)
                } else {
                    cloneButton.modifyIntoCloneButton(nonNullValue)
                }
            }
        }
    }

    /**
     * Modify a button into a clone button.
     */
    private fun Button.modifyIntoCloneButton(script: WebBrowserScript): Button {
        text = "Clone"
        graphic = loadImageAsset("Make-Copy-Script.png", FontAwesome.Glyph.COPY)
        isDisable = false
        action { runAsync { controller.forkScript(script) } }
        return this
    }

    /**
     * Modify a button into an edit button.
     */
    private fun Button.modifyIntoEditButton(script: WebBrowserScript): Button {
        text = "Edit"
        graphic = loadImageAsset("Edit-Script.png", FontAwesome.Glyph.EDIT)
        isDisable = false
        action { runAsync { controller.editScript(script) } }
        return this
    }

    init {
        webview.engine.load(currentUrl)
    }

    companion object {
        const val HOME_PAGE = "webview_home_page"
        const val DEFAULT_HOME_PAGE =
            "http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/"

        fun create(urlToLoad: String? = null) = WebBrowserView(
            getInstanceOf<WebBrowserController>(),
            urlToLoad
        )
    }
}
