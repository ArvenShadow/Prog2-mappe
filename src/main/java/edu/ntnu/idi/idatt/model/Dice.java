package edu.ntnu.idi.idatt.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Dice {

    private ArrayList<Die> dice;
    private int[] lastRoll;

    public Dice(int numberOfDice){
        if(numberOfDice < 1){
            throw new IllegalArgumentException("You must have at least 1 die");
        }
        dice = new ArrayList<>();
        lastRoll = new int[numberOfDice];
        for(int i = 0; i < numberOfDice; i++){
            dice.add(new Die());
        }
    }

    public int[] rollAllDice(){
        int total = 0;
        lastRoll = new int[dice.size()];

        for(int i = 0; i < dice.size(); i++){
            lastRoll[i] = dice.get(i).roll();
            total += lastRoll[i];
        }
        return lastRoll;
    }

    public int Roll(){
        rollAllDice();
        return getTotal();
    }

    public int getTotal() {
        int total = 0;
        for (int val : lastRoll) {
            total += val;
        }
        return total;
    }

    public int getDie(int dieNumber){
        return dice.get(dieNumber).getValue();
    }

    public int[] getLastRoll() {
        return Arrays.copyOf(lastRoll, lastRoll.length);
    }

    public int getNumberOfDice(){
        return dice.size();
    }

    public void setNumberOfDice(int count) {
        if(count < 1) {
            throw new IllegalArgumentException("Must have at least 1 die");
        }

        // If increasing dice count
        while (dice.size() < count) {
            dice.add(new Die());
        }

        // If decreasing dice count
        while (dice.size() > count) {
            dice.remove(dice.size() - 1);
        }

        lastRoll = new int[count];
    }
}