package edu.ntnu.idi.idatt.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Objects;

public class MainMenuView {
  private BorderPane root;
  private Runnable newGameHandler;
  private Runnable loadGameHandler;
  private Runnable exitHandler;

  public MainMenuView() {
    createUI();
  }

  private void createUI() {
    root = new BorderPane();
    root.setPadding(new Insets(20));
    root.getStyleClass().add("main-menu");

    // Title
    Label titleLabel = new Label("Board Game");
    titleLabel.setFont(Font.font("Arial", 36));
    titleLabel.getStyleClass().add("title-label");

    // Menu buttons
    Button newGameButton = new Button("New Game");
    Button loadGameButton = new Button("Load Game");
    Button exitButton = new Button("Exit");

    // Style buttons
    for (Button button : new Button[]{newGameButton, loadGameButton, exitButton}) {
      button.setPrefWidth(200);
      button.setPrefHeight(40);
      button.getStyleClass().add("menu-button");
    }

    // Add event handlers
    newGameButton.setOnAction(e -> {
      if (newGameHandler != null) {
        newGameHandler.run();
      }
    });

    loadGameButton.setOnAction(e -> {
      if (loadGameHandler != null) {
        loadGameHandler.run();
      }
    });

    exitButton.setOnAction(e -> {
      if (exitHandler != null) {
        exitHandler.run();
      }
    });

    // Layout
    VBox menuBox = new VBox(20);
    menuBox.setAlignment(Pos.CENTER);
    menuBox.getChildren().addAll(titleLabel, newGameButton, loadGameButton, exitButton);

    // Try to load a background image if available
    try {
      Image bgImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/background.jpg")));
      ImageView bgView = new ImageView(bgImage);
      bgView.setPreserveRatio(true);
      bgView.fitWidthProperty().bind(root.widthProperty());
      bgView.fitHeightProperty().bind(root.heightProperty());
      bgView.setOpacity(0.3);
      root.setBackground(null);
      root.getChildren().add(bgView);
    } catch (Exception e) {
      // Fallback if image can't be loaded
      root.setStyle("-fx-background-color: linear-gradient(to bottom, #f2f2f2, #d9d9d9);");
    }

    root.setCenter(menuBox);
  }

  public Parent getRoot() {
    return root;
  }

  public void setNewGameHandler(Runnable handler) {
    this.newGameHandler = handler;
  }

  public void setLoadGameHandler(Runnable handler) {
    this.loadGameHandler = handler;
  }

  public void setExitHandler(Runnable handler) {
    this.exitHandler = handler;
  }
}