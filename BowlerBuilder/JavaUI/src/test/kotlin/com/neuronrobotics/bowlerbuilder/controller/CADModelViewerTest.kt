/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.hasElement
import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest
import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.FxHelper
import com.neuronrobotics.bowlerbuilder.controller.module.DefaultCADModelViewerControllerModule
import eu.mihosoft.vrl.v3d.Cube
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.util.Callback
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.io.IOException

class CADModelViewerTest : AbstractAutoClosingApplicationTest() {

    private var controller: DefaultCADModelViewerController? = null

    @Throws(IOException::class)
    override fun start(stage: Stage) {
        val loader = FXMLLoader(
            javaClass.getResource("../view/DefaultCADModelViewer.fxml"),
            null, null,
            Callback<Class<*>, Any> {
                BowlerBuilder.injector.createChildInjector(
                    DefaultCADModelViewerControllerModule()
                ).getInstance(it)
            })
        val mainWindow = loader.load<BorderPane>()
        controller = loader.getController<DefaultCADModelViewerController>()
        stage.scene = Scene(mainWindow)
        stage.show()
    }

    @AfterEach
    fun afterEach() {
        FxHelper.runAndWait { controller!!.clearMeshes() }
    }

    @Test
    @Disabled
    fun addCSGTest() {
        val foo = Cube(1.0, 1.0, 1.0).toCSG()
        controller!!.addCSG(foo)

        assertTrue(controller!!.csgMap.containsKey(foo))
    }

    @Test
    @Disabled
    fun addCSGsTest() {
        val foo = Cube(1.0, 1.0, 1.0).toCSG()
        val bar = Cube(2.0, 1.0, 1.0).toCSG()
        controller!!.addAllCSGs(foo, bar)

        val keys = controller!!.csgMap.keys
        assertAll(
            { assertThat(keys, hasElement(foo)) },
            { assertThat(keys, hasElement(bar)) }
        )
    }

    @Test
    @Disabled
    fun addCSGs2Test() {
        val foo = Cube(1.0, 1.0, 1.0).toCSG()
        val bar = Cube(2.0, 1.0, 1.0).toCSG()
        controller!!.addAllCSGs(listOf(foo, bar))

        val keys = controller!!.csgMap.keys
        assertAll(
            { assertThat(keys, hasElement(foo)) },
            { assertThat(keys, hasElement(bar)) }
        )
    }
}
