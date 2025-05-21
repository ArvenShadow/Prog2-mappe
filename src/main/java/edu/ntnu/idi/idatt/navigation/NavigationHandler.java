package edu.ntnu.idi.idatt.navigation;

public interface NavigationHandler {
  /**
   * Navigate to a specific screen or game state.
   * @param target The target screen or game state
   */
  void navigateTo(NavTo target);

  /**
   * Return to the previous screen.
   */
  void navigateBack();

  /**
   * Set the root view for the current screen.
   * @param root The root view to be displayed
   */
  void setRoot(javafx.scene.Parent root);
}
