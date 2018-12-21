/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.webbrowser

import com.neuronrobotics.bowlerbuilder.controller.webbrowser.WebBrowserController
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerbuilder.view.main.MainWindowView
import com.neuronrobotics.bowlerbuilder.view.util.ThreadMonitoringButton
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import org.controlsfx.glyphfont.Glyph
import org.jlleitschuh.guice.key
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

    private val currentScriptProperty =
        SimpleObjectProperty<WebBrowserScript>(WebBrowserScript.empty)
    private var currentScript by currentScriptProperty

    private var webview: WebView by singleAssign()

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

                button(graphic = Glyph(FONT_AWESOME, "ARROW_LEFT")) {
                    action { webview.engine.executeScript("history.back();") }
                }

                button(graphic = Glyph(FONT_AWESOME, "ARROW_RIGHT")) {
                    action { webview.engine.executeScript("history.forward();") }
                }

                button(graphic = Glyph(FONT_AWESOME, "REFRESH")) {
                    action { webview.engine.reload() }
                }

                button(graphic = Glyph(FONT_AWESOME, "HOME")) {
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

            button(graphic = Glyph(FONT_AWESOME, "STAR")) {
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
                    "Run" to AssetFactory.loadIcon("Run.png"),
                    "Stop" to AssetFactory.loadIcon("Stop.png")
                ) {
                    controller.runScript(currentScript)
                }
            )

            val cloneButton = button()
                .modifyIntoCloneButton(WebBrowserScript.empty)
                .apply {
                    isDisable = true
                }

            combobox<WebBrowserScript> {
                cellFormat {
                    text = it.gistFile.filename
                }

                valueProperty().addListener { _, _, new ->
                    val nonNullValue = new ?: WebBrowserScript.empty
                    currentScript = nonNullValue

                    runAsync {
                        controller.doesUserOwnScript(nonNullValue)
                    } success {
                        if (it) {
                            cloneButton.modifyIntoEditButton(nonNullValue)
                        } else {
                            cloneButton.modifyIntoCloneButton(nonNullValue)
                        }
                    }
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

    /**
     * Modify a button into a clone button.
     */
    private fun Button.modifyIntoCloneButton(script: WebBrowserScript): Button {
        text = "Clone"
        graphic = AssetFactory.loadIcon("Make-Copy-Script.png")
        isDisable = false
        action { runAsync { controller.cloneScript(script) } }
        return this
    }

    /**
     * Modify a button into an edit button.
     */
    private fun Button.modifyIntoEditButton(script: WebBrowserScript): Button {
        text = "Edit"
        graphic = AssetFactory.loadIcon("Edit-Script.png")
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
        private const val FONT_AWESOME = "FontAwesome"

        fun create(urlToLoad: String? = null) = WebBrowserView(
            MainWindowView.injector.getInstance(key<WebBrowserController>()),
            urlToLoad
        )
    }
}
