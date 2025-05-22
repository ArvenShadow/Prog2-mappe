package edu.ntnu.idi.idatt.view.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

public class PlayerPanel {
  private final int playerId;
  private boolean active;
  private GridPane pane;
  private TextField nameField;
  private ComboBox<String> tokenComboBox;
  private Button toggleButton;
  private ImageView tokenImageView;
  private List<String> availableTokens;

  public PlayerPanel(int playerId, boolean active, List<String> availableTokens) {
    this.playerId = playerId;
    this.active = active;
    this.availableTokens = availableTokens;
    createPanel();
  }

  private void createPanel() {
    pane = new GridPane();
    pane.setPadding(new Insets(10));
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setPrefSize(250, 150);
    pane.setBorder(new Border(new BorderStroke(
      Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT
    )));

    createComponents();
    layoutComponents();
    setupEventHandlers();
    updatePanelState();
  }

  private void createComponents() {
    Label titleLabel = new Label("Player " + playerId);
    titleLabel.setFont(javafx.scene.text.Font.font("Arial", 16));

    nameField = new TextField();
    nameField.setPromptText("Enter player name");
    nameField.setText("Player " + playerId);

    tokenComboBox = new ComboBox<>();
    tokenComboBox.getItems().addAll(availableTokens);
    tokenComboBox.setPromptText("Select token");

    tokenImageView = new ImageView();
    tokenImageView.setFitWidth(40);
    tokenImageView.setFitHeight(40);
    tokenImageView.setPreserveRatio(true);

    toggleButton = new Button(active ? "Remove" : "Add");

    // Store title label for layout
    pane.add(titleLabel, 0, 0, 2, 1);
  }

  private void layoutComponents() {
    pane.add(new Label("Name:"), 0, 1);
    pane.add(nameField, 1, 1);
    pane.add(new Label("Token:"), 0, 2);
    pane.add(tokenComboBox, 1, 2);
    pane.add(tokenImageView, 2, 2);

    // Only show toggle button for players 3 and 4
    if (playerId > 2) {
      pane.add(toggleButton, 0, 3, 3, 1);
    }
  }

  private void setupEventHandlers() {
    tokenComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      updateTokenPreview(newVal);
    });

    toggleButton.setOnAction(e -> {
      active = !active;
      updatePanelState();
    });
  }

  private void updateTokenPreview(String tokenName) {
    if (tokenName != null) {
      try {
        Image tokenImage = new Image(Objects.requireNonNull(
          getClass().getResourceAsStream("/images/tokens/" + tokenName.toLowerCase() + ".png")
        ));
        tokenImageView.setImage(tokenImage);
      } catch (Exception e) {
        tokenImageView.setImage(null);
      }
    } else {
      tokenImageView.setImage(null);
    }
  }

  private void updatePanelState() {
    toggleButton.setText(active ? "Remove" : "Add");
    nameField.setDisable(!active);
    tokenComboBox.setDisable(!active);
    pane.setOpacity(active ? 1.0 : 0.6);
  }

  // Public getters and setters
  public GridPane getPane() {
    return pane;
  }

  public int getPlayerId() {
    return playerId;
  }

  public boolean isActive() {
    return active;
  }

  public String getPlayerName() {
    return nameField.getText();
  }

  public String getSelectedToken() {
    return tokenComboBox.getValue();
  }

  public void setActive(boolean active) {
    this.active = active;
    updatePanelState();
  }

  public void setPlayerName(String name) {
    nameField.setText(name);
  }

  public void setSelectedToken(String token) {
    tokenComboBox.setValue(token);
  }

  public void clearData() {
    nameField.setText("Player " + playerId);
    tokenComboBox.setValue(null);
  }

  public boolean hasValidData() {
    return active &&
      getPlayerName() != null &&
      !getPlayerName().trim().isEmpty() &&
      getSelectedToken() != null;
  }
}