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

  public static Object getObject(String paramsKey, String objectKey, Object defaultValue) {
    if (getParamMap(paramsKey).get(objectKey) == null) {
      System.err.println("Cant find: " + paramsKey + ":" + objectKey);
      setObject(paramsKey, objectKey, defaultValue);
    }
    return getParamMap(paramsKey).get(objectKey);
  }

  public static HashMap<String, Object> getParamMap(String paramsKey) {
    if (getDatabase().get(paramsKey) == null) {
      getDatabase().put(paramsKey, new HashMap<>());
    }
    return getDatabase().get(paramsKey);
  }

  public static Object setObject(String paramsKey, String objectKey, Object value) {
    return getParamMap(paramsKey).put(objectKey, value);
  }

  public static Object removeObject(String paramsKey, String objectKey) {
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
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  public static HashMap<String, HashMap<String, Object>> getDatabase() {
    if (database != null) {
      return database;
    }
    try {
      database =
          (HashMap<String, HashMap<String, Object>>)
              ScriptingEngine.inlineFileScriptRun(loadFile(), null);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (database == null) {
      database = new HashMap<>();
    }
    return database;
  }

  public static File loadFile() throws Exception {
    return ScriptingEngine.fileFromGit(
        getGitSource(), // git repo, change
        getDbFile());
  }

  public static String getGitSource() throws Exception {
    if (!checked) {
      checked = true;
      if (ScriptingEngine.hasNetwork() && ScriptingEngine.isLoginSuccess()) {

        ScriptingEngine.setAutoupdate(true);
        org.kohsuke.github.GitHub github = ScriptingEngine.getGithub();
        GHMyself self = github.getMyself();
        Map<String, GHRepository> myPublic = self.getAllRepositories();
        for (Map.Entry<String, GHRepository> entry : myPublic.entrySet()) {
          if (entry.getKey().contentEquals(repo)
              && entry.getValue().getOwnerName().equals(self.getName())) {
            GHRepository ghrepo = entry.getValue();
            setRepo(ghrepo);
          }
        }
        if (gitSource == null) {
          GHRepository defaultRep = github.getRepository("CommonWealthRobotics/" + repo);
          GHRepository forkedRep = defaultRep.fork();
          setRepo(forkedRep);
        }
      } else {
        ConfigurationDatabase.setGitSource(
            HTTPS_GITHUB_COM_NEURON_ROBOTICS_BOWLER_STUDIO_CONFIGURATION_GIT);
      }
    }
    return gitSource;
  }

  private static void setRepo(GHRepository forkedRep) {
    String myAssets = forkedRep.getGitTransportUrl().replaceAll("git://", "https://");
    // System.out.println("Using my version of configuration database: " + myAssets);
    setGitSource(myAssets);
  }

  public static void setGitSource(String myAssets) {
    database = null;
    gitSource = myAssets;
    getDatabase();
  }

  public static String getDbFile() {
    return dbFile;
  }

  public static void setDbFile(String dbFile) {
    ConfigurationDatabase.dbFile = dbFile;
    setGitSource(gitSource);
  }
}
