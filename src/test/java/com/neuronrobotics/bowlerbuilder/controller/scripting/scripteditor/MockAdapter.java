/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor;

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.WebEngineAdapter;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;

public class MockAdapter implements WebEngineAdapter {
  public String lastExecutedScript = "";

  @Override
  public Object executeScript(String script) {
    lastExecutedScript = script;
    return null;
  }

  @Override
  public Worker<Void> getLoadWorker() {
    return new Worker<Void>() {
      @Override
      public State getState() {
        return State.SUCCEEDED;
      }

      @Override
      public ReadOnlyObjectProperty<State> stateProperty() {
        return new SimpleObjectProperty<>(State.SUCCEEDED);
      }

      @Override
      public Void getValue() {
        return null;
      }

      @Override
      public ReadOnlyObjectProperty<Void> valueProperty() {
        return null;
      }

      @Override
      public Throwable getException() {
        return null;
      }

      @Override
      public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return null;
      }

      @Override
      public double getWorkDone() {
        return 0;
      }

      @Override
      public ReadOnlyDoubleProperty workDoneProperty() {
        return null;
      }

      @Override
      public double getTotalWork() {
        return 0;
      }

      @Override
      public ReadOnlyDoubleProperty totalWorkProperty() {
        return null;
      }

      @Override
      public double getProgress() {
        return 0;
      }

      @Override
      public ReadOnlyDoubleProperty progressProperty() {
        return null;
      }

      @Override
      public boolean isRunning() {
        return false;
      }

      @Override
      public ReadOnlyBooleanProperty runningProperty() {
        return null;
      }

      @Override
      public String getMessage() {
        return null;
      }

      @Override
      public ReadOnlyStringProperty messageProperty() {
        return null;
      }

      @Override
      public String getTitle() {
        return null;
      }

      @Override
      public ReadOnlyStringProperty titleProperty() {
        return null;
      }

      @Override
      public boolean cancel() {
        return false;
      }
    };
  }
}
