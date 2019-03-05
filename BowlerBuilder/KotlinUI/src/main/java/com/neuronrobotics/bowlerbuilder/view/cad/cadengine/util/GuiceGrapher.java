/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

/**
 * Graphs the Guice dependency graph.
 * https://stackoverflow.com/questions/9301007/is-there-any-way-to-get-guice-grapher-to-work
 */
public class GuiceGrapher {

  public static void graph(final String filename, final Injector inj) throws Exception {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintWriter out = new PrintWriter(baos);

    final Injector injector = Guice.createInjector(new GraphvizModule());
    final GraphvizGrapher renderer = injector.getInstance(GraphvizGrapher.class);
    renderer.setOut(out);
    renderer.setRankdir("TB");
    renderer.graph(inj);

    out = new PrintWriter(new File(filename), "UTF-8");
    String s = baos.toString("UTF-8");
    s = fixGrapherBug(s);
    s = hideClassPaths(s);
    out.write(s);
    out.close();
  }

  private static String hideClassPaths(String s) {
    s = s.replaceAll("\\w[a-z\\d_\\.]+\\.([A-Z][A-Za-z\\d_\\$]*)", "$1");
    s = s.replaceAll("value=[\\w-]+", "random");
    return s;
  }

  private static String fixGrapherBug(String s) {
    s = s.replaceAll("style=invis", "style=solid");
    s = s.replaceAll(" margin=(\\S+), ", " margin=\"$1\", ");
    return s;
  }
}
