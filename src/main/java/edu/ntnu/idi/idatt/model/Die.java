package edu.ntnu.idi.idatt.model;
import java.util.Random;

public class Die {

    private Random random;
    private int lastRolledValue;

    public Die(){
        random = new Random();
        roll();
    }

    public int roll(){
        lastRolledValue = random.nextInt(6) + 1;
        return lastRolledValue;
    }
    public int getValue(){
        return lastRolledValue;
    }

}
