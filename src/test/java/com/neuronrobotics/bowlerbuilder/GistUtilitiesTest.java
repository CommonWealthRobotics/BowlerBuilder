package com.neuronrobotics.bowlerbuilder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GistUtilitiesTest {

  @Test
  void testIsValidCodeFileName1() {
    assertTrue(GistUtilities.isValidCodeFileName("a.b").isPresent());
  }

  @Test
  void testIsValidCodeFileName2() {
    assertFalse(GistUtilities.isValidCodeFileName("a").isPresent());
  }

  @Test
  void testIsValidCodeFileName3() {
    assertFalse(GistUtilities.isValidCodeFileName("a.").isPresent());
  }

  @Test
  void testIsValidCodeFileName4() {
    assertFalse(GistUtilities.isValidCodeFileName("").isPresent());
  }

}
