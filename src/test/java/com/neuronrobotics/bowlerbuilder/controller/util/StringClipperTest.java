package com.neuronrobotics.bowlerbuilder.controller.util;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class StringClipperTest {

  private final StringClipper stringClipper = new StringClipper();

  @Test
  void emptyInputTest() {
    assertEquals("", stringClipper.clipStringToLines("", 1));
  }

  @Test
  void emptyInput0Test() {
    assertEquals("", stringClipper.clipStringToLines("", 0));
  }

  @Test
  void simpleInputTest() {
    assertEquals("test", stringClipper.clipStringToLines("test", 1));
  }

  @Test
  void simpleInput0Test() {
    assertEquals("", stringClipper.clipStringToLines("test", 0));
  }

  @Test
  void negativeLinesTest() {
    assertEquals("", stringClipper.clipStringToLines("test", -1));
  }

  @Test
  void oneLineTest() {
    assertEquals("a", stringClipper.clipStringToLines("a\nb", 1));
  }

  @Test
  void twoLineTest() {
    assertEquals("a\nb", stringClipper.clipStringToLines("a\nb", 2));
  }

  @Test
  void lineOOBTest() {
    assertEquals("a", stringClipper.clipStringToLines("a", 2));
  }

}
