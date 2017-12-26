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
    for (int i = 0; i < lines; i++) {
      String line = allLines[i];
      out.append(line);
      if (i < lines - 1) {
        out.append('\n');
      }
    }

    return out.toString();
  }

}
