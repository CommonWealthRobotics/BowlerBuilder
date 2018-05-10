/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller;

import eu.mihosoft.vrl.v3d.CSG;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AceCadEditorTabController {

  private final AceScriptEditorController aceScriptEditorController;
  private final CADModelViewerController cadModelViewerController;

  public AceCadEditorTabController(
      final AceScriptEditorController aceScriptEditorController,
      final CADModelViewerController cadModelViewerController) {
    this.aceScriptEditorController = aceScriptEditorController;
    this.cadModelViewerController = cadModelViewerController;

    aceScriptEditorController
        .getScriptRunner()
        .resultProperty()
        .addListener((observable, oldValue, newValue) -> parseCSG(newValue));
  }

  /**
   * Recursively parse CSG'scale out of an Object. Can contain CSGs in an Iterable. Adds CSG'scale
   * to the CAD viewer.
   *
   * @param item script result
   */
  private void parseCSG(final Object item) {
    if (item instanceof CSG) {
      cadModelViewerController.addCSG((CSG) item);
    } else if (item instanceof Iterable) {
      final Iterable<?> itemList = (Iterable) item;
      itemList.forEach(this::parseCSG);
    }
  }

  public AceScriptEditorController getAceScriptEditorController() {
    return aceScriptEditorController;
  }

  public CADModelViewerController getCadModelViewerController() {
    return cadModelViewerController;
  }
}
