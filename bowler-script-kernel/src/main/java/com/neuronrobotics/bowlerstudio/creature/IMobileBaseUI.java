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
package com.neuronrobotics.bowlerstudio.creature;

import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface IMobileBaseUI {

  /**
   * Replace all objects in the UI with these CSGs.
   *
   * @param toAdd CSGs to add
   * @param source script source file
   */
  void setAllCSG(Collection<CSG> toAdd, File source);

  /**
   * Add these objects to the UI.
   *
   * @param toAdd CSGs to add
   * @param source script source file
   */
  void addCSG(Collection<CSG> toAdd, File source);

  /**
   * Highlight the exception-causing lines in a file.
   *
   * @param fileEngineRunByName The file that was running when the exception occurred
   * @param ex the stack trace for file names of open files, or for open or executed file names
   */
  void highlightException(File fileEngineRunByName, Exception ex);

  /**
   * Return the CSGs currently visible in the UI.
   *
   * @return visible CSGs
   */
  Set<CSG> getVisibleCSGs();

  /**
   * Highlight the given list of CSGs. This should not change the CSG, just highlight it.
   *
   * @param selectedCsg the list to highlight NULL is used as a clear highlights
   */
  void setSelectedCsg(Collection<CSG> selectedCsg);

  default void selectCsgByFile(File script, int lineNumber) {
    List<CSG> objsFromScriptLine = new ArrayList<>();

    // check all visible CSGs
    for (CSG checker : getVisibleCSGs()) {
      for (String trace : checker.getCreationEventStackTraceList()) {
        String[] traceParts = trace.split(":");
        if (traceParts[0].trim().toLowerCase().contains(script.getName().toLowerCase().trim())) {
          int num = Integer.parseInt(traceParts[1].trim());

          if (num == lineNumber) {
            objsFromScriptLine.add(checker);
          }
        }
      }
    }

    if (objsFromScriptLine.size() > 0) {
      setSelectedCsg(objsFromScriptLine);
    }
  }

  default void setCsg(CSG toAdd, File source) {
    setAllCSG(Collections.singletonList(toAdd), source);
  }

  default void setCsg(List<CSG> toAdd) {
    setAllCSG(toAdd, null);
  }

  default void setCsg(CSG toAdd) {
    setAllCSG(Collections.singletonList(toAdd), null);
  }

  default void setCsg(MobileBaseCadManager thread, File cadScript) {
    setAllCSG(thread.getAllCad(), cadScript);
  }

  default void addCsg(CSG toAdd, File source) {
    addCSG(Collections.singletonList(toAdd), source);
  }
}
