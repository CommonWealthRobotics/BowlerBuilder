/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.util;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StringClipper {

  public StringClipper() {}

  /**
   * Clip a String to a maximum number of lines.
   *
   * @param input input string
   * @param lines max line count
   * @return clipped string
   */
  public String clipStringToLines(final String input, final Integer lines) {
    final String[] allLines = input.split("[\n|\r]");

    final StringBuilder out = new StringBuilder();
    final int upperBound = Math.min(lines, allLines.length);

    for (int i = 0; i < upperBound; i++) {
      final String line = allLines[i];
      out.append(line);
      if (i < upperBound - 1) {
        out.append('\n');
      }
    }

    return out.toString();
  }
}
