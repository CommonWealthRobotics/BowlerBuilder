package com.neuronrobotics.bowlerbuilder;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

public final class GistUtilities {

  private static final Logger logger = LoggerUtilities.getLogger(GistUtilities.class.getSimpleName());

  /**
   * Create and publish a new gist.
   *
   * @param filename Gist filename for first file
   * @param description Gist description
   * @param isPublic Public/private viewing
   * @return New gist
   */
  public static GHGist createNewGist(String filename, String description, boolean isPublic)
      throws IOException {
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
   * Create a new Gist.
   *
   * @param builder Gist builder
   * @param filename Gist file filename
   * @return New gist
   */
  private static GHGist createGistFromBuilder(GHGistBuilder builder, String filename)
      throws IOException {
    GHGist gist;
    gist = builder.create();

    while (true) {
      try {
        ScriptingEngine.fileFromGit(gist.getGitPullUrl(), filename);
        break;
      } catch (GitAPIException e) {
        logger.log(Level.INFO, "Waiting on Git API.");
      }

      ThreadUtil.wait(500);
    }

    return gist;
  }

  /**
   * Use reflection to get the id of a gist, because the API does not provide this for some reason.
   *
   * @param gist gist with id
   * @return id of gist
   */
  public static Optional<String> getGistID(GHGist gist) {
    try {
      Field id = GHGist.class.getDeclaredField("id");
      return Optional.of((String) id.get(gist));
    } catch (Exception e) {
      logger.log(Level.SEVERE,
          "Could not get gist id.\n" + Throwables.getStackTraceAsString(e));
    }

    return Optional.empty();
  }

}
