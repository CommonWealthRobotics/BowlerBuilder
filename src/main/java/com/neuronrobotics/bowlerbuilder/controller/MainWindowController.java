/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller; // NOPMD

import static com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine.hasNetwork;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.plugin.Plugin;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.BowlerMobileBaseUI;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.ConnectionManagerFactory;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesServiceFactory;
import com.neuronrobotics.bowlerbuilder.view.dialog.AddFileToGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.GistFileSelectionDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.HelpDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.LoginDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PreferencesDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.plugin.ManagePluginsDialog;
import com.neuronrobotics.bowlerbuilder.view.tab.AbstractScriptEditorTab;
import com.neuronrobotics.bowlerbuilder.view.tab.AceCadEditorTab;
import com.neuronrobotics.bowlerbuilder.view.tab.CreatureLabTab;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.util.ThreadUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.controlsfx.control.Notifications;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPersonSet;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

@Singleton
@ParametersAreNonnullByDefault
public class MainWindowController {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(MainWindowController.class.getSimpleName());
  private final PreferencesServiceFactory preferencesServiceFactory;
  private final PreferencesService preferencesService;
  private final ConnectionManagerFactory connectionManagerFactory;

  @FXML private BorderPane root;
  @FXML private MenuItem logIn;
  @FXML private MenuItem logOut;
  @FXML private Menu myGists;
  @FXML private Menu myOrgs;
  @FXML private Menu myRepos;
  @FXML private Menu favorites;
  @FXML private Menu creatures;
  @FXML private Menu cadVitamins;
  @FXML private Menu installedPlugins;
  @FXML private TabPane tabPane;
  @FXML private Tab homeTab;
  @FXML private WebBrowserController webBrowserController;
  @FXML private Tab consoleTab;
  @FXML private TextArea console;
  @FXML private Tab connectionTab;
  @FXML private HBox connectionsHeader;
  @FXML private Accordion connections;

  @Inject
  protected MainWindowController(
      final PreferencesServiceFactory preferencesServiceFactory,
      final ConnectionManagerFactory connectionManagerFactory) {
    this.preferencesServiceFactory = preferencesServiceFactory;
    this.connectionManagerFactory = connectionManagerFactory;

    preferencesService = preferencesServiceFactory.create("MainWindowController");
    preferencesService.load();
  }

  @FXML
  protected void initialize() {
    consoleTab.setGraphic(AssetFactory.loadIcon("Command-Line.png"));
    connectionTab.setGraphic(AssetFactory.loadIcon("Connected-Devices.png"));

    // Add date to console
    console.setText(
        console.getText()
            + new SimpleDateFormat("HH:mm:ss, MM dd, yyyy", new Locale("en", "US"))
                .format(new Date())
            + "\n");

    // Redirect output to console
    PrintStream stream = null;
    try {
      stream = new PrintStream(new TextAreaPrintStream(console), true, "UTF-8");
    } catch (final UnsupportedEncodingException e) {
      LOGGER.severe("UTF-8 encoding not supported.");
    }

    System.setOut(stream);
    System.setErr(stream);

    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");

    SplitPane.setResizableWithParent(console, false);

    // Just needed to initialize here (ConnectionManager marked @singleton)
    connectionManagerFactory.get(connectionsHeader, connections);

    try {
      ScriptingEngine.runLogin();
      if (ScriptingEngine.isLoginSuccess() && hasNetwork()) {
        setupMenusOnLogin();

        LoggerUtilities.newLoggingThread(
                LOGGER,
                () -> {
                  Thread.currentThread().setName("ScriptingEngine Asset Preloader Thread");
                  try {
                    ScriptingEngine.filesInGit(AssetFactory.getGitSource(), "0.25.1", null);
                  } catch (final Exception e) {
                    LOGGER.warning(
                        "Could not preload assets.\n" + Throwables.getStackTraceAsString(e));
                  }
                })
            .start();
      }
    } catch (final IOException e) {
      LOGGER.info("Could not automatically log in.");
      logOut.setDisable(true); // Can't log out when not logged in
      logIn.setDisable(false);
    }

    reloadPlugins(preferencesService.get("Widgets", new ArrayList<>()));
  }

  @FXML
  private void openPreferences(final ActionEvent actionEvent) {
    new PreferencesDialog(preferencesServiceFactory.getAllPreferencesServices()).showAndWait();
  }

  @FXML
  private void onLogOutFromGitHub(final ActionEvent actionEvent) {
    try {
      ScriptingEngine.logout();
      logIn.setDisable(false);
      logOut.setDisable(true);
      myGists.getItems().clear();
      myOrgs.getItems().clear();
      myRepos.getItems().clear();
    } catch (final IOException e) {
      LOGGER.log(
          Level.SEVERE, "Could not log out from GitHub.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onDeleteLocalCache(final ActionEvent actionEvent) {
    final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

    alert.setTitle("Confirm Deletion");
    alert.setHeaderText("Delete All Local Files and Quit?");
    alert.setContentText("Deleting the cache will remove unsaved work and quit. Are you sure?");

    if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
      LoggerUtilities.newLoggingThread(
              LOGGER,
              () -> {
                Thread.currentThread().setName("Delete Cache Thread");

                try {
                  FileUtils.deleteDirectory(
                      new File(ScriptingEngine.getWorkspace().getAbsolutePath() + "/gistcache/"));
                } catch (IOException e) {
                  LOGGER.log(
                      Level.SEVERE,
                      "Unable to delete cache.\n" + Throwables.getStackTraceAsString(e));
                }

                Platform.runLater(this::quit);
              })
          .start();
    }
  }

  @FXML
  private void onExitProgram(final ActionEvent actionEvent) {
    saveAndQuit();
  }

  @FXML
  private void onLogInToGitHub(final ActionEvent actionEvent) {
    tryLogin();
  }

  @FXML
  private void onReloadMenus(final ActionEvent actionEvent) {
    reloadGitMenus();
  }

  @FXML
  private void onOpenScratchpad(final ActionEvent actionEvent) {
    final AceCadEditorTab tab = new AceCadEditorTab("Scratchpad");
    final AceCadEditorTabController controller = tab.getController();

    controller.getAceScriptEditorController().initScratchpad(tab, this::reloadGitMenus);

    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);
  }

  @FXML
  private void onLoadCreature(final ActionEvent actionEvent) {
    final GistFileSelectionDialog dialog =
        new GistFileSelectionDialog("Select Creature File", file -> file.endsWith(".xml"));
    dialog.showAndWait().ifPresent(result -> loadCreatureLab(result[0], result[1]));
  }

  @FXML
  private void onReloadVitamins(final ActionEvent actionEvent) {
    reloadCadMenus();
  }

  @FXML
  private void onManageWidgets(final ActionEvent actionEvent) {
    final ManagePluginsDialog dialog =
        new ManagePluginsDialog(
            FXCollections.observableArrayList(
                preferencesService.get("Widgets", new ArrayList<>())));
    dialog
        .showAndWait()
        .ifPresent(
            widgets -> {
              preferencesService.set("Widgets", new ArrayList<>(widgets));
              reloadPlugins(widgets);
            });
  }

  @FXML
  private void openEditorHelp(final ActionEvent actionEvent) {
    new HelpDialog().showAndWait();
  }

  /**
   * Load a page into the home WebView.
   *
   * @param url URL to load
   */
  public void loadPage(final String url) {
    Platform.runLater(() -> webBrowserController.loadPage(url));
  }

  /**
   * Load a page into a new Tab.
   *
   * @param tabName name for new tab
   * @param url URL to load
   */
  public void loadPageIntoNewTab(final String tabName, final String url) {
    Platform.runLater(
        () -> {
          final FXMLLoader loader =
              new FXMLLoader(
                  MainWindowController.class.getResource(
                      "/com/neuronrobotics/bowlerbuilder/view/WebBrowser.fxml"));

          try {
            final Tab tab = new Tab(tabName, loader.load());
            final WebBrowserController controller = loader.getController();
            controller.loadPage(url);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
          } catch (final IOException e) {
            LOGGER.log(
                Level.SEVERE, "Could not load WebBrowser.\n" + Throwables.getStackTraceAsString(e));
          }
        });
  }

  /**
   * Open a gist file in the file editor.
   *
   * @param gist Gist containing file
   * @param gistFile File
   */
  public void openGistFileInEditor(final GHGist gist, final GHGistFile gistFile) {
    Platform.runLater(
        () -> {
          final AceCadEditorTab tab = new AceCadEditorTab(gistFile.getFileName());
          final AceCadEditorTabController controller = tab.getController();

          controller.getAceScriptEditorController().loadGist(gist, gistFile);

          tabPane.getTabs().add(tab);
          tabPane.getSelectionModel().select(tab);
        });
  }

  /**
   * Load a MobileBase from the supplied file and open it in a new {@link CreatureLabTab}.
   *
   * @param gist gist clone URL
   * @param fileName file name (with .xml extension)
   */
  public void loadCreatureLab(final String gist, final String fileName) {
    loadCreatureLab(new String[] {gist, fileName});
  }

  /**
   * Load a MobileBase from the supplied file and open it in a new {@link CreatureLabTab}.
   *
   * @param file xml file in gist
   */
  public void loadCreatureLab(final String[] file) { // NOPMD
    Platform.runLater(
        () -> {
          final CreatureLabTab tab = new CreatureLabTab("Creature Lab");
          LoggerUtilities.newLoggingThread(
                  LOGGER,
                  () -> {
                    final AceCreatureLabController controller = tab.getController();

                    try {
                      final String xmlContent = ScriptingEngine.codeFromGit(file[0], file[1])[0];

                      final MobileBase mobileBase =
                          new MobileBase(IOUtils.toInputStream(xmlContent, "UTF-8"));
                      mobileBase.setGitSelfSource(file);
                      mobileBase.connect();

                      final MobileBaseCadManager mobileBaseCadManager =
                          new MobileBaseCadManager(
                              mobileBase,
                              new BowlerMobileBaseUI(
                                  controller.getCadModelViewerController().getEngine()));
                      mobileBase.updatePositions();

                      DeviceManager.addConnection(mobileBase, mobileBase.getScriptingName());
                      controller
                          .getCreatureEditorController()
                          .generateMenus(mobileBase, mobileBaseCadManager, controller);

                      mobileBaseCadManager.generateCad();
                      LOGGER.log(Level.INFO, "Waiting for cad to generate.");

                      controller
                          .getCreatureEditorController()
                          .getCadProgress()
                          .progressProperty()
                          .bind(MobileBaseCadManager.get(mobileBase).getProcesIndictor());
                      ThreadUtil.wait(1000);
                      while (MobileBaseCadManager.get(mobileBase).getProcesIndictor().get() < 1) {
                        ThreadUtil.wait(1000);
                      }
                    } catch (final IOException e) {
                      LOGGER.log(
                          Level.SEVERE,
                          "Could not load assets for robot.\n"
                              + Throwables.getStackTraceAsString(e));
                    } catch (final Exception e) {
                      LOGGER.log(
                          Level.SEVERE,
                          "Could not start building robot.\n"
                              + Throwables.getStackTraceAsString(e));
                    }
                  })
              .start();

          tabPane.getTabs().add(tab);
          tabPane.getSelectionModel().select(tab);
        });
  }

  private void tryLogin() {
    ScriptingEngine.setLoginManager(
        loginID -> {
          final LoginDialog dialog = new LoginDialog();

          final Optional<Boolean> result = dialog.showAndWait();
          if (result.isPresent() && result.get()) {
            return new String[] {dialog.getName(), dialog.getPassword()};
          } else {
            return new String[0];
          }
        });

    try {
      ScriptingEngine.waitForLogin();
      if (ScriptingEngine.isLoginSuccess() && hasNetwork()) {
        Platform.runLater(
            () ->
                Notifications.create()
                    .title("Login Success")
                    .text("Successfully logged in to GitHub as " + ScriptingEngine.getLoginID())
                    .show());
        setupMenusOnLogin();
      }
    } catch (final IOException e) {
      LOGGER.log(
          Level.SEVERE,
          "Could not launch GitHub as non-anonymous.\n" + Throwables.getStackTraceAsString(e));
      try {
        ScriptingEngine.setupAnyonmous();
      } catch (final IOException e1) {
        LOGGER.log(
            Level.SEVERE,
            "Could not launch GitHub anonymous.\n" + Throwables.getStackTraceAsString(e));
      }
    } catch (final GitAPIException e) {
      LOGGER.log(Level.SEVERE, "Could not log in.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /** Setup the menus for the main menu bar. */
  private void setupMenusOnLogin() {
    try {
      ScriptingEngine.setAutoupdate(true);
    } catch (final IOException e) {
      LOGGER.log(
          Level.SEVERE, "Could not set auto update.\n" + Throwables.getStackTraceAsString(e));
    }

    logIn.setDisable(true);
    logOut.setDisable(false);

    reloadGitMenus();
    reloadCadMenus();
  }

  /** Reload the GitHub-related menus. */
  public void reloadGitMenus() {
    Platform.runLater(
        () -> {
          // Wait for GitHub to load in
          GitHub gitHub;

          while ((gitHub = ScriptingEngine.getGithub()) == null) {
            ThreadUtil.wait(20);
          }

          myGists.getItems().clear();
          myOrgs.getItems().clear();
          myRepos.getItems().clear();
          favorites.getItems().clear();
          creatures.getItems().clear();

          final GHMyself myself;
          try {
            myself = gitHub.getMyself();

            LoggerUtilities.newLoggingThread(
                    LOGGER,
                    () -> {
                      try {
                        loadGistsIntoMenus(myGists, myself.listGists());
                      } catch (IOException e) {
                        LOGGER.log(
                            Level.SEVERE,
                            "Unable to list gists.\n" + Throwables.getStackTraceAsString(e));
                      }
                    })
                .start();

            LoggerUtilities.newLoggingThread(
                    LOGGER,
                    () -> {
                      try {
                        loadOrgsIntoMenus(myOrgs, myself.getAllOrganizations());
                      } catch (IOException e) {
                        LOGGER.log(
                            Level.SEVERE,
                            "Unable to get organizations.\n" + Throwables.getStackTraceAsString(e));
                      }
                    })
                .start();

            LoggerUtilities.newLoggingThread(
                    LOGGER, () -> loadReposIntoMenus(myRepos, myself.listRepositories()))
                .start();

            LoggerUtilities.newLoggingThread(LOGGER, () -> loadFavoritesIntoMenus(favorites))
                .start();

            LoggerUtilities.newLoggingThread(LOGGER, () -> loadCreaturesIntoMenus(creatures))
                .start();
          } catch (final IOException e) {
            LOGGER.log(
                Level.SEVERE, "Could not get GitHub.\n" + Throwables.getStackTraceAsString(e));
          }
        });
  }

  /** Reload the CAD menus. */
  public void reloadCadMenus() {
    Platform.runLater(
        () -> {
          cadVitamins.getItems().clear();

          LoggerUtilities.newLoggingThread(
                  LOGGER,
                  () ->
                      cadVitamins
                          .getItems()
                          .addAll(getAllVitaminMenus(Vitamins.listVitaminTypes(), tabPane)))
              .start();
        });
  }

  /**
   * Creates Vitamin menus for the given types.
   *
   * @param vitaminTypes vitamin types
   * @param tabPane tab pane to check for an {@link AbstractScriptEditorTab} in to insert the
   *     selection
   * @return the vitamin menus
   */
  @Nonnull
  private static Collection<Menu> getAllVitaminMenus(
      final Collection<String> vitaminTypes, final TabPane tabPane) {
    return vitaminTypes
        .stream()
        .sorted()
        .map(type -> getVitaminMenu(type, tabPane))
        .collect(Collectors.toList());
  }

  /**
   * Creates one Vitamin menu. Lists all the sizes for a given type.
   *
   * @param vitaminType vitamin type
   * @param tabPane tab pane to check for an {@link AbstractScriptEditorTab} in to insert the
   *     selection
   * @return the vitamin menu
   */
  @Nonnull
  private static Menu getVitaminMenu(final String vitaminType, final TabPane tabPane) {
    final Menu vitaminMenu = new Menu(vitaminType);

    vitaminMenu
        .getItems()
        .addAll(
            Vitamins.listVitaminSizes(vitaminType)
                .stream()
                .sorted()
                .map(
                    size -> {
                      final MenuItem sizeMenu = new MenuItem(size);

                      sizeMenu.setOnAction(
                          __ -> {
                            final Tab selection = tabPane.getSelectionModel().getSelectedItem();
                            if (selection instanceof AbstractScriptEditorTab) {
                              final AbstractScriptEditorTab editorTab =
                                  (AbstractScriptEditorTab) selection;

                              final String vitaminInsertionString =
                                  "CSG foo = Vitamins.get(\""
                                      + vitaminType
                                      + "\", \""
                                      + size
                                      + "\");";

                              editorTab
                                  .getScriptEditorView()
                                  .getScriptEditor()
                                  .insertAtCursor(vitaminInsertionString);
                            }
                          });

                      return sizeMenu;
                    })
                .collect(Collectors.toList()));

    return vitaminMenu;
  }

  /**
   * Load gists into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param gists list of gists
   */
  private void loadGistsIntoMenus(final Menu menu, final Iterable<GHGist> gists) {
    gists.forEach(
        gist -> {
          final MenuItem showWebGist = new MenuItem("Show Gist on Web");
          showWebGist.setOnAction(
              event -> loadPageIntoNewTab(gist.getDescription(), gist.getHtmlUrl()));

          final MenuItem addFileToGist = new MenuItem("Add File");
          addFileToGist.setOnAction(
              event ->
                  Platform.runLater(
                      () -> {
                        final AddFileToGistDialog dialog = new AddFileToGistDialog();
                        dialog
                            .showAndWait()
                            .ifPresent(
                                name -> {
                                  try {
                                    ScriptingEngine.pushCodeToGit(
                                        gist.getGitPushUrl(),
                                        ScriptingEngine.getFullBranch(gist.getGitPushUrl()),
                                        name,
                                        "//Your code here",
                                        "New file");
                                    reloadGitMenus();
                                    ScriptingEngine.getGithub()
                                        .getMyself()
                                        .listGists()
                                        .asList()
                                        .stream()
                                        .filter(item -> item.equals(gist))
                                        .findFirst()
                                        .ifPresent(
                                            newGist ->
                                                openGistFileInEditor(
                                                    newGist, newGist.getFile(name)));
                                  } catch (final Exception e) {
                                    LOGGER.log(
                                        Level.SEVERE,
                                        "Could not add file to gist.\n"
                                            + Throwables.getStackTraceAsString(e));
                                  }
                                });
                      }));

          final MenuItem addFileFromDisk = new MenuItem("Add File from Disk");
          addFileFromDisk.setOnAction(
              event -> {
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select File to Add");
                final File selection = fileChooser.showOpenDialog(root.getScene().getWindow());
                if (selection != null && selection.isFile()) {
                  try {
                    ScriptingEngine.pushCodeToGit(
                        gist.getGitPushUrl(),
                        ScriptingEngine.getFullBranch(gist.getGitPushUrl()),
                        selection.getName(),
                        Files.readLines(selection, Charsets.UTF_8)
                            .stream()
                            .collect(Collectors.joining("\n")),
                        "Add file: " + selection.getName());
                    reloadGitMenus();
                  } catch (final Exception e) {
                    LOGGER.log(
                        Level.SEVERE,
                        "Could not add file from disk to gist.\n"
                            + Throwables.getStackTraceAsString(e));
                  }
                }
              });

          final String gistID = ScriptingEngine.urlToGist(gist.getGitPushUrl());

          String gistMenuText = gist.getDescription();
          if (gistMenuText == null || gistMenuText.length() == 0) {
            final Set<String> filenames = gist.getFiles().keySet();
            if (filenames.size() >= 1) {
              gistMenuText = filenames.iterator().next();
            } else {
              gistMenuText = "";
            }
          }

          // Cap length
          gistMenuText = gistMenuText.substring(0, Math.min(25, gistMenuText.length()));

          final Menu gistMenu = new Menu(gistMenuText);
          gistMenu.getItems().addAll(showWebGist, addFileToGist, addFileFromDisk);

          try {
            final GHMyself myself = ScriptingEngine.getGithub().getMyself();
            final Iterable<GHGist> reloadGists = myself.listGists();

            final Runnable reloadFavorites =
                () -> {
                  favorites.getItems().clear();
                  myGists.getItems().clear();

                  loadFavoritesIntoMenus(favorites);
                  loadGistsIntoMenus(myGists, reloadGists);
                };

            final HashSet<String> favoriteGists =
                preferencesService.get("Favorite Gists", new HashSet<>());
            if (favoriteGists.contains(gistID)) {
              final MenuItem favoriteGist = new MenuItem("Unfavorite");
              favoriteGist.setOnAction(
                  event -> {
                    favoriteGists.remove(gistID);

                    // Reload gists and favorites
                    LoggerUtilities.newLoggingThread(LOGGER, reloadFavorites).start();
                  });
              gistMenu.getItems().add(favoriteGist);
            } else {
              final MenuItem favoriteGist = new MenuItem("Favorite");
              favoriteGist.setOnAction(
                  event -> {
                    favoriteGists.add(gistID);

                    // Reload gists and favorites
                    LoggerUtilities.newLoggingThread(LOGGER, reloadFavorites).start();
                  });
              gistMenu.getItems().add(favoriteGist);
            }

          } catch (IOException e) {
            LOGGER.warning("Could not get user's gists.\n" + Throwables.getStackTraceAsString(e));
          }

          gist.getFiles()
              .forEach(
                  (name, gistFile) -> {
                    if (name.endsWith(".xml")) {
                      final MenuItem openGist = new MenuItem("Open File");
                      openGist.setOnAction(event -> openGistFileInEditor(gist, gistFile));
                      final MenuItem loadCreature = new MenuItem("Open as Creature");
                      loadCreature.setOnAction(
                          event -> loadCreatureLab(gist.getGitPushUrl(), gistFile.getFileName()));

                      final Menu gistFileItem = new Menu(name);
                      gistFileItem.getItems().addAll(openGist, loadCreature);
                      gistMenu.getItems().add(gistFileItem);
                    } else {
                      final MenuItem gistFileItem = new MenuItem(name);
                      gistFileItem.setOnAction(event -> openGistFileInEditor(gist, gistFile));
                      gistMenu.getItems().add(gistFileItem);
                    }
                  });

          menu.getItems().add(gistMenu);
        });
  }

  /**
   * Load organizations into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param orgs organizations
   */
  private void loadOrgsIntoMenus(final Menu menu, final GHPersonSet<GHOrganization> orgs) {
    final Function<GHOrganization, String> getName =
        org -> {
          try {
            String name = org.getName();
            if (name == null || name.length() == 0) {
              name = org.getLogin();
            }
            return name;
          } catch (final IOException e) {
            LOGGER.log(
                Level.SEVERE,
                "Error while sanitizing organization name.\n"
                    + Throwables.getStackTraceAsString(e));
          }

          return "";
        };

    orgs.stream()
        .sorted(Comparator.comparing(getName))
        .forEach(
            org -> {
              try {
                final Menu orgMenu = new Menu(getName.apply(org));
                org.getRepositories()
                    .forEach(
                        (key, value) -> {
                          final MenuItem repoMenu = new MenuItem(key);
                          repoMenu.setOnAction(
                              event -> {
                                loadPageIntoNewTab(
                                    value
                                        .getDescription()
                                        .substring(
                                            0, Math.min(15, value.getDescription().length())),
                                    value.gitHttpTransportUrl());
                                event.consume();
                              });
                          orgMenu.getItems().add(repoMenu);
                        });

                orgMenu.setOnAction(
                    event -> {
                      try {
                        loadPageIntoNewTab(org.getName(), org.getHtmlUrl());
                      } catch (final IOException e) {
                        LOGGER.log(
                            Level.SEVERE,
                            "Could not get organization name when loading new tab.\n"
                                + Throwables.getStackTraceAsString(e));
                      }
                    });

                menu.getItems().add(orgMenu);
              } catch (final IOException e) {
                LOGGER.log(
                    Level.SEVERE,
                    "Unable to get name of organization.\n" + Throwables.getStackTraceAsString(e));
              }
            });
  }

  /**
   * Load repositories into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param repos repositories
   */
  private void loadReposIntoMenus(final Menu menu, final PagedIterable<GHRepository> repos) {
    // Map the repo owner's name to the repos it falls under to group similar repos together
    final Multimap<String, GHRepository> repoMap =
        MultimapBuilder.treeKeys().arrayListValues().build();
    repos.forEach(repo -> repoMap.put(repo.getOwnerName(), repo));

    menu.getItems()
        .addAll(
            // One Menu per owner
            repoMap
                .keySet()
                .stream()
                .sorted()
                .sequential()
                .map(repoOwner -> createRepoOwnerMenu(repoOwner, repoMap.get(repoOwner)))
                .collect(Collectors.toList()));
  }

  /**
   * Create a Menu for a repository owner containing all the repos under that owner.
   *
   * @param repoOwner the repo owner
   * @param repos the repos the owner owns
   * @return the Menu
   */
  @Nonnull
  private Menu createRepoOwnerMenu(final String repoOwner, final Collection<GHRepository> repos) {
    final Menu repoOwnerMenu = new Menu(repoOwner);

    // One MenuItem per repo
    repoOwnerMenu
        .getItems()
        .addAll(
            repos
                .stream()
                .sorted(Comparator.comparing(GHRepository::getName))
                .sequential()
                .map(this::createRepoMenuItem)
                .collect(Collectors.toList()));

    return repoOwnerMenu;
  }

  /**
   * Create a MenuItem for a repo.
   *
   * @param repo the repo
   * @return the MenuItem
   */
  @Nonnull
  private MenuItem createRepoMenuItem(final GHRepository repo) {
    final MenuItem menuItem = new MenuItem(repo.getName());

    menuItem.setOnAction(
        event ->
            loadPageIntoNewTab(
                repo.getName().substring(0, Math.min(15, repo.getName().length())),
                repo.gitHttpTransportUrl()));

    return menuItem;
  }

  /**
   * Loads the saved favorite gists into MenuItems and adds one Menu to the input Menu.
   *
   * @param menu the Menu to side-effect
   */
  private void loadFavoritesIntoMenus(final Menu menu) {
    final HashSet<String> gistIDs = preferencesService.get("Favorite Gists", new HashSet<>());
    final GitHub gitHub = ScriptingEngine.getGithub();

    final List<GHGist> gists =
        gistIDs
            .stream()
            .map(
                gistID -> {
                  try {
                    return gitHub.getGist(gistID);
                  } catch (IOException e) {
                    LOGGER.warning(
                        "Unable to get GHGist from gist ID when loading favorites.\n"
                            + Throwables.getStackTraceAsString(e));
                  }
                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    loadGistsIntoMenus(menu, gists);
  }

  private void loadCreaturesIntoMenus(final Menu menu) {
    try {
      final String gistURL =
          preferencesService.get(
              "Default Creatures Push URL",
              "https://gist.github.com/e72d6c298cfc02cc5b5f11061cd99702.git");
      final String gistName =
          preferencesService.get("Default Creatures Filename", "defaultCreatures.json");
      final String[] code = ScriptingEngine.codeFromGit(gistURL, gistName);

      if (code != null) {
        final String content = code[0];
        final JSONParser parser = new JSONParser();
        final JSONObject result = (JSONObject) parser.parse(content);

        final JSONArray jsonArray = (JSONArray) result.get("files");
        for (final Object element : jsonArray) {
          final JSONObject jsonObject = (JSONObject) element;
          final String pushURL = (String) jsonObject.get("pushURL");
          final String fileName = (String) jsonObject.get("fileName");
          final String creatureName = (String) jsonObject.get("creatureName");

          final MenuItem menuItem = new MenuItem(creatureName);
          menuItem.setOnAction(event -> loadCreatureLab(pushURL, fileName));
          menu.getItems().add(menuItem);
        }
      }
    } catch (Exception e) {
      LOGGER.warning("Could not load default creatures.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void reloadPlugins(final Collection<Plugin> plugins) {
    installedPlugins.getItems().clear();
    installedPlugins
        .getItems()
        .addAll(
            plugins
                .stream()
                .map(
                    plugin -> {
                      final MenuItem item = new MenuItem(plugin.getDisplayName());
                      item.setOnAction(
                          event -> {
                            try {
                              plugin.run();
                            } catch (Exception e) {
                              LOGGER.log(
                                  Level.SEVERE,
                                  "Unable to run plugin "
                                      + plugin.getGitSource()
                                      + "\n"
                                      + Throwables.getStackTraceAsString(e));
                            }
                          });
                      return item;
                    })
                .collect(Collectors.toList()));
  }

  /** Save work and quit. */
  public void saveAndQuit() {
    preferencesServiceFactory.saveAllCached();
    quit();
  }

  /** Quit the application. */
  private void quit() {
    root.getScene().getWindow().hide();
    Platform.exit();

    // Need to make sure the VM exits; sometimes a rouge thread is running
    final Timer timer = new Timer(true);
    timer.schedule(
        new TimerTask() {
          @Override
          @SuppressFBWarnings(value = "DM_RUN_FINALIZERS_ON_EXIT") // NOPMD
          public void run() {
            LOGGER.log(
                Level.SEVERE,
                "Still alive for some reason. Printing threads and " + "killing VM...");

            final Set<Thread> threads = Thread.getAllStackTraces().keySet();
            final StringBuilder threadString = new StringBuilder();
            threads.forEach(item -> threadString.append(item).append("\n"));
            LOGGER.log(Level.FINE, threadString.toString());

            Runtime.getRuntime().exit(1); // Abnormal exit
          }
        },
        10000); // Wait 10 seconds before killing the VM
  }

  /**
   * Add a tab to the tab pane.
   *
   * @param tab tab to add
   */
  public void addTab(final Tab tab) {
    Platform.runLater(() -> tabPane.getTabs().add(tab));
  }

  /**
   * Get the currently selected tab.
   *
   * @return current tab
   */
  public Optional<Tab> getSelectedTab() {
    try {
      return Optional.of(FxUtil.returnFX(() -> tabPane.getSelectionModel().getSelectedItem()));
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.log(
          Level.SEVERE, "Could not get selected tab.\n" + Throwables.getStackTraceAsString(e));
    }

    return Optional.empty();
  }

  // Simple stream to append input characters to a text area
  @ParametersAreNonnullByDefault
  private static class TextAreaPrintStream extends OutputStream {

    private final TextArea textArea;

    public TextAreaPrintStream(final TextArea textArea) {
      super();
      this.textArea = textArea;
    }

    @Override
    public void write(final int character) {
      Platform.runLater(() -> textArea.appendText(String.valueOf((char) character)));
    }
  }
}
