/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine.bowlercadengine

import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice
import com.neuronrobotics.bowlerstudio.physics.TransformFactory
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR
import eu.mihosoft.vrl.v3d.CSG
import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Group
import javafx.scene.SubScene
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent
import javafx.scene.paint.Color
import javafx.scene.paint.PhongMaterial
import javafx.scene.transform.Affine
import kotlinx.coroutines.experimental.launch
import org.reactfx.util.FxTimer
import java.io.File
import java.time.Duration
import java.util.Arrays
import java.util.Optional
import java.util.function.BiConsumer
import kotlinx.coroutines.experimental.javafx.JavaFx as UI

class DefaultSelectionManager
/**
 * Manages [CSG] selections (from the GUI or programmatically).
 *
 * @param csgManager [CSGManager] to pull the CSG map from
 * @param focusGroup focus group
 * @param virtualCam virtual camera
 * @param moveCamera [BiConsumer] to move the camera around for a new selection
 */(
     private val csgManager: CSGManager,
     private val focusGroup: Group,
     private val virtualCam: VirtualCameraDevice,
     private val moveCamera: BiConsumer<TransformNR, Double>
 ) : SelectionManager {

    private var mousePosX: Double = 0.0
    private var mousePosY: Double = 0.0
    private var mouseOldX: Double = 0.0
    private var mouseOldY: Double = 0.0
    private var mouseDeltaX: Double = 0.0
    private var mouseDeltaY: Double = 0.0
    private var previousTarget = TransformNR()
    private val selectedCSG: ObjectProperty<Optional<CSG>> = SimpleObjectProperty(Optional.empty())

    /**
     * Select all CSGs from the line in the script.
     *
     * @param script script containing CSG source
     * @param lineNumber line number in script
     */
    override fun setSelectedCSG(script: File, lineNumber: Int) {
        launch(context = UI) {
            val csgs = csgManager
                    .csgParser
                    .parseCsgFromSource(script.name, lineNumber, csgManager.getCSGs())

            if (csgs.size == 1) {
                selectCSG(csgs.iterator().next())
            } else {
                selectCSGs(csgs)
            }
        }
    }

    /**
     * Select a CSG and pan the camera to that CSG.
     *
     * @param selection CSG to select
     */
    override fun selectCSG(selection: CSG) {
        if (selectedCSG.value.isPresent && selection == selectedCSG.value.get()) {
            return
        }

        csgManager
                .getCSGs()
                .forEach { key ->
                    Platform.runLater {
                        csgManager.getMeshView(key)?.material = PhongMaterial(key.color)
                    }
                }

        selectedCSG.value = Optional.of(selection)
        csgManager.getMeshView(selection)?.material = PhongMaterial(Color.GOLD)

        val xCenter = selection.centerX
        val yCenter = selection.centerY
        val zCenter = selection.centerZ

        val poseToMove = TransformNR()

        if (selection.maxX < 1 || selection.minX > -1) {
            poseToMove.translateX(xCenter)
        }

        if (selection.maxY < 1 || selection.minY > -1) {
            poseToMove.translateY(yCenter)
        }

        if (selection.maxZ < 1 || selection.minZ > -1) {
            poseToMove.translateZ(zCenter)
        }

        val centering = TransformFactory.nrToAffine(poseToMove)

        // this section keeps the camera oriented the same way to avoid whipping around
        val rotationOnlyComponentOfManipulator = TransformFactory.affineToNr(selection.manipulator)
        rotationOnlyComponentOfManipulator.x = 0.0
        rotationOnlyComponentOfManipulator.y = 0.0
        rotationOnlyComponentOfManipulator.z = 0.0
        val reverseRotation = rotationOnlyComponentOfManipulator.inverse()

        // TODO: Issue #32
        val startSelectNr = previousTarget.copy()
        val targetNR = if (checkManipulator(selection)) {
            TransformFactory.affineToNr(selection.manipulator)
        } else {
            TransformFactory.affineToNr(centering)
        }

        val interpolator = Affine()
        val correction = TransformFactory.nrToAffine(reverseRotation)

        launch(context = UI) {
            interpolator.tx = startSelectNr.x - targetNR.x
            interpolator.ty = startSelectNr.y - targetNR.y
            interpolator.tz = startSelectNr.z - targetNR.z
            removeAllFocusTransforms()
            focusGroup.transforms.add(interpolator)

            if (checkManipulator(selection)) {
                focusGroup.transforms.add(selection.manipulator)
                focusGroup.transforms.add(correction)
            } else {
                focusGroup.transforms.add(centering)
            }

            focusInterpolate(startSelectNr, targetNR, 0, 30, interpolator)
        }
    }

    /**
     * Select all CSGs in the collection.
     *
     * @param selection CSGs to select
     */
    override fun selectCSGs(selection: Iterable<CSG>) {
        selection.forEach { csg ->
            val meshView = csgManager.getMeshView(csg)
            if (meshView != null) {
                FxTimer.runLater(
                        Duration.ofMillis(20)) { meshView.material = PhongMaterial(Color.GOLD) }
            }
        }
    }

    /** De-select the selection.  */
    override fun cancelSelection() {
        for (key in csgManager.getCSGs()) {
            launch(context = UI) {
                csgManager.getMeshView(key)?.material = PhongMaterial(key.color)
            }
        }

        selectedCSG.value = Optional.empty()
        val startSelectNr = previousTarget.copy()
        val targetNR = TransformNR()
        val interpolator = Affine()
        TransformFactory.nrToAffine(startSelectNr, interpolator)

        launch(context = UI) {
            removeAllFocusTransforms()
            focusGroup.transforms.add(interpolator)
            focusInterpolate(startSelectNr, targetNR, 0, 15, interpolator)
        }
    }

    /**
     * Handle a mouse event from the 3D window.
     *
     * @param mouseEvent JavaFX-generated mouse event
     * @param csg CSG the event was generated from
     */
    override fun mouseEvent(mouseEvent: MouseEvent, csg: CSG) {
        selectCSG(csg)

        mouseOldX = mousePosX
        mouseOldY = mousePosY
        mousePosX = mouseEvent.sceneX
        mousePosY = mouseEvent.sceneY
    }

    /**
     * Attach mouse listeners to the scene. Side-effects the scene.
     *
     * @param scene the scene
     */
    override fun attachMouseListenersToScene(scene: SubScene) {
        scene.setOnMousePressed { mouseEvent ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = mouseEvent.sceneX
            mousePosY = mouseEvent.sceneY
        }

        scene.setOnMouseDragged { mouseEvent ->
            mouseOldX = mousePosX
            mouseOldY = mousePosY
            mousePosX = mouseEvent.sceneX
            mousePosY = mouseEvent.sceneY
            mouseDeltaX = mousePosX - mouseOldX
            mouseDeltaY = mousePosY - mouseOldY

            var modifier = 1.0
            val modifierFactor = 0.1

            if (mouseEvent.isControlDown) {
                modifier = 0.1
            } else if (mouseEvent.isShiftDown) {
                modifier = 10.0
            }

            if (mouseEvent.isPrimaryButtonDown) {
                val trans = TransformNR(
                        0.0,
                        0.0,
                        0.0,
                        RotationNR(
                                mouseDeltaY * modifierFactor * modifier * 2.0,
                                mouseDeltaX * modifierFactor * modifier * 2.0,
                                0.0))

                if (mouseEvent.isPrimaryButtonDown) {
                    moveCamera.accept(trans, 0.0)
                }
            } else if (mouseEvent.isSecondaryButtonDown) {
                val depth = -100 / virtualCam.zoomDepth
                moveCamera.accept(
                        TransformNR(
                                mouseDeltaX * modifierFactor * modifier * 1.0 / depth,
                                mouseDeltaY * modifierFactor * modifier * 1.0 / depth,
                                0.0,
                                RotationNR()),
                        0.0)
            }
        }

        scene.addEventHandler(
                ScrollEvent.ANY
        ) { event ->
            if (ScrollEvent.SCROLL == event.eventType) {
                val zoomFactor = -event.deltaY * virtualCam.zoomDepth / 500
                virtualCam.zoomDepth = virtualCam.zoomDepth + zoomFactor
            }
            event.consume()
        }
    }

    private fun focusInterpolate(
        start: TransformNR,
        target: TransformNR,
        depth: Int,
        targetDepth: Int,
        interpolator: Affine
    ) {

        val depthScale = 1 - depth.toDouble() / targetDepth.toDouble()
        val sinusoidalScale = Math.sin(depthScale * (Math.PI / 2))

        val difference = start.x - target.x

        val xIncrement = difference * sinusoidalScale
        val yIncrement = (start.y - target.y) * sinusoidalScale
        val zIncrement = (start.z - target.z) * sinusoidalScale

        launch(context = UI) {
            interpolator.tx = xIncrement
            interpolator.ty = yIncrement
            interpolator.tz = zIncrement
        }

        if (depth < targetDepth) {
            FxTimer.runLater(
                    Duration.ofMillis(16)
            ) { focusInterpolate(start, target, depth + 1, targetDepth, interpolator) }
        } else {
            launch(context = UI) { focusGroup.transforms.remove(interpolator) }
            previousTarget = target.copy()
            previousTarget.rotation = RotationNR()
        }
    }

    private fun removeAllFocusTransforms() {
        val allTrans = focusGroup.transforms
        val toRemove = allTrans.toTypedArray()
        Arrays.stream(toRemove).forEach({ allTrans.remove(it) })
    }

    private fun checkManipulator(csg: CSG): Boolean {
        return (Math.abs(csg.manipulator.tx) > 0.1 ||
                Math.abs(csg.manipulator.ty) > 0.1 ||
                Math.abs(csg.manipulator.tz) > 0.1)
    }
}
