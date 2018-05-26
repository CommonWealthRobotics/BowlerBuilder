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
package com.neuronrobotics.bowlerstudio.vitamins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.common.Log;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.STL;
import eu.mihosoft.vrl.v3d.parametrics.LengthParameter;
import eu.mihosoft.vrl.v3d.parametrics.StringParameter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.TransportException;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class Vitamins {

  private static String jsonRootDir = "json/";
  private static final Map<String, CSG> fileLastLoaded = new HashMap<>();
  private static final Map<String, HashMap<String, HashMap<String, Object>>> databaseSet =
      new HashMap<>();
  private static final String defaultgitRpoDatabase =
      "https://github.com/madhephaestus/Hardware-Dimensions.git";
  private static String gitRpoDatabase = defaultgitRpoDatabase;
  // Create the type, this tells GSON what datatypes to instantiate when parsing and saving the json
  private static Type TT_mapStringString =
      new TypeToken<HashMap<String, HashMap<String, Object>>>() {}.getType();
  // chreat the gson object, this is the parsing factory
  private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
  private static boolean checked;

  public static CSG get(final File resource) {

    if (fileLastLoaded.get(resource.getAbsolutePath()) == null) {
      // forces the first time the files is accessed by the application tou pull an update
      try {
        fileLastLoaded.put(resource.getAbsolutePath(), STL.file(resource.toPath()));
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return fileLastLoaded.get(resource.getAbsolutePath()).clone();
  }

  public static CSG get(final String type, final String id, final String purchasingVariant) {
    final String key = type + id + purchasingVariant;
    if (fileLastLoaded.get(key) == null) {
      final PurchasingData purchasData = Purchasing.get(type, id, purchasingVariant);
      for (final String variable : purchasData.getVariantParameters().keySet()) {
        final double data = purchasData.getVariantParameters().get(variable);
        final LengthParameter parameter =
            new LengthParameter(variable, data, (ArrayList<Double>) Arrays.asList(data, data));
        parameter.setMM(data);
      }

      try {
        fileLastLoaded.put(key, get(type, id));
      } catch (final Exception e) {
        e.printStackTrace();

        gitRpoDatabase = defaultgitRpoDatabase;
        databaseSet.clear();
        fileLastLoaded.clear();
        return get(type, id);
      }
    }

    return fileLastLoaded.get(type + id);
  }

  private static CSG get(final String type, final String id) {
    return get(type, id, 0);
  }

  private static CSG get(final String type, final String id, final int depthGauge) {
    final String key = type + id;

    try {
      CSG newVitamin = null;
      final HashMap<String, Object> script = getMeta(type);
      final StringParameter size =
          new StringParameter(type + " Default", id, Vitamins.listVitaminSizes(type));
      size.setStrValue(id);
      final Object file = script.get("scriptGit");
      final Object repo = script.get("scriptFile");
      if (file != null && repo != null) {
        final ArrayList<Object> servoMeasurments = new ArrayList<>();
        servoMeasurments.add(id);
        newVitamin =
            (CSG)
                ScriptingEngine.gitScriptRun(
                    script.get("scriptGit").toString(), // git location of the library
                    script.get("scriptFile").toString(), // file to load
                    servoMeasurments);
        return newVitamin;
      } else {
        Log.error(key + " Failed to load from script");
        return null;
      }
    } catch (final Exception e) {
      e.printStackTrace();
      gitRpoDatabase = defaultgitRpoDatabase;
      databaseSet.clear();
      fileLastLoaded.clear();
      if (depthGauge < 2) {
        return get(type, id, depthGauge + 1);
      } else {
        return null;
      }
    }
  }

  public static HashMap<String, Object> getMeta(final String type) {
    return getConfiguration(type, "meta");
  }

  public static void setScript(final String type, final String git, final String file) {
    setParameter(type, "meta", "scriptGit", git);
    setParameter(type, "meta", "scriptFile", file);
  }

  public static HashMap<String, Object> getConfiguration(final String type, final String id) {
    final HashMap<String, HashMap<String, Object>> database = getDatabase(type);
    database.computeIfAbsent(id, k -> new HashMap<>());
    return database.get(id);
  }

  private static String makeJson(final String type) {
    return gson.toJson(getDatabase(type), TT_mapStringString);
  }

  public static void saveDatabase(final String type) throws Exception {

    // Save contents and publish them
    final String jsonString = makeJson(type);
    try {
      ScriptingEngine.pushCodeToGit(
          getGitRepoDatabase(), // git repo, change this if you fork this demo
          ScriptingEngine.getFullBranch(getGitRepoDatabase()), // branch or tag
          getRootFolder() + type + ".json", // local path to the file in git
          jsonString, // content of the file
          "Pushing changed Database"); // commit message

    } catch (final TransportException ex) {
      System.out.println(
          "You need to fork " + defaultgitRpoDatabase + " to have permission to save");
      System.out.println(
          "You do not have permission to push to this repo, change the GIT repo to your fork with setGitRpoDatabase(String gitRpoDatabase) ");
      throw ex;
    }
  }

  public static void newVitamin(final String type, final String id) {
    final HashMap<String, HashMap<String, Object>> database = getDatabase(type);
    if (database.keySet().size() > 0) {
      String exampleKey = null;
      for (final String key : database.keySet()) {
        if (!key.contains("meta")) {
          exampleKey = key;
        }
      }
      if (exampleKey != null) {
        // this database has examples, load an example
        final HashMap<String, Object> exampleConfiguration = getConfiguration(type, exampleKey);
        final HashMap<String, Object> newConfig = getConfiguration(type, id);
        for (final String key : exampleConfiguration.keySet()) {
          newConfig.put(key, exampleConfiguration.get(key));
        }
      }
    }

    getConfiguration(type, id);
    // saveDatabase(type);

  }

  public static void setParameter(final String type, final String id, final String parameterName, final Object parameter) {

    final HashMap<String, Object> config = getConfiguration(type, id);
    try {
      config.put(parameterName, Double.parseDouble(parameter.toString()));
    } catch (final NumberFormatException ex) {
      config.put(parameterName, parameter);
    }

    // saveDatabase(type);
  }

  private static HashMap<String, HashMap<String, Object>> getDatabase(final String type) {
    if (databaseSet.get(type) == null) {
      // we are using the default vitamins configuration
      // https://github.com/madhephaestus/Hardware-Dimensions.git

      // create some variables, including our database
      final String jsonString;
      final InputStream inPut;

      // attempt to load the JSON file from the GIt Repo and pars the JSON string
      final File f;
      try {
        f =
            ScriptingEngine.fileFromGit(
                getGitRepoDatabase(), // git repo, change this if you fork this demo
                getRootFolder() + type + ".json" // File from within the Git repo
                );
        inPut = FileUtils.openInputStream(f);

        jsonString = IOUtils.toString(inPut);
        // perform the GSON parse
        final HashMap<String, HashMap<String, Object>> database =
            gson.fromJson(jsonString, TT_mapStringString);
        if (database == null) {
          throw new RuntimeException("create a new one");
        }
        databaseSet.put(type, database);

        for (final String key : databaseSet.get(type).keySet()) {
          final HashMap<String, Object> conf = database.get(key);
          for (final String confKey : conf.keySet()) {
            try {
              final double num = Double.parseDouble(conf.get(confKey).toString());
              conf.put(confKey, num);
            } catch (final NumberFormatException ex) {
              // leave as a string
              conf.put(confKey, conf.get(confKey).toString());
            }
          }
        }

      } catch (final Exception e) {
        e.printStackTrace();
        databaseSet.put(type, new HashMap<>());
      }
    }
    return databaseSet.get(type);
  }

  private static String getRootFolder() {
    return getJsonRootDir();
  }

  public static ArrayList<String> listVitaminTypes() {

    final ArrayList<String> types = new ArrayList<>();
    final File folder;
    try {
      folder =
          ScriptingEngine.fileFromGit(
              getGitRepoDatabase(), // git repo, change this if you fork this demo
              getRootFolder() + "hobbyServo.json");
      final File[] listOfFiles = folder.getParentFile().listFiles();

      for (final File f : listOfFiles) {
        if (!f.isDirectory() && f.getName().endsWith(".json")) {
          types.add(f.getName().substring(0, f.getName().indexOf(".json")));
        }
      }

    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return types;
  }

  public static ArrayList<String> listVitaminSizes(final String type) {

    final ArrayList<String> types = new ArrayList<>();
    final HashMap<String, HashMap<String, Object>> database = getDatabase(type);
    final Set<String> keys = database.keySet();
    for (final String s : keys) {
      if (!s.contains("meta")) {
        types.add(s);
      }
    }

    return types;
  }

  private static String getGitRepoDatabase() {
    if (!checked) {
      checked = true;
      try {
        if (ScriptingEngine.getLoginID() != null) {
          ScriptingEngine.setAutoupdate(true);
          final GitHub github = ScriptingEngine.getGithub();
          final GHMyself self = github.getMyself();
          final Map<String, GHRepository> myPublic = self.getAllRepositories();
          for (final String myRepo : myPublic.keySet()) {
            final GHRepository ghrepo = myPublic.get(myRepo);
            if (myRepo.contentEquals("Hardware-Dimensions")
                && ghrepo.getOwnerName().contentEquals(self.getLogin())) {

              final String myAssets = ghrepo.getGitTransportUrl().replaceAll("git://", "https://");
              // System.out.println("Using my version of Viamins: "+myAssets);
              setGitRepoDatabase(myAssets);
            }
          }
        }
      } catch (final Exception ex) {
      }
    }
    return gitRpoDatabase;
  }

  public static void setGitRepoDatabase(final String gitRpoDatabase) {
    Vitamins.gitRpoDatabase = gitRpoDatabase;
    databaseSet.clear();
    fileLastLoaded.clear();
  }

  private static String getJsonRootDir() {
    return jsonRootDir;
  }

  public static void setJsonRootDir(final String jsonRootDir) {
    Vitamins.jsonRootDir = jsonRootDir;
    setGitRepoDatabase(getGitRepoDatabase());
  }
}
