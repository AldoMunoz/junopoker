package com.v1.junopoker.service;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Hand;
import com.v1.junopoker.model.Player;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    public void setHoleCards (Player player, Card[] cards) { player.setHoleCards(cards);
    }

    public void clearHoleCards (Player player) {
        player.setHoleCards(null);
    }

    public Card[] getHoleCards (Player player) {
        return player.getHoleCards();
    }

    public Hand getHand(Player player) {
        return player.getHand();
    }

    public void setHand(Player player, Hand hand) {
        player.setHand(hand);
    }

    public int getChipCount(Player player) {
        return player.getChipCount();
    }

    public void setChipCount(Player player, int chipCount) {
        player.setChipCount(chipCount);
    }
    public void setCurrentBet(Player player, int bet) {
        player.setCurrentBet(bet);
    }
    public int getCurrentBet(Player player) {
        return player.getCurrentBet();
    }

    public void setIsActive (Player player, boolean isActive) {
        player.setActive(isActive);
    }

    public void setInHand (Player player, boolean inHand) {
        player.setInHand(inHand);
    }

    public boolean getInHand(Player player) {
        return player.getInHand();
    }
    public boolean getIsActive(Player player) {
        return player.getIsActive();
    }
}
