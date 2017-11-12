package com.neuronrobotics.bowlerbuilder;

public class GistUtilities {
  private GistUtilities() {}

//  public static String createNewGist(String filename, String description, boolean isPublic)
//  {
//    //Setup gist
//    GitHub gitHub = ScriptingEngine.getGithub();
//    GHGistBuilder builder = gitHub.createGist();
//    builder.file(filename, "//Your code here");
//    builder.description(description);
//    builder.public_(isPublic);
//
//    //Make gist
//    return createGistFromBuilder(builder, filename);
//  }
//
//  public static String addFileToGist(String filename, String content, GHGist gistID)
//  {
//    GitHub gitHub = ScriptingEngine.getGithub();
//    try
//    {
//      //Copy from old gist
//      GHGist oldGist = gistID;
//      GHGistBuilder builder = gitHub.createGist();
//
//      builder.description(oldGist.getDescription());
//      builder.public_(oldGist.isPublic());
//
//      for (String key : oldGist.getFiles().keySet())
//        builder.file(key, oldGist.getFiles().get(key).getContent());
//
//      //Add new file
//      builder.file(filename, content);
//
//      //Make new gist with old filename
//      return createGistFromBuilder(builder, oldGist.getFiles().values().iterator().next().getFileName());
//
//
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
//    return null;
//  }
//
//  private static String createGistFromBuilder(GHGistBuilder builder, String filename)
//  {
//    GHGist gist;
//    try
//    {
//      gist = builder.create();
//      //String gistID = ScriptingEngine.urlToGist(gist.getHtmlUrl());
//
//      //BowlerStudio.openUrlInNewTab(new URL(gist.getHtmlUrl()));
//      System.out.println("Creating repo");
//      while (true)
//      {
//        try
//        {
//          ScriptingEngine.fileFromGit(gist.getGitPullUrl(), filename);
//          break;
//        }
//        catch (GitAPIException e)
//        {
//          e.printStackTrace();
//        }
//
//        ThreadUtil.wait(500);
//        Log.warn(filename + " not built yet");
//      }
//
//      System.out.println("Creating gist at " + filename);
//      return gist.getGitPullUrl();
//    }
//    catch (IOException e)
//    {
//      e.printStackTrace();
//    }
//    return null;
//  }
}
