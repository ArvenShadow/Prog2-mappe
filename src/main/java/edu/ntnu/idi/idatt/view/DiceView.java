package edu.ntnu.idi.idatt.view;

import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class DiceView extends Group {
  private List<SingleDieView> diceViews = new ArrayList<>();
  private Text totalText;
  private HBox diceContainer;

  private static final double DIE_SIZE = 60;
  private static final double DIE_SPACING = 10;
  private static final int DEFAULT_DICE_COUNT = 1;

  public DiceView() {
    this(DEFAULT_DICE_COUNT);
  }

  public DiceView(int diceCount) {
    diceContainer = new HBox(DIE_SPACING);
    diceContainer.setPadding(new Insets(5));

    for (int i = 0; i < diceCount; i++) {
      SingleDieView dieView = new SingleDieView();
      diceViews.add(dieView);
      diceContainer.getChildren().add(dieView);
    }

    totalText = new Text("Total: " + getTotal());
    totalText.setTranslateY(DIE_SIZE + 20);
    totalText.setTranslateX(DIE_SIZE / 2);

    getChildren().addAll(diceContainer, totalText);

    setValues(new int[diceCount]);
  }

  public void setValues(int[] values) {
    if (values.length != diceViews.size()) {
      throw new IllegalArgumentException("Values array length must match dice count");
    }

    for (int i = 0; i < values.length; i++) {
      diceViews.get(i).setValue(values[i]);
    }

    totalText.setText("Total: " + getTotal());
  }

  public int getTotal() {
    int total = 0;
    for (SingleDieView dieView : diceViews) {
      total += dieView.getValue();
    }
    return total;
  }

  public void roll() {
    for (SingleDieView dieView : diceViews) {
      dieView.roll();
    }
  }

  private static class SingleDieView extends Group {
    private final Rectangle dice;
    private final Circle[] dots;
    private int value;

    public SingleDieView() {
      dice = new Rectangle(0, 0, DIE_SIZE, DIE_SIZE);
      dice.setFill(Color.WHITE);
      dice.setStroke(Color.BLACK);
      dice.setArcHeight(15);
      dice.setArcWidth(15);

      dots = new Circle[9];

      for (int i = 0; i < dots.length; i++) {
        int row = i / 3;
        int col = i % 3;

        double x = (col + 1) * DIE_SIZE / 4;
        double y = (row + 1) * DIE_SIZE / 4;

        dots[i] = new Circle(x, y, DIE_SIZE / 10, Color.BLACK);
        dots[i].setVisible(false);
      }

      getChildren().add(dice);
      for (Circle dot : dots) {
        getChildren().add(dot);
      }

      setValue(1);
    }

    public void setValue(int value) {
      if (value < 1 || value > 6) {
        value = 1; // Default to 1 for invalid values
      }

      this.value = value;

      // Hide all dots
      for (Circle dot : dots) {
        dot.setVisible(false);
      }

      // Show dots based on value
      switch (value) {
        case 1:
          // Center dot
          dots[4].setVisible(true);
          break;
        case 2:
          // Top-left and bottom-right
          dots[0].setVisible(true);
          dots[8].setVisible(true);
          break;
        case 3:
          // Top-left, center, and bottom-right
          dots[0].setVisible(true);
          dots[4].setVisible(true);
          dots[8].setVisible(true);
          break;
        case 4:
          // Four corners
          dots[0].setVisible(true);
          dots[2].setVisible(true);
          dots[6].setVisible(true);
          dots[8].setVisible(true);
          break;
        case 5:
          // Four corners and center
          dots[0].setVisible(true);
          dots[2].setVisible(true);
          dots[4].setVisible(true);
          dots[6].setVisible(true);
          dots[8].setVisible(true);
          break;
        case 6:
          // All dots except center
          dots[0].setVisible(true);
          dots[2].setVisible(true);
          dots[3].setVisible(true);
          dots[5].setVisible(true);
          dots[6].setVisible(true);
          dots[8].setVisible(true);
          break;
      }
    }

    public int getValue() {
      return value;
    }

    public void roll() {
      // Animate the die roll
      RotateTransition rotation = new RotateTransition(Duration.millis(500), this);
      rotation.setByAngle(360);
      rotation.setCycleCount(2);
      rotation.play();
    }
  }
}
