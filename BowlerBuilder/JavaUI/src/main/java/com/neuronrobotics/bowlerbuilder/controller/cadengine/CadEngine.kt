/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.cadengine

import eu.mihosoft.vrl.v3d.CSG
import javafx.beans.property.BooleanProperty
import javafx.scene.Node
import javafx.scene.SubScene
import javafx.scene.shape.MeshView
import java.io.File

interface CadEngine {

    /**
     * Add MeshViews from a CSG.
     *
     * @param csg CSG to add
     */
    fun addCSG(csg: CSG)

    /**
     * Add MeshViews from all CSGs.
     *
     * @param csgs CSGs to add
     */
    fun addAllCSGs(vararg csgs: CSG)

    /**
     * Add MeshViews from all CSGs.
     *
     * @param csgs List of CSGs to add
     */
    fun addAllCSGs(csgs: Iterable<CSG>)

    /**
     * Select all CSGs from the line in the script.
     *
     * @param script script containing CSG source
     * @param lineNumber line number in script
     */
    fun setSelectedCSG(script: File, lineNumber: Int)

    /**
     * Select a CSG.
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

    /** Removes all meshes except for the background. */
    fun clearMeshes()

    /** Home the camera. */
    fun homeCamera()

    /**
     * Whether the x/y/z axes and grid are showing.
     *
     * @return whether the axes are showing
     */
    fun axisShowingProperty(): BooleanProperty

    /**
     * Whether the hand is showing.
     *
     * @return whether the hand is showing
     */
    fun handShowingProperty(): BooleanProperty

    /**
     * Get the CSG map.
     *
     * @return CGS map
     */
    fun getCsgMap(): Map<CSG, MeshView>

    /**
     * Get the visual content of the engine.
     *
     * @return root node
     */
    fun getView(): Node

    /**
     * Get the subscene.
     *
     * @return subscene
     */
    fun getSubScene(): SubScene
}
