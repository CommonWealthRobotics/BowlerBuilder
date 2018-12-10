/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.gitmenu.GistFileSelectionController
import com.neuronrobotics.bowlerbuilder.model.GistFile
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Orientation
import tornadofx.*

class GistFileSelectionView : Fragment() {

    private val controller: GistFileSelectionController by inject()
    private val gistUrlProperty = SimpleStringProperty("")
    private var gistUrl by gistUrlProperty
    private val gistFileSelectionProperty = SimpleStringProperty("")
    private var gistFileSelection by gistFileSelectionProperty

    override val root = form {
        fieldset {
            field("Gist URL", Orientation.VERTICAL) {
                textfield(gistUrlProperty)
            }

            field("File") {
                combobox(gistFileSelectionProperty) {
                    controller.filesInGist.addListener(ListChangeListener {
                        runLater {
                            items.setAll(it.list)
                            if (items.size > 0) {
                                value = items[0]
                            }
                        }
                    })
                }
            }

            buttonbar {
                button("Load") {
                    action {
                        runAsync {
                            controller.openGistFile(GistFile(gistUrl, gistFileSelection))
                        }
                        close()
                    }
                }

                button("Cancel") {
                    action { close() }
                }
            }
        }
    }

    init {
        gistUrlProperty.addListener { _, _, new ->
            runAsync { controller.loadFilesInGist(new) }
        }
    }
}
