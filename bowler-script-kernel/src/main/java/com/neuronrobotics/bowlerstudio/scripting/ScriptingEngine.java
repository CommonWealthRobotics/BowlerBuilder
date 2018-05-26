/*
 * Copyright 2015 Kevin Harrington
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.neuronrobotics.bowlerstudio.scripting;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;
import eu.mihosoft.vrl.v3d.parametrics.CSGDatabase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import javafx.scene.web.WebEngine;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHub;

public class ScriptingEngine {

  private static final int TIME_TO_WAIT_BETWEEN_GIT_PULL = 100000;

  private static final Map<String, Long> fileLastLoaded = new HashMap<>();

  private static boolean hasnetwork = false;
  private static boolean autoupdate = false;

  private static final String[] imports =
      new String[] {
        "java.nio.file",
        "java.util",
        "java.awt.image",
        "javafx.scene.text",
        "javafx.scene",
        "javafx.scene.control",
        "eu.mihosoft.vrl.v3d",
        "eu.mihosoft.vrl.v3d.svg",
        "eu.mihosoft.vrl.v3d.samples",
        "eu.mihosoft.vrl.v3d.parametrics",
        "com.neuronrobotics.imageprovider",
        "com.neuronrobotics.sdk.addons.kinematics.xml",
        "com.neuronrobotics.sdk.addons.kinematics",
        "com.neuronrobotics.sdk.dyio.peripherals",
        "com.neuronrobotics.sdk.dyio",
        "com.neuronrobotics.sdk.common",
        "com.neuronrobotics.sdk.ui",
        "com.neuronrobotics.sdk.util",
        "com.neuronrobotics.sdk.serial",
        "com.neuronrobotics.sdk.addons.kinematics",
        "com.neuronrobotics.sdk.addons.kinematics.math",
        "com.neuronrobotics.sdk.addons.kinematics.gui",
        "com.neuronrobotics.sdk.config",
        "com.neuronrobotics.bowlerkernel",
        "com.neuronrobotics.bowlerstudio",
        "com.neuronrobotics.bowlerstudio.scripting",
        "com.neuronrobotics.bowlerstudio.tabs",
        "com.neuronrobotics.bowlerstudio.physics",
        "com.neuronrobotics.bowlerstudio.physics",
        "com.neuronrobotics.bowlerstudio.vitamins",
        "com.neuronrobotics.bowlerstudio.creature",
        "com.neuronrobotics.bowlerstudio.threed"
      };

  private static GitHub github;
  private static HashMap<String, File> filesRun = new HashMap<>();

  private static File creds = null;

  private static File workspace;
  private static File lastFile;
  private static String loginID = null;
  private static String pw = null;
  private static CredentialsProvider cp;
  private static ArrayList<IGithubLoginListener> loginListeners =
      new ArrayList<>();

  private static HashMap<String, IScriptingLanguage> langauges = new HashMap<>();

  private static IGitHubLoginManager loginManager =
      username -> {
        new RuntimeException("Login required").printStackTrace();

        if (username != null) {
          if (username.equals("")) {
            username = null;
          }
        }
        final String[] creds = new String[] {"", ""};
        System.out.println("#Github Login Prompt#");
        System.out.println("For anynomous mode hit enter twice");
        System.out.print("Github Username: " + username != null ? "(" + username + ")" : "");
        // create a scanner so we can read the command-line input
        final BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));

        do {
          try {
            creds[0] = buf.readLine();
          } catch (final IOException e) {
            return null;
          }
          if (creds[0].equals("") && (username == null)) {
            System.out.println("No username, using anynomous login");
            return null;
          } else {
            creds[0] = username;
          }
        } while (creds[0] == null);

        System.out.print("Github Password: ");
        try {
          creds[1] = buf.readLine();
          if (creds[1].equals("")) {
            System.out.println("No password, using anynomous login");
          }
        } catch (final IOException e) {
          return null;
        }
        return creds;
      };
  private static boolean loginSuccess = false;

  static {
    try {
      final URL url = new URL("http://github.com");
      final URLConnection conn = url.openConnection();
      conn.connect();
      conn.getInputStream();
      hasnetwork = true;
    } catch (final Exception e) {
      // we assuming we have no access to the server and run off of the
      // chached gists.
      hasnetwork = false;
    }
    workspace = new File(System.getProperty("user.home") + "/bowler-workspace/");
    if (!workspace.exists()) {
      workspace.mkdir();
    }

    try {
      loadLoginData();
      // runLogin();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    addScriptingLanguage(new ClojureHelper());
    addScriptingLanguage(new GroovyHelper());
    addScriptingLanguage(new JythonHelper());
    addScriptingLanguage(new RobotHelper());
    addScriptingLanguage(new JsonRunner());
    addScriptingLanguage(new ArduinoLoader());
  }

  /**
   * This interface is for adding additional language support.
   *
   * @param code file content of the code to be executed
   * @param args the incoming arguments as a list of objects
   * @return the objects returned form the code that ran
   */
  private static Object inlineScriptRun(final File code, final ArrayList<Object> args,
      final String shellTypeStorage)
      throws Exception {

    filesRun.putIfAbsent(code.getName(), code);

    if (langauges.get(shellTypeStorage) != null) {
      return langauges.get(shellTypeStorage).inlineScriptRun(code, args);
    }
    return null;
  }

  /**
   * This interface is for adding additional language support.
   *
   * @param line the text content of the code to be executed
   * @param args the incoming arguments as a list of objects
   * @return the objects returned form the code that ran
   */
  public static Object inlineScriptStringRun(
      final String line, final ArrayList<Object> args, final String shellTypeStorage) throws Exception {

    if (langauges.get(shellTypeStorage) != null) {
      return langauges.get(shellTypeStorage).inlineScriptRun(line, args);
    }
    return null;
  }

  private static void loadLoginData() throws IOException {
    if (loginID == null && getCreds().exists() && hasnetwork) {
      try {
        String line;

        final InputStream fis = new FileInputStream(getCreds().getAbsolutePath());
        final InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        @SuppressWarnings("resource") final BufferedReader br = new BufferedReader(isr);

        while ((line = br.readLine()) != null) {
          if (line.startsWith("login") || line.startsWith("username")) {
            loginID = line.split("=")[1];
          }
          if (line.startsWith("password")) {
            pw = line.split("=")[1];
          }
        }
        if (pw != null && loginID != null) {
          // password loaded, we can now autoupdate
          ScriptingEngine.setAutoupdate(true);
        }
        if (cp == null) {
          cp = new UsernamePasswordCredentialsProvider(loginID, pw);
        }
      } catch (final Exception e) {
        logout();
      }
    }
  }

  public static void addScriptingLanguage(final IScriptingLanguage lang) {
    langauges.put(lang.getShellType(), lang);
  }

  public static void addIGithubLoginListener(final IGithubLoginListener l) {
    if (!loginListeners.contains(l)) {
      loginListeners.add(l);
    }
  }

  public static void removeIGithubLoginListener(final IGithubLoginListener l) {
    loginListeners.remove(l);
  }

  public static File getWorkspace() {
    return workspace;
  }

  private static String getShellType(final String name) {
    for (final IScriptingLanguage l : langauges.values()) {
      if (l.isSupportedFileExtenetion(name)) {
        return l.getShellType();
      }
    }

    return "Groovy";
  }

  public static String getLoginID() {

    return loginID;
  }

  private static void login() throws IOException {
    if (!hasnetwork) {
      return;
    }
    loginID = null;

    gitHubLogin();
  }

  public static void logout() throws IOException {
    if (getCreds() != null) {

      if (getCreds().exists()) {
        Files.delete(getCreds().toPath());
      }
    }

    setGithub(null);
    for (final IGithubLoginListener l : loginListeners) {
      l.onLogout(loginID);
    }
    loginID = null;
  }

  public static GitHub setupAnyonmous() throws IOException {
    System.err.println("Using anynomous login, autoupdate disabled");
    ScriptingEngine.setAutoupdate(false);
    logout();

    setGithub(GitHub.connectAnonymously());

    return getGithub();
  }

  public static String urlToGist(String in) {

    if (in.endsWith(".git")) {
      in = in.substring(0, in.lastIndexOf('.'));
    }
    final String domain = in.split("//")[1];
    final String[] tokens = domain.split("/");
    if (tokens[0].toLowerCase().contains("gist.github.com") && tokens.length >= 2) {
      try {
        final String id = tokens[2].split("#")[0];
        Log.debug("Gist URL Detected " + id);
        return id;
      } catch (final ArrayIndexOutOfBoundsException e) {
        try {
          final String id = tokens[1].split("#")[0];
          Log.debug("Gist URL Detected " + id);
          return id;
        } catch (final ArrayIndexOutOfBoundsException ex) {
          Log.error("Parsing " + in + " failed to find gist");
          return "d4312a0787456ec27a2a";
        }
      }
    }

    return null;
  }

  private static List<String> returnFirstGist(final String html) {
    final ArrayList<String> ret = new ArrayList<>();
    final Document doc = Jsoup.parse(html);
    final Elements links = doc.select("script");
    for (final Element e : links) {
      final Attributes n = e.attributes();
      final String jSSource = n.get("src");
      if (jSSource.contains("https://gist.github.com/")) {
        final String js = jSSource.split(".js")[0];
        final String[] id = js.split("/");
        ret.add(id[id.length - 1]);
      }
    }
    return ret;
  }

  public static List<String> getCurrentGist(final String addr, final WebEngine engine) {
    final String gist = urlToGist(addr);

    if (gist == null) {
      try {
        Log.debug("Non Gist URL Detected");
        final String html;
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer t = tf.newTransformer();
        final StringWriter sw = new StringWriter();
        t.transform(new DOMSource(engine.getDocument()), new StreamResult(sw));
        html = sw.getBuffer().toString();
        return returnFirstGist(html);
      } catch (final TransformerException e) {
        e.printStackTrace();
      }
    }
    final ArrayList<String> ret = new ArrayList<>();
    ret.add(gist);
    return ret;
  }

  private static GitHub gitHubLogin() throws IOException {
    final String[] creds = loginManager.prompt(loginID);

    if (creds == null) {
      return setupAnyonmous();
    } else {
      if (creds[0].contains("@")) {
        System.err.print("###ERROR Enter the Username not the Email Address### ");
        return gitHubLogin();
      }
      if (creds[0].equals("") || creds[1].equals("")) {
        System.err.print("###No Username or password### ");
        return setupAnyonmous();
      }
    }

    loginID = creds[0];
    pw = creds[1];

    String content = "login=" + loginID + "\n";
    content += "password=" + pw + "\n";
    final PrintWriter out;
    try {
      out = new PrintWriter(getCreds().getAbsoluteFile());
      out.println(content);
      out.flush();
      out.close();
      runLogin();

    } catch (final Exception e) {
      e.printStackTrace();
      System.out.println("Login failed");
      setGithub(null);
    }
    if (getGithub() == null) {
      ThreadUtil.wait(200);
      return gitHubLogin();
    } else {
      return getGithub();
    }
  }

  public static void runLogin() throws IOException {
    setGithub(GitHub.connect());

    if (getGithub().isCredentialValid()) {
      cp = new UsernamePasswordCredentialsProvider(loginID, pw);
      for (final IGithubLoginListener l : loginListeners) {
        l.onLogin(loginID);
      }
      System.out.println("Success Login as " + loginID + "");
      setLoginSuccess(true);
    } else {
      System.err.println("Bad login credentials for " + loginID);
      setGithub(null);
      pw = null;
    }
  }

  /** The GistID we are waiting to see */
  public static void waitForLogin()
      throws IOException {
    try {
      final URL url = new URL("http://github.com");
      final URLConnection conn = url.openConnection();
      conn.connect();
      conn.getInputStream();
      hasnetwork = true;
    } catch (final Exception e) {
      // we assuming we have no access to the server and run off of the
      // chached gists.
      hasnetwork = false;
    }
    if (!hasnetwork) {
      return;
    }
    if (getGithub() == null) {

      if (getCreds().exists()) {
        try {
          setGithub(GitHub.connect());
        } catch (final IOException ex) {
          logout();
        }
      } else {
        getCreds().createNewFile();
      }

      if (getGithub() == null) {

        login();
      }
    }

    try {
      if (getGithub().getRateLimit().remaining < 2) {
        System.err.println("##Github Is Rate Limiting You## Disabling autoupdate");
        setAutoupdate(false);
      }
    } catch (final IOException e) {
      logout();
    }

    loadLoginData();
  }

  private static void deleteRepo(final String remoteURI) {

    final File gitRepoFile = uriToFile(remoteURI);
    deleteFolder(gitRepoFile.getParentFile());
  }

  public static void deleteCache() {
    deleteFolder(new File(getWorkspace().getAbsolutePath() + "/gitcache/"));
  }

  private static void deleteFolder(final File folder) {
    final File[] files = folder.listFiles();
    if (files != null) { // some JVMs return null for empty dirs
      for (final File f : files) {
        if (f.isDirectory()) {
          deleteFolder(f);
        } else {
          f.delete();
          // System.out.println("Deleting " + f.getAbsolutePath());
        }
      }
    }
    folder.delete();
  }

  private static void loadFilesToList(final ArrayList<String> f, final File directory, final String extnetion) {
    for (final File fileEntry : directory.listFiles()) {

      if (fileEntry.getName().endsWith(".git") || fileEntry.getName().startsWith(".git")) {
        continue; // ignore git files
      }
      if (extnetion != null) {
        if (extnetion.length() > 0) {
          if (!fileEntry.getName().endsWith(extnetion)) {
            continue; // skip this file as it fails the filter
          }
        }
      }
      // from the user
      if (fileEntry.isDirectory()) {
        loadFilesToList(f, fileEntry, extnetion);
      } else {

        for (final IScriptingLanguage l : langauges.values()) {
          if (l.isSupportedFileExtenetion(fileEntry.getName())) {
            f.add(findLocalPath(fileEntry));
            break;
          }
        }
      }
    }
  }

  public static ArrayList<String> filesInGit(final String remote, final String branch, final String extnetion)
      throws Exception {
    final ArrayList<String> f = new ArrayList<>();

    waitForLogin();
    final File gistDir = cloneRepo(remote, branch);
    loadFilesToList(f, gistDir, extnetion);

    return f;
  }

  public static ArrayList<String> filesInGit(final String remote) throws Exception {
    return filesInGit(remote, ScriptingEngine.getFullBranch(remote), null);
  }

  public static String getUserIdOfGist(final String id) throws Exception {

    waitForLogin();
    Log.debug("Loading Gist: " + id);
    final GHGist gist;

    gist = getGithub().getGist(id);
    return gist.getOwner().getLogin();
  }

  public static File createFile(final String git, final String fileName, final String commitMessage)
      throws Exception {
    pushCodeToGit(git, ScriptingEngine.getFullBranch(git), fileName, null, commitMessage);
    return fileFromGit(git, fileName);
  }

  public static void pushCodeToGit(
      final String id, final String branch, final String FileName, final String content, final String commitMessage)
      throws Exception {
    if (loginID == null) {
      login();
    }
    if (loginID == null) {
      return; // No login info means there is no way to publish
    }
    final File gistDir = cloneRepo(id, branch);
    final File desired = new File(gistDir.getAbsoluteFile() + "/" + FileName);

    boolean flagNewFile = false;
    if (!desired.exists()) {
      desired.createNewFile();
      flagNewFile = true;
    }
    pushCodeToGit(id, branch, FileName, content, commitMessage, flagNewFile);
  }

  private static void commit(
      final String id,
      final String branch,
      final String FileName,
      final String content,
      final String commitMessage,
      final boolean flagNewFile)
      throws Exception {

    if (loginID == null) {
      login();
    }
    if (loginID == null) {
      return; // No login info means there is no way to publish
    }
    final File gistDir = cloneRepo(id, branch);
    final File desired = new File(gistDir.getAbsoluteFile() + "/" + FileName);

    final String localPath = gistDir.getAbsolutePath();
    final File gitRepoFile = new File(localPath + "/.git");

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    final Git git = new Git(localRepo);
    try { // latest version
      if (flagNewFile) {
        git.add().addFilepattern(FileName).call();
      }
      if (content != null) {
        OutputStream out = null;
        try {
          out = FileUtils.openOutputStream(desired, false);
          IOUtils.write(content, out);
          out.close(); // don't swallow close Exception if copy
          // completes
          // normally
        } finally {
          IOUtils.closeQuietly(out);
        }
      }

      git.commit().setAll(true).setMessage(commitMessage).call();
    } catch (final Exception ex) {
      git.close();

      throw ex;
    }
    git.close();
    try {
      if (!desired.getName().contentEquals("csgDatabase.json")) {
        final String[] gitID = ScriptingEngine.findGitTagFromFile(desired);
        final String remoteURI = gitID[0];
        final ArrayList<String> f = ScriptingEngine.filesInGit(remoteURI);
        for (final String s : f) {
          if (s.contentEquals("csgDatabase.json")) {

            final File dbFile = ScriptingEngine.fileFromGit(gitID[0], s);
            if (!CSGDatabase.getDbFile().equals(dbFile)) {
              CSGDatabase.setDbFile(dbFile);
            }
            CSGDatabase.saveDatabase();
            @SuppressWarnings("resource") final String c = new Scanner(dbFile).useDelimiter("\\Z").next();
            ScriptingEngine.commit(remoteURI, branch, s, c, "saving CSG database", false);
          }
        }
      }
    } catch (final Exception e) {
      // ignore CSG database
      e.printStackTrace();
    }
  }

  private static void pushCodeToGit(
      final String id,
      final String branch,
      final String FileName,
      final String content,
      final String commitMessage,
      final boolean flagNewFile)
      throws Exception {
    commit(id, branch, FileName, content, commitMessage, flagNewFile);
    if (loginID == null) {
      login();
    }
    if (loginID == null) {
      return; // No login info means there is no way to publish
    }
    final File gistDir = cloneRepo(id, branch);
    final File desired = new File(gistDir.getAbsoluteFile() + "/" + FileName);

    if (!hasnetwork && content != null) {
      OutputStream out = null;
      try {
        out = FileUtils.openOutputStream(desired, false);
        IOUtils.write(content, out);
        out.close(); // don't swallow close Exception if copy completes
        // normally
      } finally {
        IOUtils.closeQuietly(out);
      }
      return;
    }

    waitForLogin();
    final String localPath = gistDir.getAbsolutePath();
    final File gitRepoFile = new File(localPath + "/.git");

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    final Git git = new Git(localRepo);
    try {
      git.pull().setCredentialsProvider(cp).call(); // updates to the
      // latest version
      if (flagNewFile) {
        git.add().addFilepattern(FileName).call();
      }
      if (content != null) {
        OutputStream out = null;
        try {
          out = FileUtils.openOutputStream(desired, false);
          IOUtils.write(content, out);
          out.close(); // don't swallow close Exception if copy
          // completes
          // normally
        } finally {
          IOUtils.closeQuietly(out);
        }
      }
      git.push().setCredentialsProvider(cp).call();
      System.out.println("PUSH OK! file: " + desired);
    } catch (final Exception ex) {
      final String[] gitID = ScriptingEngine.findGitTagFromFile(desired);
      final String remoteURI = gitID[0];
      deleteRepo(remoteURI);
      git.close();
      throw ex;
    }
    git.close();
  }

  public static String[] codeFromGit(final String id, final String FileName) throws Exception {

    final File targetFile = fileFromGit(id, FileName);
    if (targetFile.exists()) {
      // System.err.println("Loading file:
      // "+targetFile.getAbsoluteFile());
      // Target file is ready to go
      final String text =
          new String(
              Files.readAllBytes(Paths.get(targetFile.getAbsolutePath())), StandardCharsets.UTF_8);
      return new String[] {text, FileName, targetFile.getAbsolutePath()};
    }

    return null;
  }

  private static String[] codeFromGistID(final String id, final String FileName) throws Exception {
    final String giturl = "https://gist.github.com/" + id + ".git";

    final File targetFile = fileFromGit(giturl, FileName);
    if (targetFile.exists()) {
      System.err.println("Gist at GIT : " + giturl);
      // Target file is ready to go
      final String text =
          new String(
              Files.readAllBytes(Paths.get(targetFile.getAbsolutePath())), StandardCharsets.UTF_8);
      return new String[] {text, FileName, targetFile.getAbsolutePath()};
    }

    return null;
  }

  public static Object inlineFileScriptRun(final File f, final ArrayList<Object> args) throws Exception {

    return inlineScriptRun(f, args, getShellType(f.getName()));
  }

  public static Object inlineGistScriptRun(
      final String gistID, final String Filename, final ArrayList<Object> args)
      throws Exception {
    final String[] gistData = codeFromGistID(gistID, Filename);
    return inlineScriptRun(new File(gistData[2]), args, getShellType(gistData[1]));
  }

  public static Object gitScriptRun(final String gitURL, final String Filename, final ArrayList<Object> args)
      throws Exception {
    final String[] gistData = codeFromGit(gitURL, Filename);
    return inlineScriptRun(new File(gistData[2]), args, getShellType(gistData[1]));
  }

  public static File fileFromGit(final String remoteURI, final String fileInRepo)
      throws IOException {
    return fileFromGit(remoteURI, ScriptingEngine.getFullBranch(remoteURI), fileInRepo);
  }

  // git@github.com:CommonWealthRobotics/BowlerStudioVitamins.git
  // or
  // https://github.com/CommonWealthRobotics/BowlerStudioVitamins.git
  public static File fileFromGit(final String remoteURI, final String branch, final String fileInRepo)
      throws IOException {
    final File gitRepoFile = cloneRepo(remoteURI, branch);
    final String id = gitRepoFile.getAbsolutePath();
    if (fileLastLoaded.get(id) == null) {
      // forces the first time the files is accessed by the application
      // tou pull an update
      fileLastLoaded.put(id, System.currentTimeMillis() - TIME_TO_WAIT_BETWEEN_GIT_PULL * 2);
    }
    final long lastTime = fileLastLoaded.get(id);
    if ((System.currentTimeMillis() - lastTime) > TIME_TO_WAIT_BETWEEN_GIT_PULL
        || !gitRepoFile.exists()) // wait
    // 2
    // seconds
    // before
    // re-downloading
    // the
    // file
    {
      // System.out.println("Updating git repo, its been
      // "+(System.currentTimeMillis()-lastTime)+
      // " need to wait "+ TIME_TO_WAIT_BETWEEN_GIT_PULL);
      fileLastLoaded.put(id, System.currentTimeMillis());
      if (isAutoupdate()) {
        // System.out.println("Autoupdating " +id);
        try {
          if (cp == null) {
            cp = new UsernamePasswordCredentialsProvider(loginID, pw);
          }
          final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile() + "/.git");
          // https://gist.github.com/0e6454891a3b3f7c8f28.git
          final Git git = new Git(localRepo);
          try {
            final PullResult ret = git.pull().setCredentialsProvider(cp).call(); // updates
            // to
            // the
            // latest
            // version
            // System.out.println("Pull completed "+ret);
            //
            // git.push().setCredentialsProvider(cp).call();
            git.close();
          } catch (final Exception ex) {
            try {
              // Files.delete(gitRepoFile.toPath());
              ex.printStackTrace();
              System.err.println("Error in gist, hosing: " + gitRepoFile);
              deleteFolder(gitRepoFile);
            } catch (final Exception x) {
              x.printStackTrace();
            }
          }
          git.close();
        } catch (final NullPointerException ex) {
          setAutoupdate(false);
        }
      }
    }

    return new File(gitRepoFile.getAbsolutePath() + "/" + fileInRepo);
  }

  private static File uriToFile(final String remoteURI) {
    // new Exception().printStackTrace();
    final String[] colinSplit = remoteURI.split(":");

    final String gitSplit = colinSplit[1].substring(0, colinSplit[1].lastIndexOf('.'));

    return new File(getWorkspace().getAbsolutePath() + "/gitcache/" + gitSplit + "/.git");
  }

  public static String getBranch(final String remoteURI) throws IOException {

    File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists()) {
      gitRepoFile = cloneRepo(remoteURI, null);
    }

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    final String branch = localRepo.getBranch();
    localRepo.close();

    return branch;
  }

  public static String getFullBranch(final String remoteURI) throws IOException {

    File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists()) {
      gitRepoFile = cloneRepo(remoteURI, null);
    }

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    final String branch = localRepo.getFullBranch();
    localRepo.close();

    return branch;
  }

  public static void deleteBranch(final String remoteURI, String toDelete) throws Exception {
    boolean found = false;
    for (final String s : listBranchNames(remoteURI)) {
      if (s.contains(toDelete)) {
        found = true;
      }
    }
    if (!found) {
      throw new RuntimeException(toDelete + " can not be deleted because it does not exist");
    }

    File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists()) {
      gitRepoFile = cloneRepo(remoteURI, null);
    }

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    // CreateBranchCommand bcc = null;
    // CheckoutCommand checkout;
    // String source = getFullBranch(remoteURI);

    final Git git;

    git = new Git(localRepo);
    if (!toDelete.contains("heads")) {
      toDelete = "heads/" + toDelete;
    }
    if (!toDelete.contains("refs")) {
      toDelete = "refs/" + toDelete;
    }
    Exception ex = null;
    try {
      // delete branch 'branchToDelete' locally
      git.branchDelete().setBranchNames(toDelete).call();

      // delete branch 'branchToDelete' on remote 'origin'
      final RefSpec refSpec = new RefSpec().setSource(null).setDestination(toDelete);
      git.push().setRefSpecs(refSpec).setRemote("origin").setCredentialsProvider(cp).call();
    } catch (final Exception e) {
      ex = e;
    }
    git.close();
    if (ex != null) {
      throw ex;
    }
  }

  public static void newBranch(final String remoteURI, final String newBranch) throws Exception {
    for (final String s : listBranchNames(remoteURI)) {
      if (s.contains(newBranch)) {
        throw new RuntimeException(
            newBranch + " can not be created because " + s + " is to similar");
      }
    }

    File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists()) {
      gitRepoFile = cloneRepo(remoteURI, null);
    }

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    CreateBranchCommand bcc = null;
    final CheckoutCommand checkout;
    final String source = getFullBranch(remoteURI);

    final Git git;

    git = new Git(localRepo);

    bcc = git.branchCreate();
    checkout = git.checkout();
    bcc.setName(newBranch).setStartPoint(source).setForce(true).call();

    checkout.setName(newBranch);
    checkout.call();
    final PushCommand pushCommand = git.push();
    pushCommand
        .setRemote("origin")
        .setRefSpecs(new RefSpec(newBranch + ":" + newBranch))
        .setCredentialsProvider(cp)
        .call();

    git.close();
  }

  private static boolean hasAtLeastOneReference(final Git git) throws Exception {
    final Repository repo = git.getRepository();
    final Config storedConfig = repo.getConfig();
    final Set<String> uriList = repo.getConfig().getSubsections("remote");
    String remoteURI = null;
    for (final String remoteName : uriList) {
      if (remoteURI == null) {
        remoteURI = storedConfig.getString("remote", remoteName, "url");
      }
    }
    final long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < (startTime + 2000)) {
      for (final Ref ref : repo.getAllRefs().values()) {
        if (ref.getObjectId() != null) {
          final List<Ref> branchList = listBranches(remoteURI, git);
          if (branchList.size() > 0) {
            // System.out.println("Found "+branchList.size()+"
            // branches");
            return true;
          }
        }
      }
    }
    throw new RuntimeException("No references or branches found!");
  }

  private static List<Ref> listBranches(final String remoteURI) throws Exception {

    File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists()) {
      gitRepoFile = cloneRepo(remoteURI, null);
      return listBranches(remoteURI);
    }

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    // https://gist.github.com/0e6454891a3b3f7c8f28.git
    List<Ref> Ret = new ArrayList<>();
    final Git git = new Git(localRepo);
    Ret = listBranches(remoteURI, git);
    git.close();
    return Ret;
  }

  private static List<Ref> listBranches(final String remoteURI, final Git git) throws Exception {

    // https://gist.github.com/0e6454891a3b3f7c8f28.git
    // System.out.println("Listing references from: "+remoteURI);
    // System.out.println(" branch: "+getFullBranch(remoteURI));
    // System.out.println(" size : "+list.size());
    return git.branchList().setListMode(ListMode.ALL).call();
  }

  private static List<Ref> listLocalBranches(final String remoteURI) throws IOException {

    File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists()) {
      gitRepoFile = cloneRepo(remoteURI, null);
    }

    final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile());
    // https://gist.github.com/0e6454891a3b3f7c8f28.git
    final Git git = new Git(localRepo);
    try {
      final List<Ref> list = git.branchList().call();
      git.close();
      return list;
    } catch (final Exception ex) {

    }
    git.close();
    return new ArrayList<>();
  }

  public static List<String> listLocalBranchNames(final String remoteURI) throws Exception {
    final ArrayList<String> branchNames = new ArrayList<>();

    final List<Ref> list = listLocalBranches(remoteURI);
    for (final Ref ref : list) {
      // System.out.println("Branch: " + ref + " " + ref.getName() + " " +
      // ref.getObjectId().getName());
      branchNames.add(ref.getName());
    }
    return branchNames;
  }

  private static List<String> listBranchNames(final String remoteURI) throws Exception {
    final ArrayList<String> branchNames = new ArrayList<>();

    final List<Ref> list = listBranches(remoteURI);
    for (final Ref ref : list) {
      // System.out.println("Branch: " + ref + " " + ref.getName() + " " +
      // ref.getObjectId().getName());
      branchNames.add(ref.getName());
    }
    return branchNames;
  }

  public static void pull(final String remoteURI, final String branch) {
    cloneRepo(remoteURI, branch);
  }

  public static void checkoutCommit(final String remoteURI, final String branch, final String commitHash)
      throws IOException {
    final File gitRepoFile = ScriptingEngine.uriToFile(remoteURI);
    if (!gitRepoFile.exists() || !gitRepoFile.getAbsolutePath().endsWith(".git")) {
      System.err.println("Invailid git file!" + gitRepoFile.getAbsolutePath());
      throw new RuntimeException("Invailid git file!" + gitRepoFile.getAbsolutePath());
    }
    final Repository localRepo = new FileRepository(gitRepoFile);
    final Git git = new Git(localRepo);
    try {
      git.checkout().setName(commitHash).call();
      git.checkout().setCreateBranch(true).setName(branch).setStartPoint(commitHash).call();

    } catch (final Exception ex) {
      ex.printStackTrace();
    }

    git.close();
  }

  private static void checkout(final String remoteURI, final String branch) throws IOException {
    // cloneRepo(remoteURI, branch);
    final File gitRepoFile = uriToFile(remoteURI);
    if (!gitRepoFile.exists() || !gitRepoFile.getAbsolutePath().endsWith(".git")) {
      System.err.println("Invailid git file!" + gitRepoFile.getAbsolutePath());
      throw new RuntimeException("Invailid git file!" + gitRepoFile.getAbsolutePath());
    }

    final String currentBranch = getFullBranch(remoteURI);
    if (currentBranch != null) {
      // String currentBranch=getFullBranch(remoteURI);
      final Repository localRepo = new FileRepository(gitRepoFile);
      // if (!branch.contains("heads")) {
      // branch = "heads/" + branch;
      // }
      // if (!branch.contains("refs")) {
      // branch = "refs/" + branch;
      // }
      // System.out.println("Checking out "+branch+" :
      // "+gitRepoFile.getAbsolutePath() );
      final Git git = new Git(localRepo);
      // StoredConfig config = git.getRepository().getConfig();
      // config.setString("branch", "master", "merge", "refs/heads/master");
      if (!currentBranch.contains(branch)) {
        try {
          git.pull().setCredentialsProvider(cp).call();
          git.branchCreate()
              .setForce(true)
              .setName(branch)
              .setStartPoint("origin/" + branch)
              .call();
          git.checkout().setName(branch).call();
        } catch (final Exception ex) {
          ex.printStackTrace();
        }
      }
      git.close();
    }
  }

  // public static void checkout(String branch, File gitRepoFile) throws
  // Exception {
  // String currentBranch=getFullBranch(gitRepoFile);
  // Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile() +
  // "/.git");
  //
  // }

  /**
   * This function retrieves the local cached version of a given git repository. If it does not
   * exist, it clones it.
   *
   * @return The local directory containing the .git
   */
  private static File cloneRepo(final String remoteURI, String branch) {
    // new Exception().printStackTrace();
    final String[] colinSplit = remoteURI.split(":");

    final String gitSplit = colinSplit[1].substring(0, colinSplit[1].lastIndexOf('.'));

    final File gistDir = new File(getWorkspace().getAbsolutePath() + "/gitcache/" + gitSplit);
    if (!gistDir.exists()) {
      gistDir.mkdir();
    }
    final String localPath = gistDir.getAbsolutePath();
    final File gitRepoFile = new File(localPath + "/.git");
    final File dir = new File(localPath);

    if (!gitRepoFile.exists()) {

      System.out.println("Cloning files from: " + remoteURI);
      if (branch != null) {
        System.out.println("            branch: " + branch);
      }
      System.out.println("                to: " + localPath);

      for (int i = 0; i < 5; i++) {
        // Clone the repo
        try {
          if (branch == null) {
            final Git git =
                Git.cloneRepository()
                    .setURI(remoteURI)
                    .setDirectory(dir)
                    .setCredentialsProvider(cp)
                    .call();
            hasAtLeastOneReference(git);
            branch = getFullBranch(remoteURI);
            checkout(remoteURI, branch);
            hasAtLeastOneReference(git);
            git.close();

          } else {
            final Git git =
                Git.cloneRepository()
                    .setURI(remoteURI)
                    .setBranch(branch)
                    .setDirectory(dir)
                    .setCredentialsProvider(cp)
                    .call();
            hasAtLeastOneReference(git);
            checkout(remoteURI, branch);
            hasAtLeastOneReference(git);
            git.close();
          }

          break;
        } catch (final Exception e) {
          Log.error("Failed to clone " + remoteURI + " " + e);
          e.printStackTrace();
          deleteFolder(new File(localPath));
        }
        ThreadUtil.wait(200 * i);
      }
    }
    if (branch != null) {
      try {
        checkout(remoteURI, branch);
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return gistDir;
  }

  public static Git locateGit(final File f) throws IOException {
    File gitRepoFile = f;
    while (gitRepoFile != null) {
      gitRepoFile = gitRepoFile.getParentFile();
      if (new File(gitRepoFile.getAbsolutePath() + "/.git").exists()) {
        // System.err.println("Fount git repo for file: "+gitRepoFile);
        final Repository localRepo = new FileRepository(gitRepoFile.getAbsoluteFile() + "/.git");
        return new Git(localRepo);
      }
    }

    return null;
  }

  public static String getText(final URL website) throws Exception {

    final URLConnection connection = website.openConnection();
    final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

    final StringBuilder response = new StringBuilder();
    String inputLine;

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine + "\n");
    }

    in.close();

    return response.toString();
  }

  public static File getLastFile() {
    if (lastFile == null) {
      return getWorkspace();
    }
    return lastFile;
  }

  public static void setLastFile(final File lastFile) {
    ScriptingEngine.lastFile = lastFile;
  }

  private static File getCreds() {
    if (creds == null) {
      setCreds(new File(System.getProperty("user.home") + "/.github"));
    }
    return creds;
  }

  private static void setCreds(final File creds) {
    ScriptingEngine.creds = creds;
  }

  public static File getFileEngineRunByName(final String filename) {
    return filesRun.get(filename);
  }

  public static String[] getImports() {
    return imports;
  }

  public static IGitHubLoginManager getLoginManager() {
    return loginManager;
  }

  public static void setLoginManager(final IGitHubLoginManager loginManager) {
    ScriptingEngine.loginManager = loginManager;
  }

  private static boolean isAutoupdate() {
    return autoupdate;
  }

  public static boolean setAutoupdate(final boolean autoupdate) throws IOException {

    if (autoupdate && !ScriptingEngine.autoupdate) {
      ScriptingEngine.autoupdate = true; // prevents recoursion loop from
      // calling loadLoginData
      loadLoginData();
      if (pw == null || loginID == null) {
        login();
      }

      if (pw == null || loginID == null) {
        return false;
      }
    }
    ScriptingEngine.autoupdate = autoupdate;
    return ScriptingEngine.autoupdate;
  }

  private static File fileFromGistID(final String string, final String string2)
      throws GitAPIException, IOException {
    // TODO Auto-generated method stub
    return fileFromGit("https://gist.github.com/" + string + ".git", string2);
  }

  public static String findLocalPath(final File currentFile, final Git git) {
    final File dir = git.getRepository().getDirectory().getParentFile();

    return dir.toURI().relativize(currentFile.toURI()).getPath();
  }

  private static String findLocalPath(final File currentFile) {
    final Git git;
    try {
      git = locateGit(currentFile);
      return findLocalPath(currentFile, git);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return currentFile.getName();
  }

  public static String[] findGitTagFromFile(final File currentFile) throws IOException {

    final Git git = locateGit(currentFile);

    return new String[] {
      git.getRepository().getConfig().getString("remote", "origin", "url"),
      findLocalPath(currentFile, git)
    };
  }

  public static boolean checkOwner(final File currentFile) {
    try {
      waitForLogin();
      final Git git = locateGit(currentFile);
      git.pull().setCredentialsProvider(cp).call(); // updates to the
      // latest version
      git.push().setCredentialsProvider(cp).call();
      git.close();
      return true;
    } catch (final Exception e) {
      // just return false, the exception is it failing to push
    }

    return false;
  }

  public static GHGist fork(final String currentGist) throws Exception {

    if (getGithub() != null) {

      waitForLogin();
      final GHGist incoming = getGithub().getGist(currentGist);
      for (final IGithubLoginListener l : loginListeners) {
        l.onLogin(loginID);
      }
      return incoming.fork();
    }

    return null;
  }

  public static String[] forkGitFile(final String[] incoming) throws Exception {
    final GitHub github = ScriptingEngine.getGithub();

    String id = null;
    if (incoming[0].endsWith(".git")) {
      id = urlToGist(incoming[0]);
    } else {
      id = incoming[0];
      incoming[0] = "https://gist.github.com/" + id + ".git";
    }
    GHGist incomingGist = github.getGist(id);
    File incomingFile = ScriptingEngine.fileFromGistID(id, incoming[1]);
    if (!ScriptingEngine.checkOwner(incomingFile)) {
      incomingGist = incomingGist.fork();
      incoming[0] =
          "https://gist.github.com/"
              + ScriptingEngine.urlToGist(incomingGist.getHtmlUrl())
              + ".git";
      // sync the new file to the disk
      incomingFile = ScriptingEngine.fileFromGistID(id, incoming[1]);
    }
    for (final IGithubLoginListener l : loginListeners) {
      l.onLogin(loginID);
    }

    return incoming;
  }

  public static GitHub getGithub() {
    return github;
  }

  private static void setGithub(final GitHub github) {
    ScriptingEngine.github = github;
    if (github == null) {
      setLoginSuccess(false);
    }
  }

  public static List<String> getAllLangauges() {
    final ArrayList<String> langs = new ArrayList<>();
    for (final String L : getLangaugesMap().keySet()) {
      langs.add(L);
    }
    return langs;
  }

  // private static ArrayList<IScriptingLanguage> getLangauges() {
  // ArrayList<IScriptingLanguage> langs = new ArrayList<>();
  // for (String L : getLangaugesMap().keySet()) {
  // langs.add(getLangaugesMap().get(L));
  // }
  // return langs;
  // }

  private static HashMap<String, IScriptingLanguage> getLangaugesMap() {
    return langauges;
  }

  public static boolean hasNetwork() {

    return hasnetwork;
  }

  public static boolean isLoginSuccess() {
    return loginSuccess;
  }

  private static void setLoginSuccess(final boolean loginSuccess) {
    ScriptingEngine.loginSuccess = loginSuccess;
  }

  public static String[] copyGitFile(final String sourceGit, final String targetGit, final String filename) {

    final String[] WalkingEngine;
    try {
      WalkingEngine = ScriptingEngine.codeFromGit(sourceGit, filename);
      try {
        if (null == ScriptingEngine.fileFromGit(targetGit, filename)) {

          ScriptingEngine.createFile(targetGit, filename, "copy file");
          while (true) {
            try {
              ScriptingEngine.fileFromGit(targetGit, filename);
              break;
            } catch (final Exception e) {

            }
            ThreadUtil.wait(500);
            // Log.warn(targetGit +"/"+filename+ " not built yet");
          }
        }
      } catch (final InvalidRemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final TransportException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final GitAPIException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      String[] newFileCode;
      try {
        newFileCode = ScriptingEngine.codeFromGit(targetGit, filename);
        if (newFileCode == null) {
          newFileCode = new String[] {""};
        }
        if (!WalkingEngine[0].contentEquals(newFileCode[0])) {
          System.out.println("Copy Content to " + targetGit + "/" + filename);
          ScriptingEngine.pushCodeToGit(
              targetGit,
              ScriptingEngine.getFullBranch(targetGit),
              filename,
              WalkingEngine[0],
              "copy file content");
        }
      } catch (final Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    } catch (final Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    return new String[] {targetGit, filename};
  }
}
