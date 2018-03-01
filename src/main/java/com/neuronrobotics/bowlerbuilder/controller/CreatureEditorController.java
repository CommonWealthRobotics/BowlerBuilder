package com.neuronrobotics.bowlerbuilder.controller; //NOPMD

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.module.LimbLayoutControllerModule;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.LimbLayoutController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.LimbLinkLayoutController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.ConfigTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.LimbTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.MovementTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.ConfigTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.MovementTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.model.LimbType;
import com.neuronrobotics.bowlerbuilder.view.dialog.AddLimbDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.GistFileSelectionDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.Notifications;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

public class CreatureEditorController {

  private static final Logger logger =
      LoggerUtilities.getLogger(CreatureEditorController.class.getSimpleName());
  private final AnchorPane limbWidget;
  private final AnchorPane movementWidget;
  private final AnchorPane configWidget;
  private final ObjectProperty<Selection> selectionProperty;
  private final ObjectProperty<AnchorPane> selectedWidgetPane;
  private final MainWindowController mainWindowController;
  @FXML
  private ProgressIndicator cadProgress;
  @FXML
  private CheckBox autoRegenCAD;
  @FXML
  private Button regenCADButton;
  @FXML
  private Button genPrintableCAD;
  @FXML
  private Button genKinSTL;
  @FXML
  private TabPane creatureTabPane;
  @FXML
  private Tab limbTab;
  @FXML
  private Tab movementTab;
  @FXML
  private Tab scriptTab;
  @FXML
  private Tab configTab;
  private MobileBase device;
  private MobileBaseCadManager cadManager;
  private AceCreatureEditorController controller;

  @Inject
  public CreatureEditorController(MainWindowController mainWindowController) {
    this.mainWindowController = mainWindowController;

    limbWidget = new AnchorPane();
    movementWidget = new AnchorPane();
    configWidget = new AnchorPane();
    selectionProperty = new SimpleObjectProperty<>();
    selectedWidgetPane = new SimpleObjectProperty<>();
  }

  /**
   * Calculate the taken ("occupied") hardware channels on the device based on the config saved to
   * the device.
   *
   * @param device device to check
   * @return all taken channels
   */
  public static Set<Integer> getTakenChannels(MobileBase device) {
    return device.getAllDHChains().stream().map(AbstractKinematicsNR::getLinkConfigurations)
        .flatMap(Collection::stream)
        .map(LinkConfiguration::getHardwareIndex)
        .collect(Collectors.toSet());
  }

  @FXML
  protected void initialize() {
    selectedWidgetPane.set(limbWidget); //Limb widget to start

    //Change the widget pane new widgets go into when the user changes tabs
    creatureTabPane.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue == limbTab) {
            selectedWidgetPane.set(limbWidget);
          } else if (newValue == movementTab) {
            selectedWidgetPane.set(movementWidget);
          } else if (newValue == configTab) {
            selectedWidgetPane.set(configWidget);
          }
        });

    //Fill the widget pane with the widget for the selection
    selectionProperty.addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        final AnchorPane widgetPane = selectedWidgetPane.get();
        if (widgetPane != null) {
          widgetPane.getChildren().setAll(newValue.getWidget());
        }
      }
    });

    limbTab.setGraphic(AssetFactory.loadIcon("creature.png"));
    limbTab.setStyle("-fx-padding: 5px;");
    movementTab.setGraphic(AssetFactory.loadIcon("Move-Limb.png"));
    movementTab.setStyle("-fx-padding: 5px;");
    configTab.setGraphic(AssetFactory.loadIcon("Advanced-Configuration.png"));
    configTab.setStyle("-fx-padding: 5px;");
    scriptTab.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
    scriptTab.setStyle("-fx-padding: 5px;");

    regenCADButton.setGraphic(AssetFactory.loadIcon("Generate-Cad.png"));
    regenCADButton.setText("Regenerate CAD");

    genPrintableCAD.setGraphic(AssetFactory.loadIcon("Printable-Cad.png"));
    genPrintableCAD.setText("Printable CAD");

    genKinSTL.setGraphic(AssetFactory.loadIcon("Printable-Cad.png"));
    genKinSTL.setText("Kinematic STL");
  }

  /**
   * Fill the CreatureLab tabs with menus for a {@link MobileBase}.
   *
   * @param device {@link MobileBase} to load menus from
   * @param cadManager {@link MobileBaseCadManager} to trigger CAD regens to
   */
  public void generateMenus(MobileBase device, MobileBaseCadManager cadManager,
      AceCreatureEditorController controller) {
    this.device = device;
    this.cadManager = cadManager;
    this.controller = controller;

    autoRegenCAD.selectedProperty().addListener((observable, oldValue, newValue) ->
        cadManager.setAutoRegen(newValue));
    cadManager.setAutoRegen(autoRegenCAD.isSelected());

    //TODO: Make the content fit width
    generateLimbTab();
    generateMovementTab();
    generateConfigTab();
    generateScriptTab();
  }

  /**
   * Regenerate menus using the parameters from the last time generateMenus() was called.
   */
  public void regenerateMenus() {
    generateMenus(device, cadManager, controller);
  }

  /**
   * Clear the selected widget.
   */
  public void clearWidget() {
    selectionProperty.setValue(null);
    selectedWidgetPane.get().getChildren().clear();
  }

  private void generateLimbTab() {
    final FXMLLoader loader = new FXMLLoader(CreatureEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLayout.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new LimbLayoutControllerModule(device))::getInstance);

    try {
      final Node content = loader.load();
      Platform.runLater(() -> limbTab.setContent(getScrollPane(
          new VBox(10, content, limbWidget))));

      final LimbLayoutController controller = loader.getController();

      controller.limbSelectionProperty().addListener((observable, oldValue, newValue) ->
          newValue.ifPresent(limb ->
              selectionProperty.set(new LimbTabLimbSelection(device, limb, this))));

      Platform.runLater(() -> {
        controller.addToLegHBox(getAddLinkButton(AssetFactory.loadIcon("Add-Leg.png"),
            LimbType.LEG));
        controller.addToArmHBox(getAddLinkButton(AssetFactory.loadIcon("Add-Arm.png"),
            LimbType.ARM));
        controller.addToSteerableHBox(getAddLinkButton(
            AssetFactory.loadIcon("Add-Steerable-Wheel.png"), LimbType.STEERABLE_WHEEL));
        controller.addToFixedHBox(getAddLinkButton(
            AssetFactory.loadIcon("Add-Fixed-Wheel.png"), LimbType.FIXED_WHEEL));
      });
    } catch (IOException e) {
      logger.severe("Could not load LimbLayout.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private Button getAddLinkButton(ImageView icon, LimbType limbType) {
    final Button button = new Button();
    button.setGraphic(icon);
    button.setOnAction(event -> {
      switch (limbType) {
        case LEG:
          promptAndAddLimb(LimbType.LEG.getDefaultFileName(), device, device.getLegs());
          break;
        case ARM:
          promptAndAddLimb(LimbType.ARM.getDefaultFileName(), device, device.getAppendages());
          break;
        case FIXED_WHEEL:
          promptAndAddLimb(LimbType.FIXED_WHEEL.getDefaultFileName(), device,
              device.getDrivable());
          break;
        case STEERABLE_WHEEL:
          promptAndAddLimb(LimbType.STEERABLE_WHEEL.getDefaultFileName(), device,
              device.getSteerable());
          break;
        default: //Nothing to do for default, only 4 kinds of limbs
          break;
      }
    });
    return button;
  }

  private void generateMovementTab() {
    final FXMLLoader loader = new FXMLLoader(CreatureEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLinkLayout.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new LimbLayoutControllerModule(device))::getInstance);

    try {
      final Node content = loader.load();
      VBox container = new VBox(10, content, movementWidget);
      container.maxWidth(Double.MAX_VALUE);
      content.maxWidth(Double.MAX_VALUE);
      Platform.runLater(() -> movementTab.setContent(getScrollPane(container)));

      final LimbLinkLayoutController controller = loader.getController();

      controller.limbSelectionProperty().addListener((observable, oldValue, newValue) ->
          newValue.ifPresent(limb ->
              selectionProperty.set(new MovementTabLimbSelection(limb))));

      controller.linkSelectionProperty().addListener((observable, oldValue, newValue) ->
          newValue.ifPresent(linkData ->
              selectionProperty.set(new MovementTabLinkSelection(linkData))));
    } catch (IOException e) {
      logger.severe("Could not load LimbLinkLayout.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void generateConfigTab() {
    final FXMLLoader loader = new FXMLLoader(CreatureEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/robotmanager/LimbLinkLayout.fxml"),
        null,
        null,
        BowlerBuilder.getInjector().createChildInjector(
            new LimbLayoutControllerModule(device))::getInstance);

    try {
      final Node content = loader.load();
      VBox container = new VBox(10, content, configWidget);
      container.maxWidth(Double.MAX_VALUE);
      content.maxWidth(Double.MAX_VALUE);
      Platform.runLater(() -> configTab.setContent(getScrollPane(container)));

      final LimbLinkLayoutController controller = loader.getController();

      controller.limbSelectionProperty().addListener((observable, oldValue, newValue) ->
          newValue.ifPresent(limb -> {
            selectionProperty.set(new ConfigTabLimbSelection(limb, device, cadManager));
          }));

      controller.linkSelectionProperty().addListener((observable, oldValue, newValue) ->
          newValue.ifPresent(linkData ->
              selectionProperty.set(new ConfigTabLinkSelection(linkData.dhLink,
                  linkData.linkConfiguration, linkData.parentLimb, cadManager))));
    } catch (IOException e) {
      logger.severe("Could not load LimbLinkLayout.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void generateScriptTab() {
    final Button makeCopy = new Button("Clone Creature");
    makeCopy.setGraphic(AssetFactory.loadIcon("Make-Copy-of-Creature.png"));
    makeCopy.setOnAction(event -> Platform.runLater(() -> {
      final String oldName = device.getScriptingName();
      final TextInputDialog dialog = new TextInputDialog(oldName + "_copy");
      dialog.setTitle("Make a copy of " + oldName);
      dialog.setHeaderText("Set the scripting name for this creature");
      dialog.setContentText("Name of the new creature:");

      final Optional<String> result = dialog.showAndWait();
      result.ifPresent(name -> new Thread(() -> {
        logger.log(Level.INFO, "Your new creature: " + name);
        device.setScriptingName(name);

        final GitHub github = ScriptingEngine.getGithub();
        final GHGistBuilder builder = github.createGist();
        builder.description(name + " copy of " + oldName);
        final String filename = name + ".xml";
        builder.file(filename, "<none>");
        builder.public_(true);
        final GHGist gist;
        try {
          gist = builder.create();
          final String gitURL = "https://gist.github.com/"
              + ScriptingEngine.urlToGist(gist.getHtmlUrl()) + ".git";

          logger.log(Level.INFO, "Creating new Robot repo.");
          while (true) {
            try {
              ScriptingEngine.fileFromGit(gitURL, filename);
              break;
            } catch (Exception ignored) {
              logger.log(Level.INFO, "Waiting. " + gist + " not built yet.");
            }
            ThreadUtil.wait(500);
          }
          logger.log(Level.INFO, "Creating Gist at: " + gitURL);

          logger.log(Level.INFO, "Copying CAD engine.");
          device.setGitCadEngine(ScriptingEngine.copyGitFile(device.getGitCadEngine()[0], gitURL,
              device.getGitCadEngine()[1]));

          logger.log(Level.INFO, "Copying walking engine. Was: "
              + Arrays.toString(device.getGitWalkingEngine()));
          device.setGitWalkingEngine(ScriptingEngine.copyGitFile(device.getGitWalkingEngine()[0],
              gitURL, device.getGitWalkingEngine()[1]));

          logger.log(Level.INFO, "Walking engine is now: "
              + Arrays.toString(device.getGitWalkingEngine()));
          for (DHParameterKinematics dh : device.getAllDHChains()) {
            logger.log(Level.INFO, "Copying leg CAD engine: "
                + Arrays.toString(dh.getGitCadEngine()));
            dh.setGitCadEngine(ScriptingEngine.copyGitFile(dh.getGitCadEngine()[0], gitURL,
                dh.getGitCadEngine()[1]));

            logger.log(Level.INFO, "Copying leg DH engine.");
            dh.setGitDhEngine(ScriptingEngine.copyGitFile(dh.getGitDhEngine()[0], gitURL,
                dh.getGitDhEngine()[1]));
          }

          final String xml = device.getXml();
          ScriptingEngine.pushCodeToGit(gitURL, ScriptingEngine.getFullBranch(gitURL),
              filename, xml, "new Robot content");

          logger.info("Clone finished.");
          Platform.runLater(() -> Notifications.create()
              .title("Clone Finished")
              .text("The creature cloning operation finished successfully.")
              .show());

          MobileBase mobileBase = new MobileBase(IOUtils.toInputStream(xml, "UTF-8"));
          mobileBase.setGitSelfSource(new String[]{gitURL, name + ".xml"});
          device.disconnect();

          DeviceManager.addConnection(mobileBase, mobileBase.getScriptingName());
          final String[] selfSource = mobileBase.getGitSelfSource();
          mainWindowController.loadCreatureLab(selfSource);
          mainWindowController.reloadGitMenus();
        } catch (MalformedURLException e) {
          logger.log(Level.SEVERE, "Could not make copy of creature. Malformed url.\n"
              + Throwables.getStackTraceAsString(e));

          Platform.runLater(() -> Notifications.create()
              .title("Error")
              .text("Could not make copy of creature.")
              .showError());
        } catch (Exception e) {
          logger.log(Level.SEVERE, "Could not make copy of creature."
              + Throwables.getStackTraceAsString(e));

          Platform.runLater(() -> Notifications.create()
              .title("Error")
              .text("Could not make copy of creature.")
              .showError());
        }

      }).start());
    }));

    //Have to declare these here because the following block is deeper scope
    final GridPane topLevelControls = new GridPane();
    topLevelControls.setPadding(new Insets(5));
    topLevelControls.add(makeCopy, 0, 0);

    final String[] gitSelfSource = device.getGitSelfSource();
    final String[] gitWalkingEngine = device.getGitWalkingEngine();
    final String[] gitCADEngine = device.getGitCadEngine();
    File deviceXMLFile;
    File deviceWalkingEngineFile;
    File deviceCADEngineFile;
    try {
      deviceXMLFile = ScriptingEngine.fileFromGit(gitSelfSource[0], gitSelfSource[1]);
      deviceWalkingEngineFile = ScriptingEngine.fileFromGit(gitWalkingEngine[0],
          gitWalkingEngine[1]);
      deviceCADEngineFile = ScriptingEngine.fileFromGit(gitCADEngine[0], gitCADEngine[1]);
    } catch (GitAPIException | IOException e) {
      logger.severe("Could not parse creature file from source: "
          + Arrays.toString(gitSelfSource) + "\n"
          + Arrays.toString(gitWalkingEngine) + "\n"
          + Throwables.getStackTraceAsString(e));

      Platform.runLater(() -> Notifications.create()
          .title("Error")
          .text("Could not check owner of creature.")
          .showError());

      return;
    }

    if (ScriptingEngine.checkOwner(deviceXMLFile)) {
      Button publish = new Button("Publish");
      publish.setGraphic(AssetFactory.loadIcon("Publish.png"));
      publish.setOnAction(event -> new PublishDialog().showAndWait().ifPresent(commitMessage -> {
        try {
          Git git = ScriptingEngine.locateGit(deviceXMLFile);
          String remote = git.getRepository().getConfig().getString("remote", "origin", "url");
          String relativePath = ScriptingEngine.findLocalPath(deviceXMLFile, git);

          //Push to existing gist
          ScriptingEngine.pushCodeToGit(remote, ScriptingEngine.getFullBranch(remote),
              relativePath, device.getXml(), commitMessage);
        } catch (Exception e) {
          logger.severe("Could not commit.\n" + Throwables.getStackTraceAsString(e));

          Platform.runLater(() -> Notifications.create()
              .title("Commit error")
              .text("Could not make commit.")
              .showError());
        }
      }));

      //Load walking and cad engine code into two tabs
      Platform.runLater(() -> {
        controller.loadFileIntoNewTab("Walking Engine",
            AssetFactory.loadIcon("Edit-Walking-Engine.png"),
            gitWalkingEngine[0], gitWalkingEngine[1], deviceWalkingEngineFile);

        controller.loadFileIntoNewTab("CAD Engine",
            AssetFactory.loadIcon("Edit-CAD-Engine.png"),
            gitCADEngine[0], gitCADEngine[1], deviceCADEngineFile);
      });

      Button editWalkingEngine = new Button("Edit Walking Engine");
      editWalkingEngine.setGraphic(AssetFactory.loadIcon("Edit-Walking-Engine.png"));
      editWalkingEngine.setOnAction(event ->
          controller.loadFileIntoNewTab("Walking Engine",
              AssetFactory.loadIcon("Edit-Walking-Engine.png"),
              gitWalkingEngine[0], gitWalkingEngine[1], deviceWalkingEngineFile));

      Button editCADEngine = new Button("Edit CAD Engine");
      editCADEngine.setGraphic(AssetFactory.loadIcon("Edit-CAD-Engine.png"));
      editCADEngine.setOnAction(event ->
          controller.loadFileIntoNewTab("CAD Engine",
              AssetFactory.loadIcon("Edit-CAD-Engine.png"),
              gitCADEngine[0], gitCADEngine[1], deviceCADEngineFile));

      Button setWalkingEngine = new Button("Set Walking Engine");
      setWalkingEngine.setGraphic(AssetFactory.loadIcon("Set-Walking-Engine.png"));
      setWalkingEngine.setOnAction(event ->
          new GistFileSelectionDialog("Select Walking Engine", file -> !file.endsWith(".xml"))
              .showAndWait().ifPresent(result -> device.setGitWalkingEngine(result)));

      Button setCADEngine = new Button("Set CAD Engine");
      setCADEngine.setGraphic(AssetFactory.loadIcon("Set-CAD-Engine.png"));
      setCADEngine.setOnAction(event ->
          new GistFileSelectionDialog("Select CAD Engine", file -> !file.endsWith(".xml"))
              .showAndWait().ifPresent(result -> device.setGitCadEngine(result)));

      GridPane.setHalignment(makeCopy, HPos.RIGHT);

      topLevelControls.add(publish, 1, 0);

      topLevelControls.add(editWalkingEngine, 0, 1);
      GridPane.setHalignment(editWalkingEngine, HPos.RIGHT);

      topLevelControls.add(editCADEngine, 1, 1);

      topLevelControls.add(setWalkingEngine, 0, 2);
      GridPane.setHalignment(setWalkingEngine, HPos.RIGHT);

      topLevelControls.add(setCADEngine, 1, 2);

      topLevelControls.setVgap(5);
      topLevelControls.setHgap(5);

      //TODO: Add limb selector to edit cad engine for limbs
    }

    Platform.runLater(() -> scriptTab.setContent(getScrollPane(new VBox(5, topLevelControls))));
  }

  private ScrollPane getScrollPane(Node node) {
    ScrollPane pane = new ScrollPane(node);
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
  private void promptAndAddLimb(String defaultFileName, MobileBase device,
      List<DHParameterKinematics> toAdd) {
    try {
      final String xmlContent = ScriptingEngine.codeFromGit(
          "https://gist.github.com/d11d69722610930ae1db9e5821a26178.git", defaultFileName)[0];
      final DHParameterKinematics newLeg = new DHParameterKinematics(null,
          IOUtils.toInputStream(xmlContent, "UTF-8"));

      final List<LinkConfiguration> linkConfigurations = newLeg.getLinkConfigurations();
      final AddLimbDialog dialog = new AddLimbDialog(newLeg.getScriptingName(),
          linkConfigurations.size(), getTakenChannels(device));

      dialog.showAndWait().ifPresent(result -> {
        newLeg.setScriptingName(result.name);

        for (int i = 0; i < linkConfigurations.size(); i++) {
          final LinkConfiguration conf = linkConfigurations.get(i);
          conf.setHardwareIndex(result.indices.get(i));
          newLeg.getFactory().refreshHardwareLayer(conf);
        }

        toAdd.add(newLeg);
        regenerateMenus();
      });
    } catch (Exception e) {
      logger.warning("Could not add limb.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onRegenCAD(ActionEvent actionEvent) {
    regenCAD();
  }

  @FXML
  private void onGenPrintableCAD(ActionEvent actionEvent) {
    genSTLs(device, cadManager, false);
  }

  @FXML
  private void onGenKinSTL(ActionEvent actionEvent) {
    genSTLs(device, cadManager, true);
  }

  /**
   * Regenerate {@link MobileBase} CAD if there is a non-null {@link MobileBaseCadManager}.
   */
  public void regenCAD() {
    if (cadManager != null) {
      cadManager.generateCad(); //TODO: Always regen CAD regardless of auto regen flag
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
  public void genSTLs(MobileBase device, MobileBaseCadManager cadManager,
      Boolean isKinematic) {
    final File defaultStlDir = new File(System.getProperty("user.home")
        + "/bowler-workspace/STL/");

    if (!defaultStlDir.exists() && !defaultStlDir.mkdirs()) {
      logger.log(Level.WARNING, "Could not create default directory to save STL files.");
      return;
    }

    Platform.runLater(() -> {
      final DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("Select Output Directory For STL files");

      chooser.setInitialDirectory(defaultStlDir);
      final File baseDirForFiles = chooser.showDialog(creatureTabPane.getScene().getWindow());
      if (baseDirForFiles == null) {
        logger.log(Level.INFO, "No directory selected. Not saving STL files.");
        return;
      }

      LoggerUtilities.newLoggingThread(logger, () -> {
        try {
          final List<File> files = cadManager.generateStls(device, baseDirForFiles, isKinematic);

          Platform.runLater(() ->
              Notifications.create()
                  .title("STL Export Success")
                  .text("All STL files for the creature generated at:\n"
                      + files.get(0).getAbsolutePath())
                  .showInformation());
        } catch (IOException e) {
          logger.log(Level.WARNING, "Could not generate STL files to save in: "
              + baseDirForFiles.getAbsolutePath()
              + "\n" + Throwables.getStackTraceAsString(e));

          Platform.runLater(() ->
              Notifications.create()
                  .title("STL Export Failure")
                  .text("Could not generate STL files.")
                  .showError());
        } catch (RuntimeException e) {
          if (e.getMessage().contains("IgenerateBed")) {
            logger.log(Level.INFO, "Cannot generate STL files because the supplied CAD manager "
                + "does not implement the IgenerateBed interface.\n"
                + Throwables.getStackTraceAsString(e));
          } else {
            logger.log(Level.WARNING, Throwables.getStackTraceAsString(e));
          }

          Platform.runLater(() ->
              Notifications.create()
                  .title("STL Export Failure")
                  .text("Could not generate STL files.")
                  .showError());
        }
      }).start();
    });
  }

  public ProgressIndicator getCadProgress() {
    return cadProgress;
  }

}
