/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog

import com.neuronrobotics.bowlerbuilder.GistUtilities
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.ComboBox
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.util.Callback
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.Callable
import java.util.function.Function
import java.util.function.Predicate
import kotlinx.coroutines.experimental.javafx.JavaFx

/**
 * A [Dialog] to select files from a GitHub Gist.
 *
 * @param title the dialog title
 * @param extensionFilter file extension filter
 */
class GistFileSelectionDialog(
    title: String,
    extensionFilter: Predicate<String>
) : Dialog<Array<String>>() {

    private val gistField: ValidatedTextField =
            ValidatedTextField(
                    "Invalid Gist URL",
                    Function { GistUtilities.isValidGitURL(it).isPresent })

    private val fileChooser: ComboBox<String> = ComboBox()

    init {
        fileChooser.id = "gistFileChooser"
        fileChooser.disableProperty().bind(gistField.invalidProperty())

        gistField.id = "gistField"
        gistField
                .invalidProperty()
                .addListener { _, _, newValue ->
                    launch {
                        if (!newValue) {
                            val files = ScriptingEngine
                                    .filesInGit(gistField.text)
                                    .filter { extensionFilter.test(it) }
                                    .toSet()

                            launch(context = JavaFx) {
                                fileChooser.items = FXCollections.observableArrayList(files)
                            }
                        }
                    }
                }

        setTitle(title)

        val pane = GridPane()
        pane.id = "root"
        pane.alignment = Pos.CENTER
        pane.hgap = 5.0
        pane.vgap = 5.0

        pane.add(Label("Gist URL"), 0, 0)
        pane.add(gistField, 1, 0)
        pane.add(Label("File name"), 0, 1)
        pane.add(fileChooser, 1, 1)

        dialogPane.content = pane
        dialogPane.minWidth = 300.0
        isResizable = true
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        launch(context = JavaFx) {
            gistField.requestFocus()
        }

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.isDefaultButton = true
        okButton.disableProperty().bind(gistField.invalidProperty())
        okButton
                .disableProperty()
                .bind(
                        Bindings.createBooleanBinding(
                                Callable {
                                    fileChooser.selectionModel.selectedItem == null ||
                                            gistField.text.isEmpty()
                                },
                                gistField.textProperty(),
                                fileChooser.selectionModel.selectedItemProperty()
                        )
                )

        resultConverter = Callback {
            if (it == ButtonType.OK) {
                arrayOf(gistField.text, fileChooser.selectionModel.selectedItem)
            } else {
                null
            }
        }
    }
}
