/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager

import com.google.common.collect.ImmutableList
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.hasElement
import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest
import com.neuronrobotics.bowlerbuilder.BowlerBuilder
import com.neuronrobotics.bowlerbuilder.FxHelper
import com.neuronrobotics.bowlerbuilder.controller.module.DefaultCADModelViewerControllerModule
import eu.mihosoft.vrl.v3d.Cube
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.testfx.util.WaitForAsyncUtils
import java.util.ArrayList

class BowlerMobileBaseUITest : AbstractAutoClosingApplicationTest() {

    private var controller: BowlerMobileBaseUI? = null

    override fun start(stage: Stage) {
        controller = BowlerBuilder.getInjector()
                .createChildInjector(DefaultCADModelViewerControllerModule())
                .getInstance(BowlerMobileBaseUI::class.java)
    }

    @AfterEach
    fun afterEach() {
        FxHelper.runAndWait { controller!!.setAllCSG(ArrayList(), null) }
    }

    @Test
    fun addCSGTest() {
        val foo = Cube(1.0, 1.0, 1.0).toCSG()
        controller!!.addCSG(setOf(foo), null)
        WaitForAsyncUtils.waitForFxEvents()

        assertThat(controller!!.visibleCSGs, hasElement(foo))
    }

    @Test
    fun addCSGsTest() {
        val foo = Cube(1.0, 1.0, 1.0).toCSG()
        val bar = Cube(2.0, 1.0, 1.0).toCSG()
        controller!!.addCSG(ImmutableList.of(foo, bar), null)
        WaitForAsyncUtils.waitForFxEvents()

        assertAll(
                { assertThat(controller!!.visibleCSGs, hasElement(foo)) },
                { assertThat(controller!!.visibleCSGs, hasElement(bar)) }
        )
    }
}
