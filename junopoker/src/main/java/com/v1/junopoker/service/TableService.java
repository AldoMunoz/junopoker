package com.v1.junopoker.service;

import com.v1.junopoker.factory.DeckServiceFactory;
import com.v1.junopoker.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Scanner;

@Service
public class TableService {
    private final DeckServiceFactory deckServiceFactory;
    @Autowired
    public TableService(DeckServiceFactory deckServiceFactory) {
        this.deckServiceFactory = deckServiceFactory;
    }


    //adds a player to the "seats" array, increments playerCount by 1
    public void addPlayer (Table table, Player player, int seat) {
        if (table.getSeats()[seat] == null) table.getSeats()[seat] = player;
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() + 1);
        //if two people are seated, start the game
        //if(table.getSeatedPlayerCount() > 1) runGame(table);
    }

    //removes player at the given seat, decrements playerCount by 1
    public void removePlayer(Table table, int seat) {
        table.getSeats()[seat] = null;
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() - 1);
    }

    //Executes methods needed to run ring poker game
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
            preFlopBetting(table);
            //clearBets(table);
            dealFlop(table);
            getHandVals(table);
            postFlopBetting(table);
            //clearBets(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            //clearBets(table);
            getHandVals(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            //clearBets(table);
            getHandVals(table);
            completeHand(table);
            clearTable(table);
        }
        //if there are less than 2 people at the table, stop the game
        table.setGameIsRunning(false);
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
        DeckService deckService = deckServiceFactory.createDeckService();
        deckService.shuffleCards(table.getDeck());

        //deals cards to every seat with an active player in it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != null) {
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
        DeckService deckService = deckServiceFactory.createDeckService();

        //adds 3 cards (flop) to the board
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));

        //iterates through players, gets their hand ranking, and sets it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != null) {
                //creates new Hand and assigns it to the player
                Hand hand = new Hand(table.getSeats()[i].getHoleCards(), table.getBoard());
                table.getSeats()[i].setHand(hand);
                table.getSeats()[i].getHand().getHandRanking();
            }
        }
    }

    //deals the turn or river card
    public void dealTurnOrRiver (Table table) {
        DeckService deckService = deckServiceFactory.createDeckService();

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

    //deals with the pre-flop betting round
    public void preFlopBetting(Table table) {
        // pre-flop betting starts at the player to the left of the big blind
        table.getSeats()[table.getSmallBlind()].setCurrentBet(table.getStakes()[0]);
        table.getSeats()[table.getBigBlind()].setCurrentBet(table.getStakes()[1]);
        // Initializes currPlayer index to be one more than the big blind, wrapping around players.length and
        // skipping any nulls
        int currPlayer = (table.getBigBlind() + 1) % table.getSeatCount();
        while (table.getSeats()[currPlayer] == null) {
            currPlayer = (currPlayer + 1) % table.getSeatCount();
        }
        table.setCurrentBet(table.getStakes()[1]);
        boolean actionOver = false;
        while (!actionOver) {
            // skips any empty seats or folded players
            if(table.getSeats()[currPlayer] == null || !table.getSeats()[currPlayer].isInHand()) {
                currPlayer = (currPlayer + 1) % table.getSeatCount();
            }
            // moves on to the flop if any of these conditions are met
            else if (table.getSeats()[currPlayer].getCurrentBet() == table.getCurrentBet() && table.getCurrentBet() > table.getStakes()[1])
                actionOver = true;
            else if (table.getSeats()[currPlayer].getCurrentBet() == table.getStakes()[1] && currPlayer != table.getBigBlind())
                actionOver = true;
            else {
                System.out.println("current bet is: " + table.getCurrentBet());
                System.out.println("your bet: ");
                Scanner sc = new Scanner(System.in);
                int bet = sc.nextInt();
                // if block handling cases where a bet is less than a min-raise. Assumes a bet of 0 is a fold and a bet
                // equaling the current bet is a call
                if (bet < 2 * table.getCurrentBet()) {
                    if (currPlayer == table.getBigBlind() && table.getCurrentBet() == table.getStakes()[1] && bet == 0) {
                        currPlayer = (currPlayer + 1) % table.getSeatCount();
                        continue;
                    } else if (table.getSeats()[currPlayer].getChipCount() < table.getCurrentBet()) {
                        bet = table.getSeats()[currPlayer].getChipCount();
                    } else if (bet > table.getCurrentBet()) {
                        bet = 2 * table.getCurrentBet();
                    } else if (bet != 0) {
                        bet = table.getCurrentBet();
                    } else {
                        table.getSeats()[currPlayer].setInHand(false);
                        currPlayer = (currPlayer + 1) % table.getSeatCount();
                        continue;
                    }
                }
                // handles cases where a bet is at least a min-raise
                table.getSeats()[currPlayer].setChipCount(
                        table.getSeats()[currPlayer].getChipCount() - (bet - table.getSeats()[currPlayer].getCurrentBet()));
                table.setPot(table.getPot() + (bet - table.getSeats()[currPlayer].getCurrentBet()));
                table.getSeats()[currPlayer].setCurrentBet(bet);
                table.setCurrentBet(bet);
                currPlayer = (currPlayer + 1) % table.getSeatCount();
            }
        }
    }

    //deals with the post-flop betting rounds
    public void postFlopBetting(Table table) {
        int button = (table.getSmallBlind() - 1) % table.getSeatCount();
        // Initializes currPlayer index to be the small blind, wrapping around players.length and
        // skipping any nulls
        int currPlayer = table.getSmallBlind();
        while (table.getSeats()[currPlayer] == null) {
            currPlayer = (currPlayer + 1) % table.getSeatCount();
        }


        //TODO -- This clean up should be a seperate method, clearBets();
        //TODO -- should be called after all betting rounds
        table.setCurrentBet(0);
        for (Player player : table.getSeats()) {
            if (player != null) player.setCurrentBet(0);
        }

        int firstToAct = currPlayer;
        boolean actionOver = false;
        boolean firstAction = true;
        while (!actionOver) {
            // skips any empty seats or folded players
            if (table.getSeats()[currPlayer] == null || !table.getSeats()[currPlayer].isInHand()) {
                currPlayer = (currPlayer + 1) % table.getSeatCount();
                continue;
            }
            // moves on to the next street if any of these conditions are met
            if (table.getSeats()[currPlayer].getCurrentBet() == table.getCurrentBet() && table.getCurrentBet() > 0)
                actionOver = true;
            else if (currPlayer == firstToAct && table.getCurrentBet() == 0 && !firstAction)
                actionOver = true;
            else {
                System.out.println("current bet is: " + table.getCurrentBet());
                System.out.println("your bet: ");
                Scanner sc = new Scanner(System.in);
                int bet = sc.nextInt();
                // if block handling cases where a bet is less than a min-raise. Assumes a bet of 0 is a fold and a bet
                // equaling the current bet is a call
                if (bet < 2 * table.getCurrentBet()) {
                    if (currPlayer == table.getBigBlind() && table.getCurrentBet() == table.getStakes()[1] && bet == 0) {
                        currPlayer = (currPlayer + 1) % table.getSeatCount();
                        continue;
                    } else if (table.getSeats()[currPlayer].getChipCount() < table.getCurrentBet()) {
                        bet = table.getSeats()[currPlayer].getChipCount();
                    } else if (bet > table.getCurrentBet()) {
                        bet = 2 * table.getCurrentBet();
                    } else if (bet != 0) {
                        bet = table.getCurrentBet();
                    } else {table.getSeats()[currPlayer].setInHand(false);
                        currPlayer = (currPlayer + 1) % table.getSeatCount();
                        continue;
                    }
                }
                // handles cases where a bet is at least a min-raise
                table.getSeats()[currPlayer].setChipCount(
                        table.getSeats()[currPlayer].getChipCount() - (bet - table.getSeats()[currPlayer].getCurrentBet()));
                table.setPot(table.getPot() + (bet - table.getSeats()[currPlayer].getCurrentBet()));
                table.getSeats()[currPlayer].setCurrentBet(bet);
                table.setCurrentBet(bet);

                currPlayer = (currPlayer + 1) % table.getSeatCount();
            }
        }
    }
    //finds the Player(s) with the winning hand(s) and awards them the pot
    //TODO still checking for straight and straight flushes, did you not push?
    public void completeHand(Table table) {
        // Initializing the best made hand rank
        HandRanking max_rank = null;
        for (Player player : table.getSeats()) {
            if (player == null) continue;
            if (max_rank == null) max_rank = player.getHand().getHandRanking();
            else if (player.getHand().getHandRanking().getRanking() > max_rank.getRanking())
                max_rank = player.getHand().getHandRanking();
        }
        // Appending all players with hand rank equal to the max rank
        ArrayList<Player> potential_winners = new ArrayList();
        for (Player player : table.getSeats()) {
            if (player.getHand().getHandRanking() == max_rank) potential_winners.add(player);
        }
        /*
         * Algorithm for determining the winners of the pot and awarding winnings:
         *   - Iterate through all players in the potential_winners array list.
         *   - Initialize winners to contain the first player in potential_winners if it's empty, then continue.
         *   - If the max_rank is a straight or straight-flush, we compare only the second cards in the hands of the curr
         * player in potential_winners and the first player in winners. This still works because for any straight or
         * straight-flush, the second card in the hand can be used to determine the strength of it. This takes care of
         * the case where one player has a wheel straight or straight flush and a different player has a better straight
         * but with a lower first card.
         *   - Otherwise, we will potentially compare every card in the hands of the first player in winners and the
         * current player in potential_winners. If the first player in winners has a greater card value, we move on to
         * the next player in potential_winners. If all the cards are equal value, we add the player to winners. Else we
         * clear the winners array list and initialize it with the current player in potential_winners.
         *   - After iterating through potential winners, we add pot / size(winners) to each player's stack in winners.
         *  NOTE: This ignores potential side pots and assumes the pot is evenly divisible by the number of winners,
         * which is often not the case.
         */
        ArrayList<Player> winners = new ArrayList<>();
        for (Player player : potential_winners) {
            boolean isStraightOrStraightFlush = max_rank == HandRanking.STRAIGHT
                    || max_rank == HandRanking.STRAIGHT_FLUSH;
            if (winners.size() == 0) {winners.add(player); continue;}
            int hand_pos = 0;
            boolean decided = false;
            while (hand_pos < 5 && !decided) {
                int currWinnerVal = winners.get(0).getHand().getFiveCardHand()[hand_pos].getVal();
                int currPlayerVal = player.getHand().getFiveCardHand()[hand_pos].getVal();
                if (isStraightOrStraightFlush) {
                    if (hand_pos == 0) {
                        currWinnerVal = winners.get(0).getHand().getFiveCardHand()[1].getVal();
                        currPlayerVal = player.getHand().getFiveCardHand()[1].getVal();
                        hand_pos = 1;
                    }
                }
                if (currWinnerVal > currPlayerVal) decided = true;
                else if (currWinnerVal == currPlayerVal) {
                    if (isStraightOrStraightFlush) {
                        hand_pos = 5;
                        break;
                    }
                    hand_pos ++;
                }
                else {
                    winners.clear();
                    decided = true;
                }
            }
            if (decided) {
                if (winners.size() == 0) winners.add(player);
            } else if (hand_pos == 5) winners.add(player);
        }
        for (Player winner : winners) {
            winner.setChipCount(winner.getChipCount() + (table.getPot() / winners.size()));
        }
    }

    //Goes through the list of players and reassigns Hand value after turn and river
    public void getHandVals(Table table) {
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != null) {
                table.getSeats()[i].getHand().getHandRanking();
            }
        }
    }

    //Resets Player Hands, the Table board, and rejoins dead cards with to the deck
    public void clearTable(Table table) {
        DeckService deckService = deckServiceFactory.createDeckService();

        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                table.getSeats()[i].setHand(null);
            }
        }
        table.setBoard(new ArrayList<Card>());
        deckService.joinDeck(table.getDeck());
    }

    public Player getPlayerAtSeat(Table table, int seat) {
        return table.getSeats()[seat];
    }
}
