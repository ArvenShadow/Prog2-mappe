import static org.junit.jupiter.api.Assertions.*;

import edu.ntnu.idi.idatt.model.Dice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class DiceTest {

  private Dice dice;

  @BeforeEach
  void setUp() {
    dice = new Dice(3); //Mekker 3 terninger
  }

  @Test
  void testCorrectNumberOfDIce(){
    assertEquals(3, dice.getNumberOfDice(), "Should be 3 dice");
  }

  @Test
  void testRollReturnsValidSum(){
    int sum = dice.Roll();
    assertTrue(sum >= 3 && sum <= 18, "Sum of 3 dice should be between 3 and 18");
  }

  @Test
  void testRollReturnsCorrectSum(){
    int value = dice.getDie(1);
    assertTrue(value >=1 && value <= 6, "Value must be between 1 and 6");
  }

  @Test
  void testRollChangesValues() {
    int oldValue1 = dice.getDie(0);
    int oldValue2 = dice.getDie(1);
    int oldValue3 = dice.getDie(2);

    dice.Roll();

    boolean changed = (dice.getDie(0) != oldValue1) ||
      (dice.getDie(1) != oldValue2) ||
      (dice.getDie(2) != oldValue3);

    assertTrue(changed, "One die should change value");
  }

  @Test
  void testGetDieThrowsExceptionForInvalidIndex() {
    assertThrows(IndexOutOfBoundsException.class, () -> dice.getDie(-1), "Negative index should throw exeption");
    assertThrows(IndexOutOfBoundsException.class, () -> dice.getDie(3), "Index bigger than number of dice should throw exeption");
  }

  @Test
  void testDiceConstructorThrowsExceptionForInvalidDiceCount() {
    assertThrows(IllegalArgumentException.class, () -> new Dice(0), "Can't make 0 dice");
  }
}





