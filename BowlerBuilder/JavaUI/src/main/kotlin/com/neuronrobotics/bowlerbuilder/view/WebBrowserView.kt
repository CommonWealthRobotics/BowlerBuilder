package com.neuronrobotics.bowlerbuilder.view

import com.neuronrobotics.bowlerbuilder.controller.WebBrowserControllerNew
import com.neuronrobotics.bowlerbuilder.model.WebBrowserScript
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.concurrent.Worker
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.web.WebView
import kotlinx.coroutines.runBlocking
import org.controlsfx.glyphfont.Glyph
import tornadofx.*

class WebBrowserView : Fragment() {

    private val controller: WebBrowserControllerNew by inject()
    private val currentUrlProperty = SimpleStringProperty(config.string(HOME_PAGE))
    private var currentUrl by currentUrlProperty
    private val currentScriptSelection =
        SimpleObjectProperty<WebBrowserScript>(WebBrowserScript.empty)
    private var currentScript by currentScriptSelection
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

                button(graphic = Glyph("FontAwesome", "ARROW_LEFT")) {
                    action { webview.engine.executeScript("history.back();") }
                }

                button(graphic = Glyph("FontAwesome", "ARROW_RIGHT")) {
                    action { webview.engine.executeScript("history.forward();") }
                }

                button(graphic = Glyph("FontAwesome", "REFRESH")) {
                    action { webview.engine.reload() }
                }

                button(graphic = Glyph("FontAwesome", "HOME")) {
                    action { webview.engine.load(currentUrl) }
                }
            }

            textfield {
                textProperty().bindBidirectional(currentUrlProperty)

                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }

                action {
                    currentUrl = text.let {
                        // Format the url to have an "https://" prefix
                        if (it.startsWith("http://") || it.startsWith("https://")) {
                            it
                        } else {
                            "https://$it"
                        }
                    }

                    webview.engine.load(currentUrl)
                }
            }

            button(graphic = Glyph("FontAwesome", "STAR")) {
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

            button(text = "Run", graphic = AssetFactory.loadIcon("Run.png")) {
                action { runAsync { controller.runScript(currentScript) } }
            }

            val cloneButton = button(
                text = "Clone",
                graphic = AssetFactory.loadIcon("Make-Copy-Script.png")
            ) {
                action {
                    runAsync {
                        controller.cloneScript(currentScript)
                    } success {
                        modifyIntoEditButton()
                    }
                }
            }.apply {
                runAsync {
                    controller.doesUserOwnScript(currentScript)
                } success {
                    if (it) {
                        modifyIntoEditButton()
                    }
                }
            }

            fun refreshCloneButton() {
                runAsync {
                    controller.doesUserOwnScript(currentScript)
                } success {
                    if (it) {
                        cloneButton.modifyIntoEditButton()
                    } else {
                        cloneButton.modifyIntoCloneButton()
                    }
                }
            }

            combobox<WebBrowserScript> {
                cellFormat {
                    text = it.filename
                }

                currentScriptSelection.bind(valueProperty())
                currentScriptSelection.addListener { _, _, new ->
                    refreshCloneButton()
                }
                controller.itemsOnPageProperty.addListener(ListChangeListener {
                    Platform.runLater { items.setAll(it.list) }
                })
            }
        }
    }

    /**
     * Modify a button into a clone button.
     */
    private fun Button.modifyIntoCloneButton() {
        text = "Clone"
        graphic = AssetFactory.loadIcon("Make-Copy-Script.png")
        action {
            runAsync { controller.cloneScript(currentScript) }
        }
    }

    /**
     * Modify a button into an edit button.
     */
    private fun Button.modifyIntoEditButton() {
        text = "Edit"
        graphic = AssetFactory.loadIcon("Edit-Script.png")
        action {
            runAsync { controller.editScript(currentScript) }
        }
    }

    init {
        webview.engine.load(currentUrl)
    }

    companion object {
        const val HOME_PAGE = "webview_home_page"
    }
}
