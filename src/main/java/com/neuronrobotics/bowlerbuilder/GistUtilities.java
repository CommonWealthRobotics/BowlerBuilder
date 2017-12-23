package com.neuronrobotics.bowlerbuilder;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

public final class GistUtilities {

  private static final Logger logger
      = LoggerUtilities.getLogger(GistUtilities.class.getSimpleName());

  private GistUtilities() {
  }

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

}
