/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.inject.Guice
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.isA
import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest
import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.FxHelper
import com.neuronrobotics.bowlerbuilder.FxUtil
import com.neuronrobotics.bowlerbuilder.controller.module.DefaultCadEditorControllerModule
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ace.AceEditorView
import eu.mihosoft.vrl.v3d.CSG
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import javafx.util.Callback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.IOException
import java.util.concurrent.ExecutionException

class AceCadEditorTest : AbstractAutoClosingApplicationTest() {

    private lateinit var controller: DefaultScriptEditorController

    @Throws(IOException::class)
    override fun start(stage: Stage) {
        val loader = FXMLLoader(
                javaClass.getResource("../view/DefaultScriptEditor.fxml"), null, null,
                Callback<Class<*>, Any> {
                    Guice.createInjector(
                            DefaultCadEditorControllerModule(
                                    BowlerBuilder
                                            .getInjector()
                                            .getInstance(AceEditorView::class.java)))
                            .getInstance(it)
                })
        val mainWindow = loader.load<SplitPane>()
        controller = loader.getController<DefaultScriptEditorController>()
        stage.scene = Scene(mainWindow)
        stage.show()
    }

    @Test
    fun runEmptyFileTest() {
        FxHelper.runAndWait { controller.runEditorContent() }

        assertAll(
                { assertTrue(controller.scriptRunner.getResult().isSuccess()) },
                { assertNull(controller.scriptRunner.getResult().getSuccess()) }
        )
    }

    @Test
    fun basicRunButtonTest() {
        FxHelper.runAndWait { controller.insertAtCursor("CSG foo=new Cube(10,10,10).toCSG()") }
        FxHelper.runAndWait { controller.runEditorContent() }

        val result = controller.scriptRunner.getResult()
        assertAll(
                { assertTrue(result.isSuccess()) },
                { assertNotNull(result.getSuccess()) },
                { assertThat(result.getSuccess()!!, isA<CSG>()) }
        )
    }

    @Test
    fun runCubeTest() {
        FxHelper.runAndWait { controller.insertAtCursor("CSG foo=new Cube(10,10,10).toCSG()") }
        controller.runEditorContent()

        assertNotNull(controller.scriptRunner.getResult())
    }

    @Test
    fun runStringScriptTest() {
        controller.runStringScript("CSG foo = new Sphere(10).toCSG();", null, "BowlerGroovy")
        val result = controller.scriptRunner.getResult()

        assertAll(
                { assertTrue(result.isSuccess()) },
                { assertNotNull(result.getSuccess()) },
                { assertThat(result.getSuccess()!!, isA<CSG>()) }
        )
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun getTextTest() {
        FxHelper.runAndWait { controller.scriptEditor.insertAtCursor("foo\nbar") }

        assertEquals("foo\nbar", FxUtil.returnFX { controller.scriptEditor.getFullText() })
    }
}
