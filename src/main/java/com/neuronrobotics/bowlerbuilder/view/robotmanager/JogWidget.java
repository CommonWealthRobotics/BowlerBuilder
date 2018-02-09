package com.neuronrobotics.bowlerbuilder.view.robotmanager;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JogWidget {

  private DHParameterKinematics limb;
  private VBox view;
  private GridPane controlPane;

  public JogWidget(DHParameterKinematics limb) {
    this.limb = limb;

    view = new VBox(5);
    controlPane = new GridPane();

    view.setPadding(new Insets(5));
    controlPane.setHgap(5);
    controlPane.setVgap(5);

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

    view.getChildren().add(controlPane);

    TextField speed = new TextField("0.4");
    speed.setPrefWidth(40);
    HBox speedBox = new HBox(2, speed, new Label("m/s"));
    speedBox.setAlignment(Pos.CENTER_LEFT);

    TextField time = new TextField("0.03");
    time.setPrefWidth(40);
    HBox timeBox = new HBox(2, time, new Label("sec"));
    timeBox.setAlignment(Pos.CENTER_LEFT);

    view.getChildren().add(new HBox(5, speedBox, timeBox));
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
    controlPane.add(out, x, y);
    return out;
  }

  public Node getView() {
    return view;
  }

}
