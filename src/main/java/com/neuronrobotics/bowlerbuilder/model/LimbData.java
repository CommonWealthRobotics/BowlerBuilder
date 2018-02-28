package com.neuronrobotics.bowlerbuilder.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LimbData {

  public String name;
  public List<Integer> indices;

  public LimbData(String name, Integer... indices) {
    this.name = name;
    this.indices = new ArrayList<>();
    Collections.addAll(this.indices, indices);
  }

  public LimbData(String name, List<Integer> indices) {
    this.name = name;
    this.indices = indices;
  }

}