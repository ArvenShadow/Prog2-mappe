import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.model.Die;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DieTest {

  private Die die;

  @BeforeEach
  void setUp() {
    die = new Die(); //
  }

  @Test
  void testInitialValueIsWithinRange() {
    // Sikrer at verdien satt ved opprettelse av en Die er mellom 1 og 6
    int value = die.getValue();
    assertTrue(value >= 1 && value <= 6, "Initial value must be between 1 and 6");
  }

  @Test
  void testRollReturnsValueWithinRange() {
    // Tester at roll() alltid returnerer en verdi innenfor området 1–6
    for (int i = 0; i < 100; i++) { // Ruller flere ganger for å teste flere resultater
      int rollValue = die.roll();
      assertTrue(rollValue >= 1 && rollValue <= 6, "Roll value must be between 1 and 6");
    }
  }

  @Test
  void testGetValueMatchesLastRolledValue() {
    // Ruller og henter verdi, og sikrer at getValue() returnerer den siste verdien
    int rollValue = die.roll();
    assertEquals(rollValue, die.getValue(), "getValue() should return the last rolled value");
  }
}