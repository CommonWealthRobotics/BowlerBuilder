/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller

import com.google.inject.Inject
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine
import eu.mihosoft.vrl.v3d.CSG
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.scene.shape.MeshView

class DefaultCADModelViewerController
@Inject constructor(
    val engine: CadEngine
) {

    @FXML
    private lateinit var root: BorderPane
    private var axisShowing = true
    private var handShowing = true

    val csgMap: Map<CSG, MeshView>
        get() = engine.getCsgMap()

    @FXML
    private fun initialize() {
        val subScene = engine.getSubScene()
        subScene.isFocusTraversable = false
        subScene.widthProperty().bind(root.widthProperty())
        subScene.heightProperty().bind(root.heightProperty())
        AnchorPane.setTopAnchor(subScene, 0.0)
        AnchorPane.setRightAnchor(subScene, 0.0)
        AnchorPane.setLeftAnchor(subScene, 0.0)
        AnchorPane.setBottomAnchor(subScene, 0.0)

        root.center = engine.getView()
        root.id = "cadViewerBorderPane"
    }

    /**
     * Add MeshViews from a CSG.
     *
     * @param csg CSG to add
     */
    fun addCSG(csg: CSG) = engine.addCSG(csg)

    /**
     * Add MeshViews from all CSGs.
     *
     * @param csgs CSGs to add
     */
    fun addAllCSGs(vararg csgs: CSG) = engine.addAllCSGs(*csgs)

    /**
     * Add MeshViews from all CSGs.
     *
     * @param csgs List of CSGs to add
     */
    fun addAllCSGs(csgs: Iterable<CSG>) = engine.addAllCSGs(csgs)

    /** Removes all meshes except for the background.  */
    fun clearMeshes() {
        engine.clearMeshes()
    }

    @FXML
    private fun onHomeCamera(actionEvent: ActionEvent) = engine.homeCamera()

    @FXML
    private fun onAxis(actionEvent: ActionEvent) {
        axisShowing = !axisShowing
        engine.axisShowingProperty().value = axisShowing
    }

    @FXML
    private fun onHand(actionEvent: ActionEvent) {
        handShowing = !handShowing
        engine.handShowingProperty().value = handShowing
    }

    @FXML
    private fun onClearObjects(actionEvent: ActionEvent) = clearMeshes()
}
