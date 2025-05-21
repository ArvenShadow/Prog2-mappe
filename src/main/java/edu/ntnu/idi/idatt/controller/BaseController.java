package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.navigation.NavigationManager;

public class BaseController {
  protected final NavigationManager navigationManager;

  public BaseController(NavigationManager navigationManager) {
    this.navigationManager = NavigationManager.getInstance();
  }

  protected void navigateTo(NavTo target) {
    navigationManager.navigateTo(target);
  }

  protected void navigateBack() {
    navigationManager.navigateBack();
  }
}
