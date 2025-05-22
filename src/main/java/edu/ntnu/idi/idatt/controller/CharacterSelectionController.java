package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.io.PlayerCsvHandler;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.PlayerData;
import edu.ntnu.idi.idatt.view.CharacterSelectionView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterSelectionController {
  private CharacterSelectionView view;
  private PlayerCsvHandler csvHandler;

  public CharacterSelectionController(CharacterSelectionView view) {
    this.view = view;
    this.csvHandler = new PlayerCsvHandler(new BoardGame()); // Temporary game for CSV operations

    // Set up view event handlers
    view.setSavePlayersHandler(this::handleSavePlayers);
    view.setLoadPlayersHandler(this::handleLoadPlayers);
  }

  private void handleSavePlayers() {
    try {
      List<PlayerData> activePlayers = view.getActivePlayers();

      if (activePlayers.isEmpty()) {
        view.showAlert("No Players", "No active players to save.", "WARNING");
        return;
      }

      String filename = view.showSaveDialog();
      if (filename != null) {
        List<Player> players = convertToPlayers(activePlayers);
        csvHandler.writeToFile(players, filename);

        String fileName = new File(filename).getName();
        view.showAlert("Success", "Players saved successfully to " + fileName, "INFORMATION");
      }

    } catch (BoardGameException e) {
      view.showAlert("Save Error", "Failed to save players: " + e.getMessage(), "ERROR");
    } catch (Exception e) {
      view.showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), "ERROR");
    }
  }

  private void handleLoadPlayers() {
    try {
      String filename = view.showLoadDialog();
      if (filename != null) {
        List<Player> loadedPlayers = csvHandler.readFromFile(filename);
        List<PlayerData> playerDataList = convertToPlayerData(loadedPlayers);

        view.updatePlayersFromData(playerDataList);

        String fileName = new File(filename).getName();
        view.showAlert("Success", "Players loaded successfully from " + fileName, "INFORMATION");
      }

    } catch (BoardGameException e) {
      view.showAlert("Load Error", "Failed to load players: " + e.getMessage(), "ERROR");
    } catch (Exception e) {
      view.showAlert("Unexpected Error", "An unexpected error occurred: " + e.getMessage(), "ERROR");
    }
  }

  /**
   * Validates player selection according to game rules
   */
  public boolean validatePlayerSelection(List<PlayerData> players) {
    if (players.size() < 2) {
      view.showAlert("Invalid Selection", "At least 2 players are required.", "WARNING");
      return false;
    }

    // Check for empty names
    for (int i = 0; i < players.size(); i++) {
      PlayerData player = players.get(i);
      if (player.getName() == null || player.getName().trim().isEmpty()) {
        view.showAlert("Invalid Selection", "Player " + (i + 1) + " needs a name.", "WARNING");
        return false;
      }
      if (player.getToken() == null) {
        view.showAlert("Invalid Selection", "Player " + (i + 1) + " needs to select a token.", "WARNING");
        return false;
      }
    }

    // Check for duplicate tokens
    Set<String> usedTokens = new HashSet<>();
    for (PlayerData player : players) {
      if (usedTokens.contains(player.getToken())) {
        view.showAlert("Invalid Selection", "Each player must have a unique token.", "WARNING");
        return false;
      }
      usedTokens.add(player.getToken());
    }

    return true;
  }

  /**
   * Converts PlayerData objects to Player objects for CSV operations
   */
  private List<Player> convertToPlayers(List<PlayerData> playerDataList) {
    BoardGame tempGame = new BoardGame();
    List<Player> players = new ArrayList<>();
    for (PlayerData data : playerDataList) {
      players.add(new Player(data.getName(), tempGame, data.getToken()));
    }
    return players;
  }

  /**
   * Converts Player objects to PlayerData objects for UI
   */
  private List<PlayerData> convertToPlayerData(List<Player> players) {
    List<PlayerData> playerDataList = new ArrayList<>();
    for (Player player : players) {
      PlayerData data = new PlayerData();
      data.setName(player.getName());
      data.setToken(player.getTokenType());
      playerDataList.add(data);
    }
    return playerDataList;
  }
}