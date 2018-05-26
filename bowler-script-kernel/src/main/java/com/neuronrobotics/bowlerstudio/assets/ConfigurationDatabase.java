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
package com.neuronrobotics.bowlerstudio.assets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class ConfigurationDatabase {

  private static final String repo = "BowlerStudioConfiguration";
  private static final String HTTPS_GITHUB_COM_NEURON_ROBOTICS_BOWLER_STUDIO_CONFIGURATION_GIT =
      "https://github.com/CommonWealthRobotics/" + repo + ".git";

  private static String gitSource = null; // madhephaestus
  private static String dbFile = "database.json";
  private static boolean checked;
  private static HashMap<String, HashMap<String, Object>> database = null;
  private static final Type TT_mapStringString =
      new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType();
  // chreat the gson object, this is the parsing factory
  private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

  public static Object getObject(final String paramsKey, final String objectKey, final Object defaultValue) {
    if (getParamMap(paramsKey).get(objectKey) == null) {
      System.err.println("Cant find: " + paramsKey + ":" + objectKey);
      setObject(paramsKey, objectKey, defaultValue);
    }
    return getParamMap(paramsKey).get(objectKey);
  }

  private static HashMap<String, Object> getParamMap(final String paramsKey) {
    getDatabase().computeIfAbsent(paramsKey, k -> new HashMap<>());
    return getDatabase().get(paramsKey);
  }

  private static Object setObject(final String paramsKey, final String objectKey,
      final Object value) {
    return getParamMap(paramsKey).put(objectKey, value);
  }

  public static Object removeObject(final String paramsKey, final String objectKey) {
    return getParamMap(paramsKey).remove(objectKey);
  }

  public static void save() {
    String writeOut = null;
    getDatabase();
    // synchronized(database){
    writeOut = gson.toJson(database, TT_mapStringString);
    // }
    try {
      ScriptingEngine.pushCodeToGit(
          getGitSource(),
          ScriptingEngine.getFullBranch(getGitSource()),
          getDbFile(),
          writeOut,
          "Saving database");
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private static HashMap<String, HashMap<String, Object>> getDatabase() {
    if (database != null) {
      return database;
    }
    try {
      database =
          (HashMap<String, HashMap<String, Object>>)
              ScriptingEngine.inlineFileScriptRun(loadFile(), null);

    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (database == null) {
      database = new HashMap<>();
    }
    return database;
  }

  private static File loadFile() throws Exception {
    return ScriptingEngine.fileFromGit(
        getGitSource(), // git repo, change
        getDbFile());
  }

  private static String getGitSource() throws Exception {
    if (!checked) {
      checked = true;
      if (ScriptingEngine.hasNetwork() && ScriptingEngine.isLoginSuccess()) {

        ScriptingEngine.setAutoupdate(true);
        final GitHub github = ScriptingEngine.getGithub();
        final GHMyself self = github.getMyself();
        final Map<String, GHRepository> myPublic = self.getAllRepositories();
        for (final Map.Entry<String, GHRepository> entry : myPublic.entrySet()) {
          if (entry.getKey().contentEquals(repo)
              && entry.getValue().getOwnerName().equals(self.getName())) {
            final GHRepository ghrepo = entry.getValue();
            setRepo(ghrepo);
          }
        }
        if (gitSource == null) {
          final GHRepository defaultRep = github.getRepository("CommonWealthRobotics/" + repo);
          final GHRepository forkedRep = defaultRep.fork();
          setRepo(forkedRep);
        }
      } else {
        ConfigurationDatabase.setGitSource(
            HTTPS_GITHUB_COM_NEURON_ROBOTICS_BOWLER_STUDIO_CONFIGURATION_GIT);
      }
    }
    return gitSource;
  }

  private static void setRepo(final GHRepository forkedRep) {
    final String myAssets = forkedRep.getGitTransportUrl().replaceAll("git://", "https://");
    // System.out.println("Using my version of configuration database: " + myAssets);
    setGitSource(myAssets);
  }

  private static void setGitSource(final String myAssets) {
    database = null;
    gitSource = myAssets;
    getDatabase();
  }

  private static String getDbFile() {
    return dbFile;
  }

  public static void setDbFile(final String dbFile) {
    ConfigurationDatabase.dbFile = dbFile;
    setGitSource(gitSource);
  }
}
