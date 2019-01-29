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
package com.neuronrobotics.bowlerbuilder.view.gitmenu

import com.neuronrobotics.bowlerbuilder.controller.gitmenu.GistFileSelectionController
import com.neuronrobotics.bowlerbuilder.controller.main.MainWindowController.Companion.getInstanceOf
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Orientation
import tornadofx.*
import java.io.File

/**
 * A form to select a file in a gist via url.
 */
class GistFileSelectionView : Fragment() {

    private val controller = getInstanceOf<GistFileSelectionController>()
    private val gistUrlProperty = SimpleStringProperty("")
    private var gistUrl by gistUrlProperty
    private val gistFileSelectionProperty = SimpleObjectProperty<File>()
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
                button("Load").action {
                    runAsync {
                        controller.openGistFile(gistUrl, gistFileSelection)
                    }
                    close()
                }

                button("Cancel").action { close() }
            }
        }
    }

    init {
        gistUrlProperty.addListener { _, _, new ->
            runAsync { controller.loadFilesInGist(new) }
        }
    }

    companion object {
        fun create() = GistFileSelectionView()
    }
}
