package edu.ntnu.idi.idatt.action;

import edu.ntnu.idi.idatt.model.Player;

public class SkipTurnAction implements TileAction {

    @Override
    public void perform(Player player) {
        player.setSkipsNextTurn(true);
        System.out.println(player.getName() + " have to skip their next turn");

    }
}
