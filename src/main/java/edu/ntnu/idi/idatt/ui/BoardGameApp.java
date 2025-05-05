package edu.ntnu.idi.idatt.ui;

import edu.ntnu.idi.idatt.exception.BoardGameException;
import edu.ntnu.idi.idatt.model.BoardGame;
import edu.ntnu.idi.idatt.model.Player;
import edu.ntnu.idi.idatt.model.Tile;

import java.util.Scanner;

public class BoardGameApp {
  private BoardGame boardGame;
  private Scanner scanner;
  private static final String DEFAULT_PLAYERS_FILE = "src/Test_users.csv";
  private static final String DEFAULT_BOARD_FILE = "src/main/resources/standard_board.json";

  public BoardGameApp() {
    this.boardGame = new BoardGame();
    this.scanner = new Scanner(System.in);
  }

  public void start() {
    System.out.println("Welcome to Snakes and Ladders!");

    // Initialize game
    initializeGame();

    // Game loop
    playGame();

    // Display winner and cleanup
    endGame();

    scanner.close();
  }

  private void initializeGame() {
    System.out.println("Game Setup Options:");
    System.out.println("1. Use standard board");
    System.out.println("2. Load board from file");
    int boardChoice = getIntInput(1, 2);

    if (boardChoice == 1) {
      boardGame.createBoard();
    } else {
      try {
        System.out.print("Enter board file path (or press Enter for default): ");
        String boardFilePath = scanner.nextLine();
        if (boardFilePath.isEmpty()) boardFilePath = DEFAULT_BOARD_FILE;

        boardGame.loadBoardFromFile(boardFilePath);
        System.out.println("Board loaded successfully!");
      } catch (BoardGameException e) {
        System.out.println("Error: " + e.getMessage() + ". Using default board.");
        boardGame.createBoard();
      }
    }

    boardGame.createDice();

    System.out.println("\nPlayer Setup Options:");
    System.out.println("1. Add players manually");
    System.out.println("2. Load players from file");
    int playerChoice = getIntInput(1, 2);

    if (playerChoice == 1) {
      addPlayers();
    } else {
      try {
        System.out.print("Enter players file path (or press Enter for default): ");
        String playersFilePath = scanner.nextLine();
        if (playersFilePath.isEmpty()) playersFilePath = DEFAULT_PLAYERS_FILE;

        boardGame.loadPlayersFromFile(playersFilePath);
        System.out.println("Players loaded successfully:");
        for (Player player : boardGame.getPlayers()) {
          System.out.println("- " + player.getName() + " (" + player.getTokenType() + ")");
        }
      } catch (BoardGameException e) {
        System.out.println("Error: " + e.getMessage() + ". Adding players manually.");
        addPlayers();
      }
    }

    placePlayers();
  }

  private void addPlayers() {
    System.out.print("Enter number of players (2-4): ");
    int numPlayers = getIntInput(2, 4);

    for (int i = 0; i < numPlayers; i++) {
      System.out.print("Enter name for Player " + (i+1) + ": ");
      String name = scanner.nextLine();

      System.out.println("Choose token type:");
      System.out.println("1. TopHat");
      System.out.println("2. RaceCar");
      System.out.println("3. Shoe");
      System.out.println("4. Thimble");
      System.out.print("Enter choice (1-4): ");

      int tokenChoice = getIntInput(1, 4);
      String tokenType;

      switch (tokenChoice) {
        case 1: tokenType = "TopHat"; break;
        case 2: tokenType = "RaceCar"; break;
        case 3: tokenType = "Shoe"; break;
        case 4: tokenType = "Thimble"; break;
        default: tokenType = "TopHat";
      }

      Player player = new Player(name, boardGame, tokenType);
      boardGame.addPlayer(player);

      System.out.println(name + " will use the " + tokenType + " token.");
    }
  }

  private void placePlayers() {
    Tile startTile = boardGame.getBoard().getTile(1);
    for (Player player : boardGame.getPlayers()) {
      player.placeOnTile(startTile);
    }
  }

  private void playGame() {
    int roundNumber = 1;

    while (!boardGame.isFinished()) {
      System.out.println("\n=== Round " + roundNumber + " ===");
      displayPlayerPositions();

      System.out.println("\nPress Enter to play round...");
      scanner.nextLine();

      boardGame.playOneRound();

      roundNumber++;
    }
  }

  private void displayPlayerPositions() {
    for (Player player : boardGame.getPlayers()) {
      Tile currentTile = player.getCurrentTile();
      int position = currentTile != null ? currentTile.getTileId() : 0;
      System.out.println(player.getName() + " (" + player.getTokenType() +
        ") is on tile " + position);
    }
  }

  private void endGame() {
    System.out.println("\n=== Game Over ===");
    System.out.println("The winner is: " + boardGame.getWinner().getName() +
      " (" + boardGame.getWinner().getTokenType() + ")");
  }

  private int getIntInput(int min, int max) {
    int choice = min;
    boolean validInput = false;

    while (!validInput) {
      try {
        String input = scanner.nextLine();
        choice = Integer.parseInt(input);

        if (choice >= min && choice <= max) {
          validInput = true;
        } else {
          System.out.print("Please enter a number between " + min +
            " and " + max + ": ");
        }
      } catch (NumberFormatException e) {
        System.out.print("Invalid input. Please enter a number: ");
      }
    }

    return choice;
  }

  public static void main(String[] args) {
    BoardGameApp app = new BoardGameApp();
    app.start();
  }
}