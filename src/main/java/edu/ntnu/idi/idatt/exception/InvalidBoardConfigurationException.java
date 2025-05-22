package edu.ntnu.idi.idatt.exception;

public class InvalidBoardConfigurationException extends BoardGameException {
  public InvalidBoardConfigurationException(String message) {
    super(message);
  }

  public InvalidBoardConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}