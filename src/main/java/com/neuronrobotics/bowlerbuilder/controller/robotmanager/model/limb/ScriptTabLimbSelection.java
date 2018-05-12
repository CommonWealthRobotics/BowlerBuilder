/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.AceCreatureLabController;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.annotation.ParametersAreNonnullByDefault;
import org.eclipse.jgit.api.errors.GitAPIException;

@ParametersAreNonnullByDefault
public class ScriptTabLimbSelection extends AbstractLimbSelection {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(ScriptTabLimbSelection.class.getSimpleName());
  private final VBox widget;

  /**
   * Limb selection in the script tab.
   *
   * @param limb the limb
   * @param controller the controller to load scripts into
   */
  public ScriptTabLimbSelection(
      final DHParameterKinematics limb, final AceCreatureLabController controller) {
    super(limb);

    widget = new VBox(5, getTitleLabel(limb.getScriptingName()));

    final Button editCADEngine = new Button("Edit CAD Engine");
    editCADEngine.setGraphic(AssetFactory.loadIcon("Edit-CAD-Engine.png"));
    try {
      final String[] cadEngineSource = limb.getGitCadEngine();
      final File cadEngineFile =
          ScriptingEngine.fileFromGit(cadEngineSource[0], cadEngineSource[1]);
      editCADEngine.setOnAction(
          event ->
              controller.loadFileIntoNewTab(
                  cadEngineFile.getName(),
                  AssetFactory.loadIcon("Edit-CAD-Engine.png"),
                  cadEngineSource[0],
                  cadEngineSource[1],
                  cadEngineFile));
    } catch (GitAPIException | IOException e) {
      LOGGER.warning(
          "Could not parse file from CAD engine source.\n" + Throwables.getStackTraceAsString(e));
    }

    final Button editDHEngine = new Button("Edit DH Engine");
    editDHEngine.setGraphic(AssetFactory.loadIcon("Edit-Walking-Engine.png"));
    try {
      final String[] dhEngineSource = limb.getGitDhEngine();
      final File dhEngineFile = ScriptingEngine.fileFromGit(dhEngineSource[0], dhEngineSource[1]);
      editDHEngine.setOnAction(
          event ->
              controller.loadFileIntoNewTab(
                  dhEngineFile.getName(),
                  AssetFactory.loadIcon("Edit-Walking-Engine.png"),
                  dhEngineSource[0],
                  dhEngineSource[1],
                  dhEngineFile));
    } catch (GitAPIException | IOException e) {
      LOGGER.warning(
          "Could not parse file from DH engine source.\n" + Throwables.getStackTraceAsString(e));
    }

    final HBox controlsBox = new HBox(5, editCADEngine, editDHEngine);
    widget.getChildren().add(controlsBox);
  }

  @Override
  public Node getWidget() {
    return widget;
  }
}
