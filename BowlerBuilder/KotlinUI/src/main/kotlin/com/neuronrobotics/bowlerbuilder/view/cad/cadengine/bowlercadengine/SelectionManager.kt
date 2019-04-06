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
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

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

    /** De-select the selection. */
    fun cancelSelection()

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
