package edu.ntnu.idi.idatt.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SettingsDialog extends Dialog<SettingsDialog.SettingsResult> {

  private Spinner<Integer> diceCountSpinner;

  public SettingsDialog(Stage owner, int currentDiceCount) {
    setTitle("Game Settings");
    initOwner(owner);

    // Set the button types
    ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    getDialogPane().getButtonTypes().addAll(applyButtonType, cancelButtonType);

    // Create the dice count spinner
    diceCountSpinner = new Spinner<>(1, 5, currentDiceCount);
    diceCountSpinner.setEditable(true);

    // Create the grid for the content
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    grid.add(new Label("Number of dice:"), 0, 0);
    grid.add(diceCountSpinner, 1, 0);

    getDialogPane().setContent(grid);

    // Convert the result to a settings object when the apply button is clicked
    setResultConverter(dialogButton -> {
      if (dialogButton == applyButtonType) {
        return new SettingsResult(diceCountSpinner.getValue());
      }
      return null;
    });
  }

  // Inner static class for result, clearly showing it's part of SettingsDialog
  public static class SettingsResult {
    private final int diceCount;

    public SettingsResult(int diceCount) {
      this.diceCount = diceCount;
    }

    public int getDiceCount() {
      return diceCount;
    }
  }
}