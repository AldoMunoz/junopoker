package com.v1.junopoker.service;

import com.v1.junopoker.model.Card;
import com.v1.junopoker.model.Hand;
import com.v1.junopoker.model.Player;
import com.v1.junopoker.model.Table;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TableService {
    //adds a player to the players list, gives them a chip count and a specific seat
    public void addPlayer (Table table, Player player, int seat, int chipCount) {
        if (table.getSeats()[seat] == null) table.getSeats()[seat] = player;

        player.setChipCount(chipCount);
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() + 1);
    }

    //removes player at the giver seat
    public void removePlayer(Table table, int seat) {
        table.getSeats()[seat] = null;
    }

    //gathers and executes all the functions needed to run ring poker game
    public void runGame(Table table) {
        //starts the game and sets the blinds
        if (!table.isGameIsRunning()) {
            setBlinds(table);
            table.setGameIsRunning(true);
        }

        //game will run while there are at least 2 people seated at the table
        while (table.getSeatedPlayerCount() > 1) {
            moveBlinds(table);
            dealCards(table);
            initiatePot(table);
            //TODO preFlopBetting();
            dealFlop(table);
            getHandVals(table);
            //TODO postFlopBetting();
            dealTurnOrRiver(table);
            //TODO postFlopBetting();
            getHandVals(table);
            dealTurnOrRiver(table);
            //TODO postFlopBetting();
            getHandVals(table);
            //TODO completeHand();
            clearTable(table);
        }
    }

    //sets the BB and SB
    private void setBlinds (Table table) {
        for (int i = 0; i < table.getSeats().length; i++) {
            if(table.getSeats()[i] == null);
            else {
                if(table.getSmallBlind() == -1) table.setSmallBlind(i);
                else {
                    table.setBigBlind(i);
                    break;
                }
            }
        }
    }

    //moves the BB and SB to the next player
    private void moveBlinds(Table table) {
        //SB is set to person who was just BB
        table.setSmallBlind(table.getBigBlind());

        int seatCount = table.getSeatCount();
        //rotates clockwise using modulus until the next player is found, assigns them the BB
        for (int i = (table.getBigBlind()+1) % seatCount; i < seatCount; i = (i+1) % seatCount) {
            if (table.getSeats()[i] == null);
            else {
                table.setBigBlind(i);
                break;
            }
        }
    }

    //deals cards preflop to all players
    public void dealCards (Table table) {
        //the deck will be organized in a random order before the round starts
        DeckService deckService = new DeckService();
        deckService.shuffleCards(table.getDeck());

        //deals cards to every seat with an active player in it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                //creates an arraylist where the two dealt cards will be added
                Card[] cards = new Card[2];
                cards[0] = deckService.drawCard(table.getDeck());
                cards[1] = deckService.drawCard(table.getDeck());
                //the array is set as the player's hole cards
                table.getSeats()[i].setHoleCards(cards);
            }
        }
    }

    //deals the flop out
    public void dealFlop (Table table) {
        DeckService deckService = new DeckService();

        //adds 3 cards (flop) to the board
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));

        //iterates through players, gets their hand ranking, and sets it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                //creates new Hand and assigns it to the player
                Hand hand = new Hand(table.getSeats()[i].getHoleCards(), table.getBoard());
                table.getSeats()[i].setHand(hand);
                table.getSeats()[i].getHand().getHandRanking();
            }
        }
    }

    //deals the turn or river card
    public void dealTurnOrRiver (Table table) {
        DeckService deckService = new DeckService();

        table.getBoard().add(deckService.drawCard(table.getDeck()));
        getHandVals(table);
    }

    //collects blinds and adds them to the pot
    private void initiatePot (Table table) {
        Player[] seats = table.getSeats();
        int smallBlind = table.getSmallBlind();
        int bigBlind = table.getBigBlind();
        int stakes[] = table.getStakes();

        //BB collection
        if (seats[bigBlind].getChipCount() > stakes[1]) {
            table.setPot(table.getPot()+stakes[1]);
            seats[bigBlind].setChipCount(seats[bigBlind].getChipCount()-stakes[1]);
        }
        //edge case, if the players stack size is less than the blind
        else {
            table.setPot(table.getPot() + seats[bigBlind].getChipCount());
            seats[bigBlind].setChipCount(0);
        }

        //SB collection
        if (seats[smallBlind].getChipCount() > stakes[0]) {
            table.setPot(table.getPot()+stakes[0]);
            seats[smallBlind].setChipCount(seats[smallBlind].getChipCount()-stakes[0]);
        }
        //edge case, if the players stack size is less than the blind
        else {
            table.setPot(table.getPot() + seats[smallBlind].getChipCount());
            seats[smallBlind].setChipCount(0);
        }
    }

    //deals with the pre-flop betting rounds
    /*public void preFlopBetting (Table table) {
        //pre-flop betting starts at the player to the left of the big blind
        //TODO following code might pick an empty seat, fix
        int currPlayer = (bigBlind+1) % players.length;
        //following two values are used to calculate min bet size, which will always be lastBet-secLastBet
        int lastBet = stakes[1];
        int secLastBet = 0;

        boolean actionOver = false;
        while (actionOver == false) {
            if(players[currPlayer] == null) continue;
            //TODO complete betting round code
        }
    }*/

    //Goes through the list of players and reassigns Hand value after turn and river
    public void getHandVals(Table table) {
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                table.getSeats()[i].getHand().getHandRanking();
            }
        }
    }

    //Resets Player Hands, the Table board, and rejoins dead cards with to the deck
    public void clearTable(Table table) {
        DeckService deckService = new DeckService();

        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                table.getSeats()[i].setHand(null);
            }
        }
        table.setBoard(new ArrayList<Card>());
        deckService.joinDeck(table.getDeck());
    }
}
