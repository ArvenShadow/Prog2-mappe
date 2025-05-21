package edu.ntnu.idi.idatt.model;

import java.util.ArrayList;

public class Dice {

    private ArrayList<Die> dice;

    public Dice(int numberOfDice){
        if(numberOfDice < 1){
            throw new IllegalArgumentException("You must have at least 1 die");
        }
        dice = new ArrayList<>();
        for(int i = 0; i < numberOfDice; i++){
            dice.add(new Die());
        }
    }

    public int Roll(){
        int total = 0;

        for(Die die : dice){
            total += die.roll();
        }
        return total;
    }

    public int getDie(int dieNumber){
        return dice.get(dieNumber).getValue();
    }

    public int getNumberOfDice(){
        return dice.size();
    }
}
