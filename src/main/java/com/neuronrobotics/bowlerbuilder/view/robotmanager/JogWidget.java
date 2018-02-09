package com.neuronrobotics.bowlerbuilder.view.robotmanager;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class JogWidget extends GridPane {

  private DHParameterKinematics limb;

  public JogWidget(DHParameterKinematics limb) {
    this.limb = limb;

    setHgap(5);
    setVgap(5);
    setPadding(new Insets(5));

    Button plusX = addButton(AssetFactory.loadIcon("Plus-X.png"), "", 1, 0);
    Button minusX = addButton(AssetFactory.loadIcon("Minus-X.png"), "", 1, 2);

    Button plusY = addButton(AssetFactory.loadIcon("Plus-Y.png"), "", 0, 1);
    Button minusY = addButton(AssetFactory.loadIcon("Minus-Y.png"), "", 2, 1);

    Button plusZ = addButton(AssetFactory.loadIcon("Plus-Z.png"), "", 0, 0);
    Button minusZ = addButton(AssetFactory.loadIcon("Minus-Z.png"), "", 2, 0);

    Button home = addButton(AssetFactory.loadIcon("Home.png"), "", 1, 1);

    Button addJoystick = addButton(AssetFactory.loadIcon("Add-Game-Controller.png"), "", 3, 0);
    Button configureJoystick =
        addButton(AssetFactory.loadIcon("Configure-Game-Controller.png"), "", 3, 1);
  }

  /**
   * Add a button to the GridPane and return it.
   *
   * @param icon Graphic
   * @param text Text
   * @param x GridPane x coordinate
   * @param y GridPane y coordinate
   * @return Button
   */
  private Button addButton(ImageView icon, String text, int x, int y) {
    Button out = new Button(text);
    out.setGraphic(icon);
    add(out, x, y);
    return out;
  }

}
