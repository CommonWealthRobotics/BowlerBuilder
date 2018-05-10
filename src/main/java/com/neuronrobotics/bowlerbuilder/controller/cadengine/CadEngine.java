/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.cadengine;

import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.shape.MeshView;
import javax.annotation.Nonnull;

/** Interface to a CAD engine to display and interact with CSGs. */
public interface CadEngine {

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  void addCSG(@Nonnull CSG csg);

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs CSGs to add
   */
  void addAllCSGs(@Nonnull CSG... csgs);

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs List of CSGs to add
   */
  void addAllCSGs(@Nonnull Collection<CSG> csgs);

  /**
   * Select all CSGs from the line in the script.
   *
   * @param script script containing CSG source
   * @param lineNumber line number in script
   */
  void setSelectedCsg(@Nonnull File script, int lineNumber);

  /**
   * Select all CSGs in the collection.
   *
   * @param selection CSGs to select
   */
  void selectCSGs(@Nonnull Collection<CSG> selection);

  /** Removes all meshes except for the background. */
  void clearMeshes();

  /** Home the camera. */
  void homeCamera();

  /**
   * Whether the x/y/z axes and grid are showing.
   *
   * @return whether the axes are showing
   */
  BooleanProperty axisShowingProperty();

  /**
   * Whether the hand is showing.
   *
   * @return whether the hand is showing
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
