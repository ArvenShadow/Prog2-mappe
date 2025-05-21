package edu.ntnu.idi.idatt.controller;

import edu.ntnu.idi.idatt.navigation.NavTo;
import edu.ntnu.idi.idatt.navigation.Navigation;

public class BaseController {
  protected final Navigation navigation;

  public BaseController(Navigation navigation) {
    this.navigation = Navigation.getInstance();
  }

  protected void navigateTo(NavTo target) {
    navigation.navigateTo(target);
  }

  protected void navigateBack() {
    navigation.navigateBack();
  }
}
