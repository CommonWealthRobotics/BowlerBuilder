/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog.preferences

import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.VBox
import org.controlsfx.control.PropertySheet
import org.controlsfx.property.BeanPropertyUtils

/**
 * Dialog to show user preferences from a [PropertySheet].
 * Based on GRIP. See third-party-licenses/GRIP.txt.
 *
 * @param preferencesServices the preferences to edit
 */
class PreferencesDialog(preferencesServices: List<PreferencesService<out Preferences>>) :
        Dialog<List<Preferences>>() {
    init {
        val preferences = preferencesServices.map { it.getCurrentPreferencesOrDefault() }

        val nodes = mutableListOf<Node>()
        preferences
                .map { item -> CustomPropertySheet(BeanPropertyUtils.getProperties(item)) }
                .zip(preferencesServices)
                .forEach { (sheet, preferencesService) ->
                    nodes.add(Label(preferencesService.getName()))
                    nodes.add(sheet)
                    nodes.add(Separator())
                }

        if (nodes[nodes.size - 1] is Separator) {
            nodes.removeAt(nodes.size - 1)
        }

        val vBox = VBox(5.0).apply {
            children.addAll(nodes)
        }

        dialogPane.content = vBox
        dialogPane.buttonTypes.addAll(ButtonType.CANCEL, ButtonType.OK)
        dialogPane.id = "preferencesDialogPane"

        setResultConverter { buttonType ->
            if (ButtonType.OK == buttonType) {
                return@setResultConverter preferences
            }

            null
        }
    }

    private class CustomPropertySheet internal constructor(
        items: ObservableList<PropertySheet.Item>
    ) : PropertySheet(items) {
        init {
            mode = PropertySheet.Mode.NAME
            isModeSwitcherVisible = false
            isSearchBoxVisible = false
        }
    }
}
