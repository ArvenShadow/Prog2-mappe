package edu.ntnu.idi.idatt.exception;

public class InvalidGameStateException extends RuntimeException {
  public InvalidGameStateException(String message) {
    super(message);
  }
}