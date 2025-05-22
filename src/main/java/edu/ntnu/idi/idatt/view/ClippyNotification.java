package edu.ntnu.idi.idatt.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class ClippyNotification extends VBox {
  private static final Duration FADE_IN_DURATION = Duration.millis(400);
  private static final Duration DISPLAY_DURATION = Duration.millis(3000);
  private static final Duration FADE_OUT_DURATION = Duration.millis(600);

  private ImageView clippyImage;
  private VBox speechBubble;
  private Label messageLabel;
  private SequentialTransition animation;

  public ClippyNotification() {
    createUI();
    setupAnimation();
    setVisible(false);
    setManaged(false);
  }

  private void createUI() {
    // Load Clippy image
    clippyImage = new ImageView();
    try {
      Image clippy = new Image(getClass().getResourceAsStream("/images/clippy.png"));
      clippyImage.setImage(clippy);
      clippyImage.setFitWidth(80);
      clippyImage.setFitHeight(80);
      clippyImage.setPreserveRatio(true);
    } catch (Exception e) {
      // Fallback if image not found
      clippyImage.setFitWidth(80);
      clippyImage.setFitHeight(80);
      System.out.println("Clippy image not found, using placeholder");
    }

    // Create speech bubble
    speechBubble = new VBox();
    speechBubble.setPadding(new Insets(10, 15, 10, 15));
    speechBubble.setStyle(
      "-fx-background-color: white; " +
        "-fx-border-color: #333333; " +
        "-fx-border-width: 2px; " +
        "-fx-border-radius: 10px; " +
        "-fx-background-radius: 10px; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 2, 2);"
    );
    speechBubble.setMaxWidth(200);
    speechBubble.setAlignment(Pos.CENTER);

    // Message label
    messageLabel = new Label();
    messageLabel.setFont(Font.font("Arial", 12));
    messageLabel.setTextFill(Color.BLACK);
    messageLabel.setWrapText(true);
    messageLabel.setTextAlignment(TextAlignment.CENTER);
    messageLabel.setAlignment(Pos.CENTER);

    speechBubble.getChildren().add(messageLabel);

    // Create speech bubble tail pointing to Clippy
    Polygon tail = new Polygon();
    tail.getPoints().addAll(new Double[]{
      0.0, 0.0,
      15.0, -10.0,
      15.0, 10.0
    });
    tail.setFill(Color.WHITE);
    tail.setStroke(Color.web("#333333"));
    tail.setStrokeWidth(2);

    // Layout: Clippy on left, speech bubble on right
    HBox content = new HBox(5);
    content.setAlignment(Pos.CENTER_LEFT);
    content.getChildren().addAll(clippyImage, tail, speechBubble);

    getChildren().add(content);
    setAlignment(Pos.CENTER_LEFT);
  }

  private void setupAnimation() {
    FadeTransition fadeIn = new FadeTransition(FADE_IN_DURATION, this);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    PauseTransition pause = new PauseTransition(DISPLAY_DURATION);

    FadeTransition fadeOut = new FadeTransition(FADE_OUT_DURATION, this);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> hideNotification());

    animation = new SequentialTransition(fadeIn, pause, fadeOut);
  }

  public void showNotification(String message) {
    messageLabel.setText(message);
    setVisible(true);
    setManaged(true);
    setOpacity(0.0);

    if (animation.getStatus() == SequentialTransition.Status.RUNNING) {
      animation.stop();
    }

    animation.play();
  }

  public void showNotification(String title, String message) {
    showNotification(title + "\n" + message);
  }

  private void hideNotification() {
    setVisible(false);
    setManaged(false);
  }
}