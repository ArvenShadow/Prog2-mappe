package edu.ntnu.idi.idatt.view;

import edu.ntnu.idi.idatt.controller.CharacterSelectionController;
import edu.ntnu.idi.idatt.model.PlayerData;
import edu.ntnu.idi.idatt.view.components.PlayerPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CharacterSelectionView {
  private BorderPane root;
  private List<PlayerPanel> playerPanels;
  private List<String> availableTokens;

  // Handlers
  private Runnable startGameHandler;
  private Runnable backHandler;
  private Runnable savePlayersHandler;
  private Runnable loadPlayersHandler;

  private CharacterSelectionController controller;

  public CharacterSelectionView() {
    initializeTokens();
    createUI();

    // Create controller after UI is ready
    this.controller = new CharacterSelectionController(this);
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

    VBox contentBox = new VBox(20);
    contentBox.setAlignment(Pos.CENTER);

    // Title
    Label titleLabel = new Label("Select Players");
    titleLabel.setFont(javafx.scene.text.Font.font("Arial", 24));
    titleLabel.getStyleClass().add("title-label");

    // Create player panels
    GridPane playersGrid = createPlayerPanels();

    // Create button sections
    HBox fileOperationBox = createFileOperationButtons();
    HBox navigationBox = createNavigationButtons();

    contentBox.getChildren().addAll(titleLabel, playersGrid, fileOperationBox, navigationBox);
    root.setCenter(contentBox);
  }

  private GridPane createPlayerPanels() {
    playerPanels = new ArrayList<>();
    GridPane playersGrid = new GridPane();
    playersGrid.setHgap(20);
    playersGrid.setVgap(20);
    playersGrid.setAlignment(Pos.CENTER);

    // Create 4 player panels (2 active by default)
    for (int i = 1; i <= 4; i++) {
      PlayerPanel panel = new PlayerPanel(i, i <= 2, availableTokens);
      playerPanels.add(panel);

      int row = (i - 1) / 2;
      int col = (i - 1) % 2;
      playersGrid.add(panel.getPane(), col, row);
    }

    return playersGrid;
  }

  private HBox createFileOperationButtons() {
    Button savePlayersButton = new Button("Save Players");
    Button loadPlayersButton = new Button("Load Players");

    savePlayersButton.setOnAction(e -> {
      if (savePlayersHandler != null) {
        savePlayersHandler.run();
      }
    });

    loadPlayersButton.setOnAction(e -> {
      if (loadPlayersHandler != null) {
        loadPlayersHandler.run();
      }
    });

    HBox fileOperationBox = new HBox(15, savePlayersButton, loadPlayersButton);
    fileOperationBox.setAlignment(Pos.CENTER);
    return fileOperationBox;
  }

  private HBox createNavigationButtons() {
    Button backButton = new Button("Back");
    Button startButton = new Button("Start Game");

    backButton.setOnAction(e -> {
      if (backHandler != null) {
        backHandler.run();
      }
    });

    startButton.setOnAction(e -> {
      if (startGameHandler != null && validateAndStart()) {
        startGameHandler.run();
      }
    });

    HBox navigationBox = new HBox(20, backButton, startButton);
    navigationBox.setAlignment(Pos.CENTER);
    navigationBox.setPadding(new Insets(20, 0, 0, 0));
    return navigationBox;
  }

  private boolean validateAndStart() {
    List<PlayerData> selectedPlayers = getSelectedPlayers();
    return controller.validatePlayerSelection(selectedPlayers);
  }

  // Public methods for controller interaction
  public List<PlayerData> getActivePlayers() {
    List<PlayerData> activePlayers = new ArrayList<>();
    for (PlayerPanel panel : playerPanels) {
      if (panel.hasValidData()) {
        PlayerData data = new PlayerData();
        data.setName(panel.getPlayerName().trim());
        data.setToken(panel.getSelectedToken());
        activePlayers.add(data);
      }
    }
    return activePlayers;
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

  public void updatePlayersFromData(List<PlayerData> playerDataList) {
    // Reset all panels
    for (PlayerPanel panel : playerPanels) {
      panel.setActive(false);
      panel.clearData();
    }

    // Populate with loaded data
    int panelIndex = 0;
    for (PlayerData data : playerDataList) {
      if (panelIndex < playerPanels.size()) {
        PlayerPanel panel = playerPanels.get(panelIndex);
        panel.setActive(true);
        panel.setPlayerName(data.getName());
        panel.setSelectedToken(data.getToken());
        panelIndex++;
      }
    }
  }

  public String showSaveDialog() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Save Players");
    fileChooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter("CSV Files", "*.csv")
    );
    fileChooser.setInitialDirectory(new File("src/main/resources/playerFiles"));
    fileChooser.setInitialFileName("players.csv");

    File file = fileChooser.showSaveDialog(root.getScene().getWindow());
    return file != null ? file.getAbsolutePath() : null;
  }

  public String showLoadDialog() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Load Players");
    fileChooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter("CSV Files", "*.csv")
    );
    fileChooser.setInitialDirectory(new File("src/main/resources/playerFiles"));

    File file = fileChooser.showOpenDialog(root.getScene().getWindow());
    return file != null ? file.getAbsolutePath() : null;
  }

  public void showAlert(String title, String message, String alertType) {
    Alert.AlertType type = switch (alertType) {
      case "WARNING" -> Alert.AlertType.WARNING;
      case "ERROR" -> Alert.AlertType.ERROR;
      case "INFORMATION" -> Alert.AlertType.INFORMATION;
      default -> Alert.AlertType.NONE;
    };

    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  // Handler setters
  public void setStartGameHandler(Runnable handler) {
    this.startGameHandler = handler;
  }

  public void setBackHandler(Runnable handler) {
    this.backHandler = handler;
  }

  public void setSavePlayersHandler(Runnable handler) {
    this.savePlayersHandler = handler;
  }

  public void setLoadPlayersHandler(Runnable handler) {
    this.loadPlayersHandler = handler;
  }

  public Parent getRoot() {
    return root;
  }
}