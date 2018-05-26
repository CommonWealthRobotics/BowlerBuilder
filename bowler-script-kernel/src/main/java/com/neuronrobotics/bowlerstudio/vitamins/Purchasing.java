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
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.TransportException;

class Purchasing {

  private static String jsonRootDir = "json/";
  private static final Map<String, CSG> fileLastLoaded = new HashMap<>();
  private static final Map<String, HashMap<String, HashMap<String, PurchasingData>>> databaseSet =
      new HashMap<>();
  private static final String defaultgitRpoDatabase =
      "https://github.com/CommonWealthRobotics/Hardware-Purchasing.git";
  private static String gitRpoDatabase = defaultgitRpoDatabase;
  // Create the type, this tells GSON what datatypes to instantiate when parsing and saving the json
  private static Type TT_mapStringString =
      new TypeToken<HashMap<String, HashMap<String, PurchasingData>>>() {}.getType();
  // chreat the gson object, this is the parsing factory
  private static Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
  private static boolean checked;

  private static HashMap<String, PurchasingData> getConfiguration(final String type,
      final String id) {
    final HashMap<String, HashMap<String, PurchasingData>> database = getDatabase(type);
    database.computeIfAbsent(id, k -> new HashMap<>());

    return database.get(id);
  }

  public static void saveDatabase(final String type) throws Exception {

    // Save contents and publish them
    final String jsonString = makeJson(type);
    try {
      ScriptingEngine.pushCodeToGit(
          getGitRpoDatabase(), // git repo, change this if you fork this demo
          ScriptingEngine.getFullBranch(getGitRpoDatabase()), // branch or tag
          getRootFolder() + type + ".json", // local path to the file in git
          jsonString, // content of the file
          "Pushing changed Database"); // commit message

    } catch (final TransportException ex) {
      System.out.println("You need to fork " + getGitRpoDatabase() + " to have permission to save");
      System.out.println(
          "You do not have permission to push to this repo, change the GIT repo to your fork with setGitRpoDatabase(String gitRpoDatabase) ");
      throw ex;
    }
  }

  private static String makeJson(final String type) {
    return gson.toJson(getDatabase(type), TT_mapStringString);
  }

  public static void newVitamin(final String type, final String id) {
    final HashMap<String, HashMap<String, PurchasingData>> database = getDatabase(type);
    if (database.keySet().size() > 0) {
      String exampleKey = null;
      for (final String key : database.keySet()) {
        if (!key.contains("meta")) {
          exampleKey = key;
        }
      }
      if (exampleKey != null) {
        // this database has examples, load an example
        final HashMap<String, PurchasingData> exampleConfiguration = getConfiguration(type, exampleKey);
        final HashMap<String, PurchasingData> newConfig = getConfiguration(type, id);
        for (final String key : exampleConfiguration.keySet()) {
          newConfig.put(key, exampleConfiguration.get(key));
        }
      }
    }

    getConfiguration(type, id);
    // saveDatabase(type);

  }

  public static void setParameter(
      final String type, final String id, final String parameterName, final PurchasingData parameter) {

    final HashMap<String, PurchasingData> config = getConfiguration(type, id);
    config.put(parameterName, parameter);
    // saveDatabase(type);
  }

  private static HashMap<String, HashMap<String, PurchasingData>> getDatabase(final String type) {
    if (databaseSet.get(type) == null) {
      // we are using the default vitamins configuration
      // https://github.com/madhephaestus/Hardware-Dimensions.git

      // create some variables, including our database
      final String jsonString;
      InputStream inPut = null;

      // attempt to load the JSON file from the GIt Repo and pars the JSON string
      final File f;
      try {
        f =
            ScriptingEngine.fileFromGit(
                getGitRpoDatabase(), // git repo, change this if you fork this demo
                getRootFolder() + type + ".json" // File from within the Git repo
                );
        inPut = FileUtils.openInputStream(f);

        jsonString = IOUtils.toString(inPut);
        // perfoem the GSON parse
        final HashMap<String, HashMap<String, PurchasingData>> database =
            gson.fromJson(jsonString, TT_mapStringString);
        if (database == null) {
          throw new RuntimeException("create a new one");
        }
        databaseSet.put(type, database);

      } catch (final Exception e) {
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
              getGitRpoDatabase(), // git repo, change this if you fork this demo
              getRootFolder() + "capScrew.json");
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
    final HashMap<String, HashMap<String, PurchasingData>> database = getDatabase(type);
    final Set<String> keys = database.keySet();
    for (final String s : keys) {
      if (!s.contains("meta")) {
        types.add(s);
      }
    }

    return types;
  }

  public static ArrayList<String> listVitaminVariants(final String type, final String size) {

    final ArrayList<String> types = new ArrayList<>();
    final HashMap<String, PurchasingData> database = getDatabase(type).get(size);
    final Set<String> keys = database.keySet();

    for (final String variant : keys) {
      final PurchasingData pd = database.get(variant);
      if (!variant.endsWith("variant-1")) // exclude the stub generated purhcasing data
      {
        try {
          URL u = new URL(pd.getAPIUrl());
          HttpURLConnection huc = (HttpURLConnection) u.openConnection();
          huc.setRequestMethod("GET"); // OR  huc.setRequestMethod ("HEAD");
          huc.connect();
          huc.getResponseCode();
          huc.disconnect();
          u = new URL(pd.getCartUrl());
          huc = (HttpURLConnection) u.openConnection();
          huc.setRequestMethod("GET"); // OR  huc.setRequestMethod ("HEAD");
          huc.connect();
          huc.getResponseCode();
          huc.disconnect();
          types.add(variant);
        } catch (final ConnectException ce) {
          // server or cart is not availible, reject vitamin
        } catch (final Exception ex) {
          ex.printStackTrace();
        }
      }
    }

    return types;
  }

  public static PurchasingData get(final String type, final String size, final String variant) {
    try {
      return getDatabase(type).get(size).get(variant);
    } catch (final NullPointerException ex) {
      throw new RuntimeException(
          "Vitamin " + type + " " + size + " " + variant + " does not exist");
    }
  }

  private static String getGitRpoDatabase() {
    return gitRpoDatabase;
  }

  public static void setGitRpoDatabase(final String gitRpoDatabase) {
    Purchasing.gitRpoDatabase = gitRpoDatabase;
  }

  private static String getJsonRootDir() {
    return jsonRootDir;
  }

  public static void setJsonRootDir(final String jsonRootDir) {
    Purchasing.jsonRootDir = jsonRootDir;
  }
}
