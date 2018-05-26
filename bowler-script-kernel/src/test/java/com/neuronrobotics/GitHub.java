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
package com.neuronrobotics;

import org.junit.jupiter.api.Test;

public class GitHub {

  @Test
  public void test() throws Exception {
    /*
        ScriptingEngine.runLogin();
        try {
          if (ScriptingEngine.getLoginID() == null) {
            return;
          }
          ScriptingEngine.setAutoupdate(true);
        } catch (Exception ex) {
          System.out.println("User not logged in, test can not run");
        }
        org.kohsuke.github.GitHub github = ScriptingEngine.getGithub();
        while (github == null) {
          github = ScriptingEngine.getGithub();
          ThreadUtil.wait(2000);
          System.out.println("Waiting for github");
        }
        Map<String, GHOrganization> orgs = github.getMyOrganizations();
        for (String org : orgs.keySet()) {
          System.out.println("Org: " + org);
          GHOrganization ghorg = orgs.get(org);
          Map<String, GHRepository> repos = ghorg.getRepositories();
          for (String orgRepo : repos.keySet()) {
            System.out.println("\tRepo " + org + " " + orgRepo);
          }
        }
        Map<String, Set<GHTeam>> teams = github.getMyTeams();
        for (String team : teams.keySet()) {
          System.out.println("Team " + team);
          Set<GHTeam> ghteam = teams.get(team);
          for (GHTeam ghT : ghteam) {
            System.out.println("\tGHTeam " + ghT.getName());
            Map<String, GHRepository> repos = ghT.getRepositories();
            for (String repoName : repos.keySet()) {
              System.out.println("\t\tGHTeam " + ghT.getName() + " repo " + repoName);
            }
          }
        }
        GHMyself self = github.getMyself();
        Map<String, GHRepository> myPublic = self.getAllRepositories();
        for (String myRepo : myPublic.keySet()) {
          System.out.println("Repo " + myRepo);
          GHRepository ghrepo = myPublic.get(myRepo);
          // if(ghrepo.getOwnerName().contains("demo"))
          System.out.println("\tOwner: " + ghrepo.getOwnerName() + " " + myRepo);
        }
        PagedIterable<GHRepository> watching = self.listSubscriptions();
        for (GHRepository g : watching) {
          System.out.println("Watching " + g.getOwnerName() + " " + g.getFullName());
        }
        String gitURL ="https://github.com/madhephaestus/clojure-utils.git";
        ArrayList<String> listofFiles = ScriptingEngine.filesInGit(gitURL,
            ScriptingEngine.getFullBranch(gitURL), null);
        if (listofFiles.size() == 0)
          fail();
        for (String s : listofFiles) {
          System.out.println("Files " + s);
        }
        String asstsRepo="https://github.com/madhephaestus/BowlerStudioImageAssets.git";

        // https://github.com/madhephaestus/BowlerStudioImageAssets.git
        ScriptingEngine.deleteRepo(asstsRepo);
        List<Ref> call = ScriptingEngine.listBranches(asstsRepo);
        System.out.println("Branches # " + call.size());
        if (call.size() > 0) {
          for (Ref ref : call) {
            System.out.println("Branch: Ref= " + ref + " name= " + ref.getName()
            + " ID = " + ref.getObjectId().getName());            }
        } else {
          fail();
        }

        ScriptingEngine.checkout(asstsRepo, call.get(0).getName());
        call = ScriptingEngine.listLocalBranches(asstsRepo);
        System.out.println("Local Branches # " + call.size());
        if (call.size() > 0) {
          for (Ref ref : call) {
            System.out.println("Branch: Ref= " + ref + " name= " + ref.getName()
            + " ID = " + ref.getObjectId().getName());
          }
        } else {
          fail();
        }
        //System.out.println("Creating branch # " );
    //        ScriptingEngine.newBranch(asstsRepo, "0.20.0");
    //        try{
    //            ScriptingEngine.deleteBranch(asstsRepo, "0.20.0");
    //        }catch(Exception e){
    //            e.printStackTrace();
    //        }
        System.out.println("Current Branch # " +  ScriptingEngine.getFullBranch(asstsRepo));
        */
  }
}
