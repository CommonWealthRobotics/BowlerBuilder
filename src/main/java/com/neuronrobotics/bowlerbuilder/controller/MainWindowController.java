package com.neuronrobotics.bowlerbuilder.controller; //NOPMD

import static com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine.hasNetwork;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.view.FileEditorTab;
import com.neuronrobotics.bowlerbuilder.model.BeanPropertySheetItem;
import com.neuronrobotics.bowlerbuilder.model.Preferences;
import com.neuronrobotics.bowlerbuilder.view.dialog.AddFileToGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.HelpDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.LoginDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PropertySheet;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPersonSet;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

public class MainWindowController implements Initializable {

  //Open file editors
  private final List<FileEditorController> fileEditors = new ArrayList<>();
  private final Preferences preferences;
  private final IntegerProperty fontSizePref = new SimpleIntegerProperty(null, "Font Size");
  @FXML
  private BorderPane root;
  @FXML
  private MenuItem logOut;
  @FXML
  private Menu myGists;
  @FXML
  private Menu myOrgs;
  @FXML
  private Menu myRepos;
  @FXML
  private Menu cadVitamins;
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab homeTab;
  @FXML
  private WebBrowserController webBrowserController;
  @FXML
  private TextArea console;

  public MainWindowController() {
    //TODO: Load previous font size preference
    Map<String, Property> previousPreferences = new HashMap<>();
    previousPreferences.put("Font Size", fontSizePref);
    preferences = new Preferences(previousPreferences);
    fontSizePref.addListener((observableValue, oldVal, newVal) ->
        fileEditors.forEach(editor -> editor.setFontSize(fontSizePref)));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    //Add date to console
    console.setText(console.getText()
        + new SimpleDateFormat(
        "HH:mm:ss, MM dd, yyyy",
        new Locale("en", "US")).format(new Date())
        + "\n");

    //Redirect output to console
    PrintStream stream = null;
    try {
      stream = new PrintStream(new TextAreaPrintStream(console), true, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LoggerUtilities.getLogger().log(Level.WARNING, "UTF-8 encoding unsupported.");
    }
    System.setOut(stream);
    System.setErr(stream);

    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");

    SplitPane.setResizableWithParent(console, false);

    try {
      ScriptingEngine.runLogin();
      if (ScriptingEngine.isLoginSuccess() && hasNetwork()) {
        showLoginNotification();
        setupMenusOnLogin();
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not automatically log in.\n");
      logOut.setDisable(true); //Can't log out when not logged in
    }
  }

  @FXML
  private void onOpenScratchpad(ActionEvent actionEvent) {
    FileEditorTab tab;
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/FileEditor.fxml"));

    try {
      Node node = loader.load();
      final FileEditorController controller = loader.getController();
      fileEditors.add(controller);

      tab = new FileEditorTab("Scratchpad", controller);
      tab.setContent(node);

      controller.setFontSize(preferences.get("Font Size"));
      controller.initScratchpad(tab, this::reloadGitMenus);

      tab.setOnCloseRequest(event -> fileEditors.remove(controller));

      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load FileEditor.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onLogOutFromGitHub(ActionEvent actionEvent) {
    try {
      ScriptingEngine.logout();
      logOut.setDisable(true);
      myGists.getItems().clear();
      myOrgs.getItems().clear();
      myRepos.getItems().clear();
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not log out from GitHub.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onDeleteLocalCache(ActionEvent actionEvent) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

    alert.setTitle("Confirm Deletion");
    alert.setHeaderText("Delete All Local Files and Quit?");
    alert.setContentText("Deleting the cache will remove unsaved work and quit. Are you sure?");

    if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
      new Thread(() -> {
        Thread.currentThread().setName("Delete Cache Thread");

        try {
          FileUtils.deleteDirectory(
              new File(
                  ScriptingEngine.getWorkspace().getAbsolutePath() + "/gistcache/"));
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Unable to delete cache.\n" + Throwables.getStackTraceAsString(e));
        }

        Platform.runLater(this::quit);
      }).start();
    }
  }

  @FXML
  private void openPreferences(ActionEvent actionEvent) {
    PropertySheet propertySheet = new PropertySheet(FXCollections.observableArrayList(
        preferences.getAllProperties().stream()
            .map(BeanPropertySheetItem::new)
            .collect(Collectors.toList())));

    BorderPane root = new BorderPane();
    root.setCenter(propertySheet);

    Dialog dialog = new Dialog();
    dialog.getDialogPane().setContent(root);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    dialog.showAndWait();
  }

  @FXML
  private void openEditorHelp(ActionEvent actionEvent) {
    new HelpDialog().showAndWait();
  }

  @FXML
  private void onExitProgram(ActionEvent actionEvent) {
    saveAndQuit();
  }

  @FXML
  private void onLogInToGitHub(ActionEvent actionEvent) {
    tryLogin();
  }

  @FXML
  private void onReloadMenus(ActionEvent actionEvent) {
    reloadGitMenus();
  }

  @FXML
  private void onReloadVitamins(ActionEvent actionEvent) {
    reloadCadMenus();
  }

  /**
   * Load a page into the home WebView.
   *
   * @param url URL to load
   */
  private void loadPage(String url) {
    webBrowserController.loadPage(url);
  }

  /**
   * Load a page into a new Tab.
   *
   * @param tabName name for new tab
   * @param url URL to load
   */
  private void loadPageIntoNewTab(String tabName, String url) {
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/WebBrowser.fxml"));

    try {
      Tab tab = new Tab(tabName, loader.load());
      WebBrowserController controller = loader.getController();
      controller.loadPage(url);
      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load WebBrowser.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Open a gist file in the file editor.
   *
   * @param gist Gist containing file
   * @param gistFile File
   */
  public void openGistFileInEditor(GHGist gist, GHGistFile gistFile) {
    FileEditorTab tab;
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/FileEditor.fxml"));

    try {
      Node node = loader.load();
      final FileEditorController controller = loader.getController();
      fileEditors.add(controller);

      tab = new FileEditorTab(gistFile.getFileName(), controller);
      tab.setContent(node);

      //      controller.setFontSize((int) preferences.get("Font Size"));
      controller.loadGist(gist, gistFile);

      tab.setOnCloseRequest(event -> fileEditors.remove(controller));

      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load FileEditor.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void tryLogin() {
    ScriptingEngine.setLoginManager(s -> {
      LoginDialog dialog = new LoginDialog();

      final Optional<Boolean> result = dialog.showAndWait();
      if (result.isPresent() && result.get()) {
        return new String[]{dialog.getName(), dialog.getPassword()};
      } else {
        return new String[0];
      }
    });

    try {
      ScriptingEngine.waitForLogin();
      if (ScriptingEngine.isLoginSuccess() && hasNetwork()) {
        showLoginNotification();
        setupMenusOnLogin();
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not launch GitHub as non-anonymous.\n" + Throwables.getStackTraceAsString(e));
      try {
        ScriptingEngine.setupAnyonmous();
      } catch (IOException e1) {
        LoggerUtilities.getLogger().log(Level.WARNING,
            "Could not launch GitHub anonymous.\n" + Throwables.getStackTraceAsString(e));
      }
    } catch (GitAPIException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not log in.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Show a GitHub login toast.
   */
  private void showLoginNotification() {
    Platform.runLater(() -> {
      try {
        Notifications.create()
            .title("Login Success")
            .text(ScriptingEngine.getGithub().getMyself().getLogin())
            .owner(root)
            .position(Pos.BOTTOM_RIGHT)
            .showInformation();
      } catch (IOException e) {
        LoggerUtilities.getLogger().log(Level.WARNING,
            "Unable to get GitHub.\n" + Throwables.getStackTraceAsString(e));
      }
    });
  }

  /**
   * Setup the menus for the main menu bar.
   */
  private void setupMenusOnLogin() {
    try {
      ScriptingEngine.setAutoupdate(true);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not set auto update.\n" + Throwables.getStackTraceAsString(e));
    }

    logOut.setDisable(false);

    reloadGitMenus();
    reloadCadMenus();
  }

  /**
   * Reload the GitHub-related menus.
   */
  public void reloadGitMenus() {
    //Wait for GitHub to load in
    GitHub gitHub;

    while ((gitHub = ScriptingEngine.getGithub()) == null) {
      ThreadUtil.wait(20);
    }

    myGists.getItems().clear();
    myOrgs.getItems().clear();
    myRepos.getItems().clear();

    GHMyself myself;
    try {
      myself = gitHub.getMyself();

      LoggerUtilities.newLoggingThread(() -> {
        try {
          loadGistsIntoMenus(myGists, myself.listGists());
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.SEVERE,
              "Unable to list gists.\n" + Throwables.getStackTraceAsString(e));
        }
      }).start();

      LoggerUtilities.newLoggingThread(() -> {
        try {
          loadOrgsIntoMenus(myOrgs, myself.getAllOrganizations());
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.SEVERE,
              "Unable to get organizations.\n" + Throwables.getStackTraceAsString(e));
        }
      }).start();

      LoggerUtilities.newLoggingThread(() ->
          loadReposIntoMenus(myRepos, myself.listRepositories())).start();
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not get GitHub.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Reload the CAD menus.
   */
  public void reloadCadMenus() {
    cadVitamins.getItems().clear();

    LoggerUtilities.newLoggingThread(() ->
        Vitamins.listVitaminTypes().stream().sorted().forEach(vitamin -> {

          Menu vitaminMenu = new Menu(vitamin);

          Vitamins.listVitaminSizes(vitamin).stream().sorted().forEach(size -> {
            MenuItem sizeMenu = new MenuItem(size);

            sizeMenu.setOnAction(event1 -> {
              Tab selection = tabPane.getSelectionModel().getSelectedItem();
              if (selection instanceof FileEditorTab) {
                FileEditorTab editorTab = (FileEditorTab) selection;
                String insertion =
                    "CSG foo = Vitamins.get(\"" + vitamin + "\", \"" + size + "\");";
                editorTab.getController().insertAtCursor(insertion);
              }
            });

            vitaminMenu.getItems().add(sizeMenu);
          });

          cadVitamins.getItems().add(vitaminMenu);
        })).start();
  }

  /**
   * Load gists into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param gists list of gists
   */
  private void loadGistsIntoMenus(Menu menu, PagedIterable<GHGist> gists) {
    gists.forEach(gist -> {
      MenuItem showWebGist = new MenuItem("Show Gist on Web");
      showWebGist.setOnAction(event ->
          loadPageIntoNewTab(gist.getDescription(), gist.getHtmlUrl()));

      MenuItem addFileToGist = new MenuItem("Add File");
      addFileToGist.setOnAction(event -> Platform.runLater(() -> {
        AddFileToGistDialog dialog = new AddFileToGistDialog();
        dialog.showAndWait().ifPresent(name -> {
          try {
            ScriptingEngine.pushCodeToGit(
                gist.getGitPushUrl(),
                ScriptingEngine.getFullBranch(gist.getGitPushUrl()),
                name,
                "//Your code here",
                "New file");
            reloadGitMenus();
            ScriptingEngine.getGithub().getMyself().listGists().asList()
                .stream()
                .filter(item -> item.equals(gist))
                .findFirst()
                .ifPresent(newGist -> openGistFileInEditor(newGist, newGist.getFile(name)));
          } catch (Exception e) {
            LoggerUtilities.getLogger().log(Level.SEVERE,
                "Could not add file to gist.\n" + Throwables.getStackTraceAsString(e));
          }
        });
      }));

      String gistMenuText = gist.getDescription();
      if (gistMenuText == null || gistMenuText.length() == 0) {
        Set<String> filenames = gist.getFiles().keySet();
        if (filenames.size() >= 1) {
          gistMenuText = filenames.iterator().next();
        } else {
          gistMenuText = "";
        }
      }

      //Cap length
      gistMenuText = gistMenuText.substring(0, Math.min(25, gistMenuText.length()));

      Menu gistMenu = new Menu(gistMenuText);
      gistMenu.getItems().addAll(showWebGist, addFileToGist);

      gist.getFiles().forEach((name, gistFile) -> {
        MenuItem gistFileItem = new MenuItem(name);
        gistFileItem.setOnAction(event -> openGistFileInEditor(gist, gistFile));
        gistMenu.getItems().add(gistFileItem);
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
  private void loadOrgsIntoMenus(Menu menu, GHPersonSet<GHOrganization> orgs) {
    orgs.forEach(org -> {
      try {
        String name = org.getName();
        if (name == null || name.length() == 0) {
          name = org.getLogin();
        }

        Menu orgMenu = new Menu(name);
        org.getRepositories().forEach((key, value) -> {
          MenuItem repoMenu = new MenuItem(key);
          repoMenu.setOnAction(event -> {
                loadPageIntoNewTab(
                    value.getDescription()
                        .substring(0, Math.min(15, value.getDescription().length())),
                    value.gitHttpTransportUrl());
                event.consume();
              }
          );
          orgMenu.getItems().add(repoMenu);
        });

        orgMenu.setOnAction(event -> {
          try {
            loadPageIntoNewTab(org.getName(), org.getHtmlUrl());
          } catch (IOException e) {
            LoggerUtilities.getLogger().log(Level.WARNING,
                "Could not get organization name when loading new tab.\n"
                    + Throwables.getStackTraceAsString(e));
          }
        });

        menu.getItems().add(orgMenu);
      } catch (IOException e) {
        LoggerUtilities.getLogger().log(Level.WARNING,
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
  private void loadReposIntoMenus(Menu menu, PagedIterable<GHRepository> repos) {
    repos.forEach(repo -> {
      MenuItem menuItem = new MenuItem(repo.getName());
      menuItem.setOnAction(event ->
          loadPageIntoNewTab(
              repo.getName().substring(0, Math.min(15, repo.getName().length())),
              repo.gitHttpTransportUrl()));
      menu.getItems().add(menuItem);
    });
  }

  /**
   * Save work and quit.
   */
  public void saveAndQuit() {
    //Save here
    quit();
  }

  /**
   * Quit the application.
   */
  private void quit() {
    root.getScene().getWindow().hide();
    Platform.exit();
  }

  //Simple stream to append input characters to a text area
  private static class TextAreaPrintStream extends OutputStream {

    private final TextArea textArea;

    public TextAreaPrintStream(TextArea textArea) {
      this.textArea = textArea;
    }

    @Override
    public void write(int character) {
      Platform.runLater(() -> textArea.appendText(String.valueOf((char) character)));
    }
  }

}
