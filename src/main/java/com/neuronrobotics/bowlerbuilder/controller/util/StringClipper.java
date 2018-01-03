package com.neuronrobotics.bowlerbuilder.controller.util;

public class StringClipper {

  /**
   * Clip a String to a maximum number of lines.
   *
   * @param input input string
   * @param lines max line count
   * @return clipped string
   */
  public String clipStringToLines(String input, int lines) {
    String[] allLines = input.split("[\n|\r]");

    StringBuilder out = new StringBuilder();
    final int upperBound = Math.min(lines, allLines.length);

    for (int i = 0; i < upperBound; i++) {
      String line = allLines[i];
      out.append(line);
      if (i < upperBound - 1) {
        out.append('\n');
      }
    }

    return out.toString();
  }

}
