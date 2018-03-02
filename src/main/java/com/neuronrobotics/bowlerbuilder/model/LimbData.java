package com.neuronrobotics.bowlerbuilder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LimbData {

  public final String name;
  public final List<Integer> indices;

  public LimbData(final String name, final Integer... indices) {
    this.name = name;
    this.indices = new ArrayList<>();
    Collections.addAll(this.indices, indices);
  }

  public LimbData(final String name, final List<Integer> indices) {
    this.name = name;
    this.indices = indices;
  }

}