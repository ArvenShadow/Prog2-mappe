package edu.ntnu.idi.idatt.model;

import java.util.Random;

public class Die {
    private static final Random random = new Random();
    private int value;
    private static final int SIDES = 6;

    /**
     * Creates a new die and rolls it to set an initial value.
     */
    public Die() {
        roll();
    }

    /**
     * Rolls the die and returns the new value.
     *
     * @return The new value of the die (1-6)
     */
    public int roll() {
        value = random.nextInt(SIDES) + 1;
        return value;
    }

    /**
     * Gets the current value of the die.
     *
     * @return The current value of the die
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the number of sides on this die.
     *
     * @return The number of sides
     */
    public int getSides() {
        return SIDES;
    }
}