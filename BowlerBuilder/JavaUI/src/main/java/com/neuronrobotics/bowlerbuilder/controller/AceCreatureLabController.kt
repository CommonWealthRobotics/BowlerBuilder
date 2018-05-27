/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.common.base.Throwables
import com.neuronrobotics.bowlerbuilder.LoggerUtilities
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.Optional
import java.util.function.Supplier

class AceCreatureLabController(
    private val scriptEditorPane: TabPane,
    private val scriptEditorSupplier: Supplier<FXMLLoader>,
    val cadModelViewerController: CADModelViewerController,
    val creatureEditorController: CreatureEditorController
) {
    private val tabMap: MutableMap<File, Tab>
    private val tabControllerMap: MutableMap<Tab, AceScriptEditorController>

    init {
        this.tabMap = HashMap()
        this.tabControllerMap = HashMap()
    }

    fun loadFileIntoNewTab(
        title: String,
        pushURL: String,
        fileName: String,
        file: File
    ) {
        loadFileIntoNewTab(title, Optional.empty(), pushURL, fileName, file)
    }

    fun loadFileIntoNewTab(
        title: String,
        graphic: Node,
        pushURL: String,
        fileName: String,
        file: File
    ) {
        loadFileIntoNewTab(title, Optional.of(graphic), pushURL, fileName, file)
    }

    private fun loadFileIntoNewTab(
        title: String,
        graphic: Optional<Node>,
        pushURL: String,
        fileName: String,
        file: File
    ) {
        if (tabMap.containsKey(file)) {
            val tab = tabMap[file]!!
            if (tabControllerMap.containsKey(tab)) {
                val controller = tabControllerMap[tab]!!
                controller.loadManualGist(pushURL, fileName, file)
                scriptEditorPane.selectionModel.select(tab)
            }
        } else {
            val tab = Tab()
            tab.text = title
            graphic.ifPresent({ tab.graphic = it })
            tab.setOnClosed { _ ->
                tabMap.remove(file)
                tabControllerMap.remove(tab)
            }

            scriptEditorPane.tabs.add(tab)
            scriptEditorPane.selectionModel.select(tab)
            tabMap[file] = tab

            val loader = scriptEditorSupplier.get()
            try {
                tab.content = loader.load()
            } catch (e: IOException) {
                LOGGER.severe("Could not load Ace script editor.\n" + Throwables.getStackTraceAsString(e))
            }

            val controller = loader.getController<AceScriptEditorController>()
            tabControllerMap[tab] = controller
            controller.loadManualGist(pushURL, fileName, file)
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(AceCreatureLabController::class.java.simpleName)
    }
}
