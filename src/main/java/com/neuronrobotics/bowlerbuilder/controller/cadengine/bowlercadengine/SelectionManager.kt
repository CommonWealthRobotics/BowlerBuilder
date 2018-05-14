/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine

import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.SubScene
import javafx.scene.input.MouseEvent
import java.io.File

interface SelectionManager {

    /**
     * Select all CSGs from the line in the script.
     *
     * @param script script containing CSG source
     * @param lineNumber line number in script
     */
    fun setSelectedCSG(script: File, lineNumber: Int)

    /**
     * Select a CSG and pan the camera to that CSG.
     *
     * @param selection CSG to select
     */
    fun selectCSG(selection: CSG)

    /**
     * Select all CSGs in the collection.
     *
     * @param selection CSGs to select
     */
    fun selectCSGs(selection: Iterable<CSG>)

    /**
     * Handle a mouse event from the 3D window.
     *
     * @param mouseEvent JavaFX-generated mouse event
     * @param csg CSG the event was generated from
     */
    fun mouseEvent(mouseEvent: MouseEvent, csg: CSG)

    /**
     * Attach mouse listeners to the scene. Side-effects the scene.
     *
     * @param scene the scene
     */
    fun attachMouseListenersToScene(scene: SubScene)
}
