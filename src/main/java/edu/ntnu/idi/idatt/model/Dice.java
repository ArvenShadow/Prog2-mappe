package edu.ntnu.idi.idatt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dice {
    private List<Die> dice;
    private final Random random = new Random();
    private static final int DEFAULT_NUMBER_OF_DICE = 2;
    private static final int MAX_DICE = 4;

    /**
     * Creates a dice collection with the default number of dice (2).
     */
    public Dice() {
        this(DEFAULT_NUMBER_OF_DICE);
    }

    /**
     * Creates a dice collection with the specified number of dice.
     *
     * @param numberOfDice The number of dice to create
     * @throws IllegalArgumentException if numberOfDice is less than 1 or greater than MAX_DICE
     */
    public Dice(int numberOfDice) {
        if (numberOfDice < 1) {
            throw new IllegalArgumentException("You must have at least 1 die");
        }
        if (numberOfDice > MAX_DICE) {
            throw new IllegalArgumentException("Maximum number of dice is " + MAX_DICE);
        }

        this.dice = new ArrayList<>();
        for (int i = 0; i < numberOfDice; i++) {
            dice.add(new Die());
        }
    }

    /**
     * Rolls all dice and returns the total value.
     *
     * @return The sum of all dice values
     */
    public int Roll() {
        int total = 0;

        for (Die die : dice) {
            total += die.roll();
        }

        return total;
    }

    /**
     * Changes the number of dice, creating a new collection.
     * Preserves existing dice values where possible.
     *
     * @param numberOfDice The new number of dice
     * @throws IllegalArgumentException if numberOfDice is less than 1 or greater than MAX_DICE
     */
    public void setNumberOfDice(int numberOfDice) {
        if (numberOfDice < 1) {
            throw new IllegalArgumentException("You must have at least 1 die");
        }
        if (numberOfDice > MAX_DICE) {
            throw new IllegalArgumentException("Maximum number of dice is " + MAX_DICE);
        }

        // Store current dice values to preserve them if possible
        int[] currentValues = new int[dice.size()];
        for (int i = 0; i < dice.size(); i++) {
            currentValues[i] = dice.get(i).getValue();
        }

        // Create a new dice collection
        List<Die> newDice = new ArrayList<>();

        // Add dice with preserved values for existing dice
        for (int i = 0; i < numberOfDice; i++) {
            Die die;
            if (i < currentValues.length) {
                // Preserve value from existing die
                die = new Die();
                // We need to set the value explicitly since the constructor rolls
                // This is a bit of a hack, but works for our purposes
                for (int j = 0; j < 10 && die.getValue() != currentValues[i]; j++) {
                    die.roll(); // Roll until we get the desired value or give up after 10 tries
                }
            } else {
                // New die with random value
                die = new Die();
            }
            newDice.add(die);
        }

        this.dice = newDice;
    }

    /**
     * Gets the value of a specific die.
     *
     * @param dieIndex The index of the die to get (0-based)
     * @return The value of the die
     * @throws IndexOutOfBoundsException if dieIndex is out of range
     */
    public int getDieValue(int dieIndex) {
        if (dieIndex < 0 || dieIndex >= dice.size()) {
            throw new IndexOutOfBoundsException("Die index out of range: " + dieIndex);
        }
        return dice.get(dieIndex).getValue();
    }

    /**
     * Gets all individual die values.
     *
     * @return An array of die values
     */
    public int[] getAllDiceValues() {
        int[] values = new int[dice.size()];
        for (int i = 0; i < dice.size(); i++) {
            values[i] = dice.get(i).getValue();
        }
        return values;
    }

    /**
     * Gets the number of dice in this collection.
     *
     * @return The number of dice
     */
    public int getNumberOfDice() {
        return dice.size();
    }

    /**
     * Gets the maximum number of dice allowed.
     *
     * @return The maximum number of dice
     */
    public static int getMaxDice() {
        return MAX_DICE;
    }
}