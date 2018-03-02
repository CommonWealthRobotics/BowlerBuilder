package com.neuronrobotics.bowlerbuilder;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistBuilder;
import org.kohsuke.github.GitHub;

public final class GistUtilities {

  private static final Logger LOGGER
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
  public static GHGist createNewGist(final String filename, final String description,
      final boolean isPublic)
      throws IOException {
    //Setup gist
    final GitHub gitHub = ScriptingEngine.getGithub();
    final GHGistBuilder builder = gitHub.createGist();

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
  private static GHGist createGistFromBuilder(final GHGistBuilder builder, final String filename)
      throws IOException {
    final GHGist gist;
    gist = builder.create();

    while (true) {
      try {
        ScriptingEngine.fileFromGit(gist.getGitPullUrl(), filename);
        break;
      } catch (final GitAPIException e) {
        LOGGER.log(Level.INFO, "Waiting on Git API.");
      }

      ThreadUtil.wait(500);
    }

    return gist;
  }

  /**
   * Validate a file name. A valid file name has an extension.
   *
   * @param fileName File name to validate
   * @return An optional containing a valid file name, empty otherwise
   */
  public static Optional<String> isValidCodeFileName(final String fileName) {
    if (fileName.matches("^.*\\.[^\\\\]+$")) {
      return Optional.of(fileName);
    }

    return Optional.empty();
  }

  /**
   * Will accept http:// or https:// with .git or .git/.
   *
   * @param url gist URL
   * @return optional containing a valid gist URL, empty otherwise
   */
  public static Optional<String> isValidGitURL(final String url) {
    //Any git URL is ((git|ssh|http(scale)?)|(git@[\w\.]+))(:(//)?)([\w\.@\:/\-~]+)(\.git)(/)?
    if (url.matches("(http(s)?)(:(//)?)([\\w.@:/\\-~]+)(\\.git)(/)?")) {
      return Optional.of(url);
    }

    return Optional.empty();
  }

}
