/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.cadengine;

import eu.mihosoft.vrl.v3d.CSG;
import java.util.Collection;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.shape.MeshView;

/**
 * Interface to a CAD engine to display and interact with CSGs.
 */
public interface CadEngine {

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  void addCSG(CSG csg);

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs CSGs to add
   */
  void addAllCSGs(CSG... csgs);

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs List of CSGs to add
   */
  void addAllCSGs(Collection<CSG> csgs);

  /**
   * Removes all meshes except for the background.
   */
  void clearMeshes();


  /**
   * Home the camera.
   */
  void homeCamera();

  /**
   * Whether the x/y/z axes and grid are showing.
   */
  BooleanProperty axisShowingProperty();

  /**
   * Whether the hand is showing.
   */
  BooleanProperty handShowingProperty();

  /**
   * Get the CSG map.
   *
   * @return CGS map
   */
  Map<CSG, MeshView> getCsgMap();

  /**
   * Get the visual content of the engine.
   *
   * @return root node
   */
  Node getView();

  /**
   * Get the subscene.
   *
   * @return subscene
   */
  SubScene getSubScene();

}
