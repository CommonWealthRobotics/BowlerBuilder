/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
