package com.neuronrobotics.bowlerbuilder;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.IOException;
import java.util.logging.Level;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

public final class GistUtilities {
  private GistUtilities() {
  }

  /**
   * Create and publish a new gist.
   *
   * @param filename Gist filename for first file
   * @param description Gist description
   * @param isPublic Public/private viewing
   * @return Gist push URL
   */
  public static String createNewGist(String filename, String description, boolean isPublic) {
    //Setup gist
    GitHub gitHub = ScriptingEngine.getGithub();
    GHGistBuilder builder = gitHub.createGist();
    builder.file(filename, "//Your code here");
    builder.description(description);
    builder.public_(isPublic);

    //Make gist
    return createGistFromBuilder(builder, filename);
  }

  /**
   * Add a new file to an existing Gist.
   *
   * @param filename New file filename
   * @param content Starting content of new file
   * @param gistID Gist ID to add file to
   * @return Gist push URL
   */
  public static String addFileToGist(String filename, String content, GHGist gistID) {
    GitHub gitHub = ScriptingEngine.getGithub();
    //Copy from old gist
    GHGistBuilder builder = gitHub.createGist();

    builder.description(gistID.getDescription());
    builder.public_(gistID.isPublic());

    gistID.getFiles().keySet().forEach(key ->
        builder.file(key, gistID.getFiles().get(key).getContent()));

    //Add new file
    builder.file(filename, content);

    //Make new gist with old filename
    return createGistFromBuilder(builder,
        gistID.getFiles().values().iterator().next().getFileName());
  }

  /**
   * Create a new Gist.
   *
   * @param builder Gist builder
   * @param filename Gist file filename
   * @return Gist push URL
   */
  private static String createGistFromBuilder(GHGistBuilder builder, String filename) {
    GHGist gist;
    try {
      gist = builder.create();

      while (true) {
        try {
          ScriptingEngine.fileFromGit(gist.getGitPullUrl(), filename);
          break;
        } catch (GitAPIException e) {
          LoggerUtilities.getLogger().log(Level.INFO,
              "Waiting on Git API.");
        }

        ThreadUtil.wait(500);
      }

      return gist.getGitPullUrl();
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Unable to create gist from builder.\n" + Throwables.getStackTraceAsString(e));
    }
    return null;
  }
}
