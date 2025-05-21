package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.model.PlayerData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CharacterSelectionView {
  private BorderPane root;
  private List<PlayerPanel> playerPanels;
  private List<String> availableTokens;
  private Runnable startGameHandler;
  private Runnable backHandler;

  public CharacterSelectionView() {
    initializeTokens();
    createUI();
  }

  private void initializeTokens() {
    availableTokens = new ArrayList<>();
    availableTokens.add("TopHat");
    availableTokens.add("RaceCar");
    availableTokens.add("Shoe");
    availableTokens.add("Thimble");
    availableTokens.add("Cat");
  }

  private void createUI() {
    root = new BorderPane();
    root.setPadding(new Insets(20));
    root.getStyleClass().add("character-selection");

    // Title
    Label titleLabel = new Label("Select Players");
    titleLabel.setFont(javafx.scene.text.Font.font("Arial", 24));
    titleLabel.getStyleClass().add("title-label");

    // Player panels
    playerPanels = new ArrayList<>();

    // Create a GridPane to hold player panels
    GridPane playersGrid = new GridPane();
    playersGrid.setHgap(20);
    playersGrid.setVgap(20);
    playersGrid.setAlignment(Pos.CENTER);

    // Add two default active players
    PlayerPanel player1 = new PlayerPanel(1, true);
    PlayerPanel player2 = new PlayerPanel(2, true);
    playerPanels.add(player1);
    playerPanels.add(player2);

    // Add two inactive players
    PlayerPanel player3 = new PlayerPanel(3, false);
    PlayerPanel player4 = new PlayerPanel(4, false);
    playerPanels.add(player3);
    playerPanels.add(player4);

    // Add to grid
    playersGrid.add(player1.getPane(), 0, 0);
    playersGrid.add(player2.getPane(), 1, 0);
    playersGrid.add(player3.getPane(), 0, 1);
    playersGrid.add(player4.getPane(), 1, 1);

    // Navigation buttons
    Button backButton = new Button("Back");
    Button startButton = new Button("Start Game");

    backButton.setOnAction(e -> {
      if (backHandler != null) {
        backHandler.run();
      }
    });

    startButton.setOnAction(e -> {
      if (startGameHandler != null && validatePlayerSelection()) {
        startGameHandler.run();
      }
    });

    HBox buttonBox = new HBox(20, backButton, startButton);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(20, 0, 0, 0));

    // Layout
    VBox contentBox = new VBox(20, titleLabel, playersGrid, buttonBox);
    contentBox.setAlignment(Pos.CENTER);

    root.setCenter(contentBox);
  }

  private boolean validatePlayerSelection() {
    int activePlayers = 0;
    for (PlayerPanel panel : playerPanels) {
      if (panel.isActive()) {
        // Check name and token selection
        if (panel.getPlayerName().trim().isEmpty()) {
          showAlert("Invalid Selection", "Player " + panel.getPlayerId() + " needs a name.");
          return false;
        }
        if (panel.getSelectedToken() == null) {
          showAlert("Invalid Selection", "Player " + panel.getPlayerId() + " needs to select a token.");
          return false;
        }
        activePlayers++;
      }
    }

    if (activePlayers < 2) {
      showAlert("Invalid Selection", "At least 2 players are required.");
      return false;
    }

    // Check for duplicate tokens
    List<String> usedTokens = new ArrayList<>();
    for (PlayerPanel panel : playerPanels) {
      if (panel.isActive()) {
        String token = panel.getSelectedToken();
        if (usedTokens.contains(token)) {
          showAlert("Invalid Selection", "Each player must have a unique token.");
          return false;
        }
        usedTokens.add(token);
      }
    }

    return true;
  }

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public List<PlayerData> getSelectedPlayers() {
    List<PlayerData> selectedPlayers = new ArrayList<>();
    for (PlayerPanel panel : playerPanels) {
      if (panel.isActive()) {
        PlayerData data = new PlayerData();
        data.setName(panel.getPlayerName());
        data.setToken(panel.getSelectedToken());
        selectedPlayers.add(data);
      }
    }
    return selectedPlayers;
  }

  public Parent getRoot() {
    return root;
  }

  public void setStartGameHandler(Runnable handler) {
    this.startGameHandler = handler;
  }

  public void setBackHandler(Runnable handler) {
    this.backHandler = handler;
  }

  // Inner class to represent a player panel
  private class PlayerPanel {
    private final int playerId;
    private boolean active;
    private GridPane pane;
    private TextField nameField;
    private ComboBox<String> tokenComboBox;
    private Button toggleButton;

    public PlayerPanel(int playerId, boolean active) {
      this.playerId = playerId;
      this.active = active;
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

      Label titleLabel = new Label("Player " + playerId);
      titleLabel.setFont(javafx.scene.text.Font.font("Arial", 16));

      nameField = new TextField();
      nameField.setPromptText("Enter player name");
      nameField.setText("Player " + playerId);

      tokenComboBox = new ComboBox<>();
      tokenComboBox.getItems().addAll(availableTokens);
      tokenComboBox.setPromptText("Select token");

      // Token preview
      ImageView tokenImageView = new ImageView();
      tokenImageView.setFitWidth(40);
      tokenImageView.setFitHeight(40);
      tokenImageView.setPreserveRatio(true);

      tokenComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal != null) {
          try {
            Image tokenImage = new Image(Objects.requireNonNull(
              getClass().getResourceAsStream("/images/tokens/" + newVal.toLowerCase() + ".png")
            ));
            tokenImageView.setImage(tokenImage);
          } catch (Exception e) {
            tokenImageView.setImage(null);
          }
        } else {
          tokenImageView.setImage(null);
        }
      });

      toggleButton = new Button(active ? "Remove" : "Add");
      toggleButton.setOnAction(e -> {
        active = !active;
        updatePanelState();
      });

      pane.add(titleLabel, 0, 0, 2, 1);
      pane.add(new Label("Name:"), 0, 1);
      pane.add(nameField, 1, 1);
      pane.add(new Label("Token:"), 0, 2);
      pane.add(tokenComboBox, 1, 2);
      pane.add(tokenImageView, 2, 2);

      // Add toggle button for players 3 and 4
      if (playerId > 2) {
        pane.add(toggleButton, 0, 3, 3, 1);
      }

      updatePanelState();
    }

    private void updatePanelState() {
      toggleButton.setText(active ? "Remove" : "Add");
      nameField.setDisable(!active);
      tokenComboBox.setDisable(!active);

      if (active) {
        pane.setOpacity(1.0);
      } else {
        pane.setOpacity(0.6);
      }
    }

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
  }
}