/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller; // NOPMD

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.BowlerKernelUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.module.LimbLayoutControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.LimbLayoutController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.LimbLinkLayoutController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.ConfigTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.LimbTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.MovementTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.ScriptTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.ConfigTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.MovementTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.model.LimbType;
import com.neuronrobotics.bowlerbuilder.model.preferences.CreatureEditorControllerPreferences;
import com.neuronrobotics.bowlerbuilder.model.preferences.CreatureEditorControllerPreferencesService;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesConsumer;
import com.neuronrobotics.bowlerbuilder.view.dialog.AddLimbDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.GistFileSelectionDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.Notifications;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

@ParametersAreNonnullByDefault
public class CreatureEditorController implements PreferencesConsumer {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(CreatureEditorController.class.getSimpleName());
  private final CreatureEditorControllerPreferencesService preferencesService;
  private final AnchorPane limbWidget;
  private final AnchorPane movementWidget;
  private final AnchorPane configWidget;
  private final AnchorPane scriptWidget;
  private final ObjectProperty<Selection> widgetSelectionProperty;
  private final ObjectProperty<AnchorPane> selectedWidgetPane;
  private final MainWindowController mainWindowController;
  @FXML private ProgressIndicator cadProgress;
  @FXML private CheckBox autoRegenCAD;
  @FXML private Button regenCADButton;
  @FXML private Button genPrintableCAD;
  @FXML private Button genKinSTL;
  @FXML private TabPane creatureTabPane;
  @FXML private Tab limbTab;
  @FXML private Tab movementTab;
  @FXML private Tab scriptTab;
  @FXML private Tab configTab;
  private MobileBase device;
  private MobileBaseCadManager cadManager;
  private AceCreatureLabController controller;

  @Inject
  public CreatureEditorController(
      final MainWindowController mainWindowController,
      final CreatureEditorControllerPreferencesService preferencesService) {
    this.mainWindowController = mainWindowController;
    this.preferencesService = preferencesService;

    limbWidget = new AnchorPane();
    movementWidget = new AnchorPane();
    configWidget = new AnchorPane();
    scriptWidget = new AnchorPane();
    widgetSelectionProperty = new SimpleObjectProperty<>();
    selectedWidgetPane = new SimpleObjectProperty<>();
  }

  @FXML
  protected void initialize() {
    autoRegenCAD
        .selectedProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (cadManager != null) {
                cadManager.setAutoRegen(newValue);
              }
            });

    selectedWidgetPane.set(limbWidget); // Limb widget to start

    // Change the widget pane new widgets go into when the user changes tabs
    creatureTabPane
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue == limbTab) {
                selectedWidgetPane.set(limbWidget);
              } else if (newValue == movementTab) {
                selectedWidgetPane.set(movementWidget);
              } else if (newValue == configTab) {
                selectedWidgetPane.set(configWidget);
              } else if (newValue == scriptTab) {
                selectedWidgetPane.set(scriptWidget);
              }
            });

    // Fill the widget pane with the widget for the selection
    widgetSelectionProperty.addListener(
        (observable, oldValue, newValue) -> {
          if (newValue != null) {
            final AnchorPane widgetPane = selectedWidgetPane.get();
            if (widgetPane != null) {
              widgetPane.getChildren().setAll(newValue.getWidget());
            }
          }
        });

    final String padding = "-fx-padding: 5px;";

    limbTab.setGraphic(AssetFactory.loadIcon("creature.png"));
    limbTab.setStyle(padding);
    limbTab.setTooltip(new Tooltip("Limb Configuration"));

    movementTab.setGraphic(AssetFactory.loadIcon("Move-Limb.png"));
    movementTab.setStyle(padding);
    movementTab.setTooltip(new Tooltip("Movement"));

    configTab.setGraphic(AssetFactory.loadIcon("Advanced-Configuration.png"));
    configTab.setStyle(padding);
    configTab.setTooltip(new Tooltip("Hardware Configuration"));

    scriptTab.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
    scriptTab.setStyle(padding);
    scriptTab.setTooltip(new Tooltip("Scripting"));

    regenCADButton.setGraphic(AssetFactory.loadIcon("Generate-Cad.png"));
    regenCADButton.setText("Regenerate CAD");

    genPrintableCAD.setGraphic(AssetFactory.loadIcon("Printable-Cad.png"));
    genPrintableCAD.setText("Printable CAD");

    genKinSTL.setGraphic(AssetFactory.loadIcon("Printable-Cad.png"));
    genKinSTL.setText("Kinematic STL");

    refreshPreferences();
  }

  @Override
  public void refreshPreferences() {
    final CreatureEditorControllerPreferences preferences =
        preferencesService.getCurrentPreferencesOrDefault();
    autoRegenCAD.selectedProperty().set(preferences.getAutoRegenCAD());
  }

  /**
   * Fill the CreatureLab tabs with menus for a {@link MobileBase}.
   *
   * @param device {@link MobileBase} to load menus from
   * @param cadManager {@link MobileBaseCadManager} to trigger CAD regens to
   * @param controller {@link AceCreatureLabController} to load files into
   */
  public void generateMenus(
      final MobileBase device,
      final MobileBaseCadManager cadManager,
      final AceCreatureLabController controller) {
    this.device = device;
    this.cadManager = cadManager;
    this.controller = controller;

    cadManager.setAutoRegen(autoRegenCAD.isSelected());

    // TODO: Make the content fit width
    generateLimbTab();
    generateMovementTab();
    generateConfigTab();
    generateScriptTab();
  }

  /** Regenerate menus using the parameters from the last time generateMenus() was called. */
  public void regenerateMenus() {
    generateMenus(device, cadManager, controller);
  }

  /** Clear the selected widget. */
  public void clearWidget() {
    widgetSelectionProperty.setValue(null);
    selectedWidgetPane.get().getChildren().clear();
  }

  private void generateLimbTab() {
    final FXMLLoader loader =
        new FXMLLoader(
            CreatureEditorController.class.getResource(
                "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"),
            null,
            null,
            BowlerBuilder.getInjector().createChildInjector(new LimbLayoutControllerModule(device))
                ::getInstance);

    try {
      final Node content = loader.load();
      Platform.runLater(() -> limbTab.setContent(getScrollPane(new VBox(10, content, limbWidget))));

      final LimbLayoutController controller = loader.getController();

      controller
          .limbSelectionProperty()
          .addListener(
              (observable, oldValue, newValue) ->
                  newValue.ifPresent(
                      limb ->
                          widgetSelectionProperty.set(
                              new LimbTabLimbSelection(device, limb, this))));

      Platform.runLater(
          () -> {
            controller.addToLegHBox(
                getAddLinkButton(AssetFactory.loadIcon("Add-Leg.png"), LimbType.LEG));
            controller.addToArmHBox(
                getAddLinkButton(AssetFactory.loadIcon("Add-Arm.png"), LimbType.ARM));
            controller.addToSteerableHBox(
                getAddLinkButton(
                    AssetFactory.loadIcon("Add-Steerable-Wheel.png"), LimbType.STEERABLE_WHEEL));
            controller.addToFixedHBox(
                getAddLinkButton(
                    AssetFactory.loadIcon("Add-Fixed-Wheel.png"), LimbType.FIXED_WHEEL));
          });
    } catch (final IOException e) {
      LOGGER.severe("Could not load LimbLayout.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private Button getAddLinkButton(final ImageView icon, final LimbType limbType) {
    final Button button = new Button();
    button.setGraphic(icon);
    button.setTooltip(new Tooltip("Add Link"));
    button.setOnAction(
        event -> {
          switch (limbType) {
            case LEG:
              promptAndAddLimb(LimbType.LEG.getDefaultFileName(), device, device.getLegs());
              break;
            case ARM:
              promptAndAddLimb(LimbType.ARM.getDefaultFileName(), device, device.getAppendages());
              break;
            case FIXED_WHEEL:
              promptAndAddLimb(
                  LimbType.FIXED_WHEEL.getDefaultFileName(), device, device.getDrivable());
              break;
            case STEERABLE_WHEEL:
              promptAndAddLimb(
                  LimbType.STEERABLE_WHEEL.getDefaultFileName(), device, device.getSteerable());
              break;
            default: // Nothing to do for default, only 4 kinds of limbs
              break;
          }
        });
    return button;
  }

  private void generateMovementTab() {
    final FXMLLoader loader =
        new FXMLLoader(
            CreatureEditorController.class.getResource(
                "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLinkLayout.fxml"),
            null,
            null,
            BowlerBuilder.getInjector().createChildInjector(new LimbLayoutControllerModule(device))
                ::getInstance);

    try {
      final Node content = loader.load();
      final VBox container = new VBox(10, content, movementWidget);
      container.maxWidth(Double.MAX_VALUE);
      content.maxWidth(Double.MAX_VALUE);
      Platform.runLater(() -> movementTab.setContent(getScrollPane(container)));

      final LimbLinkLayoutController controller = loader.getController();

      controller
          .limbSelectionProperty()
          .addListener(
              (observable, oldValue, newValue) -> {
                newValue.ifPresent(
                    limb -> widgetSelectionProperty.set(new MovementTabLimbSelection(limb)));
                controller.linkSelectionProperty().setValue(Optional.empty());
              });

      controller
          .linkSelectionProperty()
          .addListener(
              (observable, oldValue, newValue) ->
                  newValue.ifPresent(
                      linkData ->
                          widgetSelectionProperty.set(new MovementTabLinkSelection(linkData))));
    } catch (final IOException e) {
      LOGGER.severe("Could not load LimbLinkLayout.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void generateConfigTab() {
    final FXMLLoader loader =
        new FXMLLoader(
            CreatureEditorController.class.getResource(
                "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLinkLayout.fxml"),
            null,
            null,
            BowlerBuilder.getInjector().createChildInjector(new LimbLayoutControllerModule(device))
                ::getInstance);

    try {
      final Node content = loader.load();
      final VBox container = new VBox(10, content, configWidget);
      container.maxWidth(Double.MAX_VALUE);
      content.maxWidth(Double.MAX_VALUE);
      Platform.runLater(() -> configTab.setContent(getScrollPane(container)));

      final LimbLinkLayoutController controller = loader.getController();

      controller
          .limbSelectionProperty()
          .addListener(
              (observable, oldValue, newValue) -> {
                newValue.ifPresent(
                    limb ->
                        widgetSelectionProperty.set(new ConfigTabLimbSelection(limb, cadManager)));
                controller.linkSelectionProperty().setValue(Optional.empty());
              });

      controller
          .linkSelectionProperty()
          .addListener(
              (observable, oldValue, newValue) ->
                  newValue.ifPresent(
                      linkData ->
                          widgetSelectionProperty.set(
                              new ConfigTabLinkSelection(
                                  linkData.getDhLink(),
                                  linkData.getLinkConfiguration(),
                                  linkData.getParentLimb(),
                                  cadManager))));
    } catch (final IOException e) {
      LOGGER.severe("Could not load LimbLinkLayout.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void generateScriptTab() {
    final Button makeCopy = new Button("Clone Creature");
    makeCopy.setGraphic(AssetFactory.loadIcon("Make-Copy-of-Creature.png"));
    makeCopy.setOnAction(
        event ->
            Platform.runLater(
                () -> {
                  final String oldName = device.getScriptingName();
                  final TextInputDialog dialog = new TextInputDialog(oldName + "_copy");
                  dialog.setTitle("Make a copy of " + oldName);
                  dialog.setHeaderText("Set the scripting name for this creature");
                  dialog.setContentText("Name of the new creature:");

                  final Optional<String> result = dialog.showAndWait();
                  result.ifPresent(
                      name ->
                          new Thread(() -> cloneCreature(mainWindowController, device, name))
                              .start());
                }));

    // Have to declare these here because the following block is deeper scope
    final GridPane topLevelControls = new GridPane();
    topLevelControls.setPadding(new Insets(5));
    topLevelControls.add(makeCopy, 0, 0);

    final String[] gitXMLSource = device.getGitSelfSource();
    try {
      final File deviceXMLFile = ScriptingEngine.fileFromGit(gitXMLSource[0], gitXMLSource[1]);

      if (ScriptingEngine.checkOwner(deviceXMLFile)) {
        final FXMLLoader loader =
            new FXMLLoader(
                CreatureEditorController.class.getResource(
                    "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"),
                null,
                null,
                BowlerBuilder.getInjector()
                        .createChildInjector(new LimbLayoutControllerModule(device))
                    ::getInstance);

        final GridPane tabContent =
            getScriptTabContentAsDeviceOwner(makeCopy, deviceXMLFile, device, controller);

        try {
          final Node content = loader.load();
          final LimbLayoutController controller = loader.getController();

          controller
              .limbSelectionProperty()
              .addListener(
                  (observable, oldValue, newValue) ->
                      newValue.ifPresent(
                          limb -> {
                            widgetSelectionProperty.set(
                                new ScriptTabLimbSelection(limb, this.controller));
                          }));

          Platform.runLater(
              () ->
                  scriptTab.setContent(
                      getScrollPane(new VBox(5, tabContent, content, scriptWidget))));
        } catch (final IOException e) {
          LOGGER.severe("Could not load LimbLayout.\n" + Throwables.getStackTraceAsString(e));

          Platform.runLater(() -> scriptTab.setContent(getScrollPane(new VBox(5, tabContent))));
        }
      } else {
        Platform.runLater(() -> scriptTab.setContent(getScrollPane(new VBox(5, topLevelControls))));
      }
    } catch (final GitAPIException | IOException e) {
      LOGGER.severe(
          "Could not parse creature file from source: "
              + Arrays.toString(gitXMLSource)
              + "\n"
              + Throwables.getStackTraceAsString(e));

      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Error")
                  .text("Could not parse file from git source. Creature loading stopped.")
                  .showError());
    }
  }

  @Nonnull
  private static GridPane getScriptTabContentAsDeviceOwner(
      final Button makeCopy,
      final File deviceXMLFile,
      final MobileBase device,
      final AceCreatureLabController controller) {
    final Button publish = new Button("Publish");
    publish.setGraphic(AssetFactory.loadIcon("Publish.png"));
    publish.setOnAction(
        event ->
            new PublishDialog()
                .showAndWait()
                .ifPresent(commitMessage -> publishCreature(device, deviceXMLFile, commitMessage)));

    final Button editWalkingEngine =
        createEditScriptButton(
            "Edit Walking Engine",
            "Edit-Walking-Engine.png",
            "Walking Engine",
            device.getGitWalkingEngine(),
            controller);

    final Button editCADEngine =
        createEditScriptButton(
            "Edit CAD Engine",
            "Edit-CAD-Engine.png",
            "CAD Engine",
            device.getGitCadEngine(),
            controller);

    final Button setWalkingEngine =
        createSetEngineButton(
            "Set Walking Engine",
            "Set-Walking-Engine.png",
            "Select Walking Engine",
            device::setGitWalkingEngine);

    final Button setCADEngine =
        createSetEngineButton(
            "Set CAD Engine", "Set-CAD-Engine.png", "Select CAD Engine", device::setGitCadEngine);

    GridPane.setHalignment(makeCopy, HPos.RIGHT);

    final GridPane tabContent = new GridPane();
    tabContent.setPadding(new Insets(5));
    tabContent.add(makeCopy, 0, 0);
    tabContent.add(publish, 1, 0);

    tabContent.add(editWalkingEngine, 0, 1);
    GridPane.setHalignment(editWalkingEngine, HPos.RIGHT);

    tabContent.add(editCADEngine, 1, 1);

    tabContent.add(setWalkingEngine, 0, 2);
    GridPane.setHalignment(setWalkingEngine, HPos.RIGHT);

    tabContent.add(setCADEngine, 1, 2);

    tabContent.setVgap(5);
    tabContent.setHgap(5);

    return tabContent;
  }

  /**
   * Create a Button to edit a script.
   *
   * @param buttonTitle button text
   * @param scriptIconName button icon file name
   * @param scriptFileName script file name
   * @param fileInGit file URL and name for {@link ScriptingEngine}
   * @param controller controller to load the script
   * @return the Button
   */
  @Nonnull
  private static Button createEditScriptButton(
      final String buttonTitle,
      final String scriptIconName,
      final String scriptFileName,
      final String[] fileInGit,
      final AceCreatureLabController controller) {
    final Button editWalkingEngine = new Button(buttonTitle);
    editWalkingEngine.setGraphic(AssetFactory.loadIcon(scriptIconName));
    editWalkingEngine.setOnAction(
        event ->
            tryParseCreatureFile(fileInGit[0], fileInGit[1])
                .ifPresent(
                    file1 ->
                        controller.loadFileIntoNewTab(
                            scriptFileName,
                            AssetFactory.loadIcon(scriptIconName),
                            fileInGit[0],
                            fileInGit[1],
                            file1)));
    return editWalkingEngine;
  }

  /**
   * Create a Button to set an engine script.
   *
   * @param buttonTitle button text
   * @param scriptIconName button icon file name
   * @param dialogTitle {@link GistFileSelectionDialog} title
   * @param setEngine {@link GistFileSelectionDialog} result consumer to set the engine script
   * @return the Button
   */
  @Nonnull
  private static Button createSetEngineButton(
      final String buttonTitle,
      final String scriptIconName,
      final String dialogTitle,
      final Consumer<? super String[]> setEngine) {
    final Button setCADEngine = new Button(buttonTitle);
    setCADEngine.setGraphic(AssetFactory.loadIcon(scriptIconName));
    setCADEngine.setOnAction(
        event ->
            new GistFileSelectionDialog(dialogTitle, file -> !file.endsWith(".xml"))
                .showAndWait()
                .ifPresent(setEngine));
    return setCADEngine;
  }

  /**
   * Try to get a {@link File} from a "file in git".
   *
   * @param remoteURI file URL
   * @param fileInRepo file name
   * @return the file
   */
  private static Optional<File> tryParseCreatureFile(
      final String remoteURI, final String fileInRepo) {
    try {
      return Optional.of(ScriptingEngine.fileFromGit(remoteURI, fileInRepo));
    } catch (final GitAPIException | IOException e) {
      LOGGER.severe(
          "Could not parse creature file from source."
              + "\n"
              + "URL: "
              + remoteURI
              + "\n"
              + "Filename: "
              + fileInRepo
              + "\n"
              + Throwables.getStackTraceAsString(e));

      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Error")
                  .text("Could not parse file from git source. Creature loading stopped.")
                  .showError());
    }

    return Optional.empty();
  }

  /**
   * Make a clone of a creature. Loads the new creature in a new tab and refreshes git menus.
   *
   * @param mainWindowController controller to load the creature in
   * @param device creature
   * @param name new (clone) creature name
   */
  private static void cloneCreature(
      final MainWindowController mainWindowController, final MobileBase device, final String name) {
    LOGGER.log(Level.INFO, "Your new creature: " + name);
    device.setScriptingName(name);

    final GitHub github = ScriptingEngine.getGithub();
    final GHGistBuilder builder = github.createGist();
    builder.description(name);
    final String filename = name + ".xml";
    builder.file(filename, "<none>");
    builder.public_(true);
    final GHGist gist;
    try {
      gist = builder.create();
      final String gitURL =
          "https://gist.github.com/" + ScriptingEngine.urlToGist(gist.getHtmlUrl()) + ".git";

      LOGGER.log(Level.INFO, "Creating new Robot repo.");
      while (true) {
        try {
          ScriptingEngine.fileFromGit(gitURL, filename);
          break;
        } catch (final Exception ignored) {
          LOGGER.log(Level.INFO, "Waiting. " + gist + " not built yet.");
        }
        ThreadUtil.wait(500);
      }
      LOGGER.log(Level.INFO, "Creating Gist at: " + gitURL);

      LOGGER.log(Level.INFO, "Copying CAD engine.");
      device.setGitCadEngine(
          ScriptingEngine.copyGitFile(
              device.getGitCadEngine()[0], gitURL, device.getGitCadEngine()[1]));

      LOGGER.log(
          Level.INFO,
          "Copying walking engine. Was: " + Arrays.toString(device.getGitWalkingEngine()));
      device.setGitWalkingEngine(
          ScriptingEngine.copyGitFile(
              device.getGitWalkingEngine()[0], gitURL, device.getGitWalkingEngine()[1]));

      LOGGER.log(
          Level.INFO, "Walking engine is now: " + Arrays.toString(device.getGitWalkingEngine()));
      for (final DHParameterKinematics dh : device.getAllDHChains()) {
        LOGGER.log(Level.INFO, "Copying leg CAD engine: " + Arrays.toString(dh.getGitCadEngine()));
        dh.setGitCadEngine(
            ScriptingEngine.copyGitFile(dh.getGitCadEngine()[0], gitURL, dh.getGitCadEngine()[1]));

        LOGGER.log(Level.INFO, "Copying leg DH engine.");
        dh.setGitDhEngine(
            ScriptingEngine.copyGitFile(dh.getGitDhEngine()[0], gitURL, dh.getGitDhEngine()[1]));
      }

      final String xml = device.getXml();
      ScriptingEngine.pushCodeToGit(
          gitURL, ScriptingEngine.getFullBranch(gitURL), filename, xml, "new Robot content");

      LOGGER.info("Clone finished.");
      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Clone Finished")
                  .text("The creature cloning operation finished successfully.")
                  .show());

      final MobileBase mobileBase = new MobileBase(IOUtils.toInputStream(xml, "UTF-8"));
      mobileBase.setGitSelfSource(new String[] {gitURL, name + ".xml"});
      device.disconnect();

      DeviceManager.addConnection(mobileBase, mobileBase.getScriptingName());
      final String[] selfSource = mobileBase.getGitSelfSource();
      mainWindowController.loadCreatureLab(selfSource);
      mainWindowController.reloadGitMenus();
    } catch (final MalformedURLException e) {
      LOGGER.log(
          Level.SEVERE,
          "Could not make copy of creature. Malformed url.\n"
              + Throwables.getStackTraceAsString(e));

      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Error")
                  .text("Could not make copy of creature.")
                  .showError());
    } catch (final Exception e) {
      LOGGER.log(
          Level.SEVERE, "Could not make copy of creature." + Throwables.getStackTraceAsString(e));

      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Error")
                  .text("Could not make copy of creature.")
                  .showError());
    }
  }

  /**
   * Publish the current creature XML file.
   *
   * @param device creature
   * @param deviceXMLFile creature XML file
   * @param commitMessage commit message
   */
  private static void publishCreature(
      final MobileBase device, final File deviceXMLFile, final String commitMessage) {
    try {
      final Git git = ScriptingEngine.locateGit(deviceXMLFile);
      final String remote = git.getRepository().getConfig().getString("remote", "origin", "url");
      final String relativePath = ScriptingEngine.findLocalPath(deviceXMLFile, git);

      // Push to existing gist
      ScriptingEngine.pushCodeToGit(
          remote,
          ScriptingEngine.getFullBranch(remote),
          relativePath,
          device.getXml(),
          commitMessage);
    } catch (final Exception e) {
      LOGGER.severe("Could not commit.\n" + Throwables.getStackTraceAsString(e));

      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Commit error")
                  .text("Could not make commit.")
                  .showError());
    }
  }

  /**
   * Wrap a {@link Node} in a {@link ScrollPane}.
   *
   * @param node node to wrap
   * @return scroll pane with node
   */
  private ScrollPane getScrollPane(final Node node) {
    final ScrollPane pane = new ScrollPane(node);
    pane.setFitToWidth(true);
    pane.setPadding(new Insets(5));
    return pane;
  }

  /**
   * Prompt with a dialog for limb name and hardware indices, then add the limb.
   *
   * @param defaultFileName filename in gist for default configuration (use {@link LimbType}
   * @param device {@link MobileBase} to query used hardware channels from
   * @param toAdd list to add the new limb to
   */
  private void promptAndAddLimb(
      final String defaultFileName,
      final MobileBase device,
      final List<DHParameterKinematics> toAdd) {
    try {
      final String xmlContent =
          ScriptingEngine.codeFromGit(
              "https://gist.github.com/d11d69722610930ae1db9e5821a26178.git", defaultFileName)[0];
      final DHParameterKinematics newLeg =
          new DHParameterKinematics(null, IOUtils.toInputStream(xmlContent, "UTF-8"));

      final List<LinkConfiguration> linkConfigurations = newLeg.getLinkConfigurations();
      final AddLimbDialog dialog =
          new AddLimbDialog(
              newLeg.getScriptingName(),
              linkConfigurations.size(),
              BowlerKernelUtilities.getTakenHardwareChannels(device));

      dialog
          .showAndWait()
          .ifPresent(
              result -> {
                newLeg.setScriptingName(result.getName());

                for (int i = 0; i < linkConfigurations.size(); i++) {
                  final LinkConfiguration conf = linkConfigurations.get(i);
                  conf.setHardwareIndex(result.getIndices().get(i));
                  newLeg.getFactory().refreshHardwareLayer(conf);
                }

                toAdd.add(newLeg);
                regenerateMenus();
              });
    } catch (final Exception e) {
      LOGGER.warning("Could not add limb.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onRegenCAD(final ActionEvent actionEvent) {
    regenCAD();
  }

  @FXML
  private void onGenPrintableCAD(final ActionEvent actionEvent) {
    genSTLs(device, cadManager, false);
  }

  @FXML
  private void onGenKinSTL(final ActionEvent actionEvent) {
    genSTLs(device, cadManager, true);
  }

  /** Regenerate {@link MobileBase} CAD if there is a non-null {@link MobileBaseCadManager}. */
  public void regenCAD() {
    if (cadManager != null) {
      cadManager.generateCad(); // TODO: Always regen CAD regardless of auto regen flag
    }
  }

  /**
   * Show a {@link DirectoryChooser} to pick a save directory and then generate and save STL files
   * for the given {@link MobileBase} and {@link MobileBaseCadManager}.
   *
   * @param device creature to gen STLs for
   * @param cadManager CAD manager to gen STLs with
   * @param isKinematic whether to gen kinematic STLs
   */
  public void genSTLs(
      final MobileBase device, final MobileBaseCadManager cadManager, final Boolean isKinematic) {
    final File defaultStlDir = new File(System.getProperty("user.home") + "/bowler-workspace/STL/");

    if (!defaultStlDir.exists() && !defaultStlDir.mkdirs()) {
      LOGGER.log(Level.WARNING, "Could not create default directory to save STL files.");
      return;
    }

    Platform.runLater(
        () -> {
          final DirectoryChooser chooser = new DirectoryChooser();
          chooser.setTitle("Select Output Directory For STL files");

          chooser.setInitialDirectory(defaultStlDir);
          final File baseDirForFiles = chooser.showDialog(creatureTabPane.getScene().getWindow());
          if (baseDirForFiles == null) {
            LOGGER.log(Level.INFO, "No directory selected. Not saving STL files.");
            return;
          }

          LoggerUtilities.newLoggingThread(
                  LOGGER,
                  () -> {
                    try {
                      final List<File> files =
                          cadManager.generateStls(device, baseDirForFiles, isKinematic);

                      Platform.runLater(
                          () ->
                              Notifications.create()
                                  .title("STL Export Success")
                                  .text(
                                      "All STL files for the creature generated at:\n"
                                          + files.get(0).getAbsolutePath())
                                  .showInformation());
                    } catch (IOException e) {
                      LOGGER.log(
                          Level.WARNING,
                          "Could not generate STL files to save in: "
                              + baseDirForFiles.getAbsolutePath()
                              + "\n"
                              + Throwables.getStackTraceAsString(e));

                      Platform.runLater(
                          () ->
                              Notifications.create()
                                  .title("STL Export Failure")
                                  .text("Could not generate STL files.")
                                  .showError());
                    } catch (RuntimeException e) {
                      if (e.getMessage().contains("IgenerateBed")) {
                        LOGGER.log(
                            Level.INFO,
                            "Cannot generate STL files because the supplied CAD manager "
                                + "does not implement the IgenerateBed interface.\n"
                                + Throwables.getStackTraceAsString(e));
                      } else {
                        LOGGER.log(Level.WARNING, Throwables.getStackTraceAsString(e));
                      }

                      Platform.runLater(
                          () ->
                              Notifications.create()
                                  .title("STL Export Failure")
                                  .text("Could not generate STL files.")
                                  .showError());
                    }
                  })
              .start();
        });
  }

  public ProgressIndicator getCadProgress() {
    return cadProgress;
  }
}
