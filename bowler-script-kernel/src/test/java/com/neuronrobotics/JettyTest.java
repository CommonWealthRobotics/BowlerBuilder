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

import static org.junit.Assert.fail;

import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JettyTest {
  private static int WEBSERVER_PORT = 8065;
  private static String HOME_Local_URL =
      "http://localhost:" + WEBSERVER_PORT + "/BowlerStudio/Welcome-To-BowlerStudio/";

  private Server server;

  @Before
  void startJetty() throws Exception {
    try {
      ScriptingEngine.setupAnyonmous();
      // ScriptingEngine.setAutoupdate(true);
    } catch (final Exception ex) {
      System.out.println("User not logged in, test can not run");
    }
    final File indexOfTutorial =
        ScriptingEngine.fileFromGit(
            "https://github.com/CommonWealthRobotics/CommonWealthRobotics.github.io.git",
            "master", // the default branch is source, so this needs to be specified
            "index.html");

    // HOME_Local_URL = indexOfTutorial.toURI().toString().replace("file:/",
    // "file:///");
    server = new Server();
    final ServerConnector connector = new ServerConnector(server);
    server.setConnectors(new Connector[] {connector});

    final ResourceHandler resource_handler = new ResourceHandler();
    resource_handler.setDirectoriesListed(true);
    resource_handler.setWelcomeFiles(new String[] {"index.html"});
    System.out.println("Serving " + indexOfTutorial.getParent());
    resource_handler.setResourceBase(indexOfTutorial.getParent());

    final HandlerList handlers = new HandlerList();
    handlers.setHandlers(new Handler[] {resource_handler, new DefaultHandler()});
    server.setHandler(handlers);

    // Start Server
    server.start();
    WEBSERVER_PORT = connector.getLocalPort();
    HOME_Local_URL =
        "http://localhost:" + WEBSERVER_PORT + "/BowlerStudio/Welcome-To-BowlerStudio/";
  }

  @Test
  @Disabled
  void test() {

    try {
      final InputStream url = new URL(HOME_Local_URL).openStream();
      try {
        System.out.println(IOUtils.toString(url));
      } finally {
        IOUtils.closeQuietly(url);
      }

      // read from your scanner
    } catch (final IOException ex) {
      // there was some connection problem, or the file did not exist on
      // the server,
      // or your URL was not in the right format.
      // think about what to do now, and put it here.
      ex.printStackTrace(); // for now, simply output it.
      fail(ex.getMessage());
    }
  }

  @After
  void stopJetty() {
    try {
      server.stop();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
