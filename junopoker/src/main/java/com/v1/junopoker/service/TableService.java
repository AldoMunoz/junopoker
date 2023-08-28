package com.v1.junopoker.service;

import com.v1.junopoker.callback.TableCallback;
import com.v1.junopoker.dto.PlayerActionResponse;
import com.v1.junopoker.factory.DeckServiceFactory;
import com.v1.junopoker.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TableService {
    //Class that creates a new DeckService when needed (for decoupling purposes)
    private final DeckServiceFactory deckServiceFactory;
    //Interface for callback methods for all TableService methods that require user interaction,
    //Callbacks are sent to the controller, and passed on to the UI via WebSocket
    private TableCallback tableCallback;
    private CompletableFuture<PlayerActionResponse> playerActionResponse;

    @Autowired
    public TableService(DeckServiceFactory deckServiceFactory) {
        this.deckServiceFactory = deckServiceFactory;
    }

    public void setTableCallback(TableCallback tableCallback) {
        this.tableCallback = tableCallback;
    }


    //TODO is this necessary
    public CompletableFuture<PlayerActionResponse> getPlayerActionResponse() {
        return playerActionResponse;
    }

    public void handlePlayerAction(PlayerActionResponse playerActionResponse) {
        this.playerActionResponse.complete(playerActionResponse);
    }


    //adds a player to the players list, gives them a chip count and a specific seat
    public void addPlayer (Table table, Player player, int seat) {
        if (table.getSeats()[seat] == null) table.getSeats()[seat] = player;
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() + 1);
    }

    //removes player at the given seat
    public void removePlayer(Table table, int seat) {
        table.getSeats()[seat] = null;
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() - 1);
    }

    //gathers and executes all the functions needed to run ring poker game
    public void runGame(Table table) {
        //starts the game and sets the blinds
        if (!table.isGameIsRunning()) {
            setBlinds(table);
            table.setGameIsRunning(true);
        }

        moveBlinds(table);
        initiatePot(table);
        dealCards(table);
        preFlopBetting(table);
        //game will run while there are at least 2 people seated at the table
        /*while (table.getSeatedPlayerCount() > 1) {
            moveBlinds(table);
            initiatePot(table);
            dealCards(table);
            preFlopBetting(table);
            dealFlop(table);
            getHandVals(table);
            postFlopBetting(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            getHandVals(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            getHandVals(table);
            completeHand(table);
            clearTable(table);
        }*/
    }

    //sets the BB and SB
    public void setBlinds (Table table) {
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
    //sets or moves the button
    public void moveBlinds(Table table) {
        //SB is set to person who was just BB
        table.setSmallBlind(table.getBigBlind());

        int seatCount = table.getSeatCount();
        boolean bigBlindSet = false;
        //rotates clockwise using modulus until the next player is found, assigns them the BB
        for (int i = (table.getBigBlind()+1) % seatCount; i < seatCount; i = (i+1) % seatCount) {
            if (table.getSeats()[i] == null);
            //first player found after big blind = new big blind
            else if(!bigBlindSet){
                table.setBigBlind(i);
                bigBlindSet = true;
            }
            //next player found after big blind = button
            else {
                table.setDealerButton(i);
                invokeButtonCallback(table.getDealerButton());
                break;
            }
        }
    }
    //callback function used to send position info to the front-end
    private void invokeButtonCallback(int buttonIndex) {
        if(tableCallback != null) {
            tableCallback.onButtonSet(buttonIndex);
        }
    }

    //collects blinds and adds them to the pot
    public void initiatePot (Table table) {
        Player[] seats = table.getSeats();
        int smallBlind = table.getSmallBlind();
        int bigBlind = table.getBigBlind();
        int[] stakes = table.getStakes();

        float sbAmount = stakes[0];
        float bbAmount = stakes[1];

        //BB collection
        if (seats[bigBlind].getChipCount() > stakes[1]) {
            table.setPot(table.getPot()+stakes[1]);
            seats[bigBlind].setChipCount(seats[bigBlind].getChipCount()-stakes[1]);
        }
        //edge case, if the players stack size is less than the blind
        else {
            bbAmount = seats[bigBlind].getChipCount();
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
            sbAmount = seats[smallBlind].getChipCount();
            table.setPot(table.getPot() + seats[smallBlind].getChipCount());
            seats[smallBlind].setChipCount(0);
        }

        invokeInitPotCallback(table.getSmallBlind(), table.getBigBlind(), sbAmount, bbAmount, table.getPot());
    }
    private void invokeInitPotCallback(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize) {
        if(tableCallback != null) {
            tableCallback.onPotInit(sbIndex, bbIndex, sbAmount, bbAmount, potSize);
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
                table.getSeats()[i].setInHand(true);

                invokeDealHoleCardsCallback(table.getSeats()[i].getUsername(), i, cards);
            }
        }
    }

    private void invokeDealHoleCardsCallback(String username, int seat, Card[] holeCards) {
        if(tableCallback != null) {
            tableCallback.onHoleCardsDealt(username, seat, holeCards);
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
        DeckService deckService = deckServiceFactory.createDeckService();

        table.getBoard().add(deckService.drawCard(table.getDeck()));
        getHandVals(table);
    }

    //deals with the pre-flop betting rounds
    public void preFlopBetting (Table table) {
        //creating local fields for SB, BB, stakes, seats
        int smallBlind = table.getSmallBlind();
        int bigBlind = table.getBigBlind();
        int[] stakes = table.getStakes();
        Player[] seats = table.getSeats();
        //set current bet for player in SB and BB position
        table.getSeats()[smallBlind].setCurrentBet(stakes[0]);
        table.getSeats()[bigBlind].setCurrentBet(stakes[1]);


        //find the first player to act
        int currPlayerIndex = (bigBlind + 1) % table.getSeatCount();
        while (seats[currPlayerIndex] == null) {
            currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
        }

        //set the current table bet to the big blind
        table.setCurrentBet(stakes[1]);

        //handle betting round actions from players
        playerAction(table, currPlayerIndex, true);

        //switch sb and bb if game is heads up
        if (table.getSeatedPlayerCount() == 2) {
            int temp = bigBlind;
            table.setBigBlind(table.getSmallBlind());
            table.setSmallBlind(temp);
        }
    }
    public void postFlopBetting(Table table) {
        //set fields
        int smallBlind = table.getSmallBlind();
        Player[] seats = table.getSeats();

        //Identify who will be first to act
        int currPlayerIndex = smallBlind;
        while (seats[currPlayerIndex] == null) {
            currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
        }

        //TODO: make separate cleanUp method
        //clean up table currentBet and player current bets
        table.setCurrentBet(0);
        int previousBet = 0;
        for (Player player : seats) {
            if (player != null) player.setCurrentBet(0);
        }

        //handle betting round actions from players
        playerAction(table, currPlayerIndex, false);
    }

    //handles player actions during the betting round until the betting round is over
    public void playerAction(Table table, int currPlayerIndex, boolean isPreflop) {
        int firstToActIndex = currPlayerIndex;
        boolean actionOver = false;
        boolean firstAction = true;
        int seatedPlayerCount = table.getSeatedPlayerCount();
        int[] stakes = table.getStakes();
        float previousBet = 0;

        //loop will continue until all action for the round is over
        while (!actionOver) {
            //TODO: should have a field called handOver on the Table object
            //TODO: in runGame(), everytime a betting round is over, check HandOver, if true, skip to complete hand
            //if everyone has folded (except for 1 player), the whole hand is over, skip to complete hand
            if (table.getSeatedFoldCount() == seatedPlayerCount - 1) {
                completeHand(table);
                break;
            }
            // skips any empty seats or folded players
            if (table.getSeats()[currPlayerIndex] == null || !table.getSeats()[currPlayerIndex].isInHand()) {
                currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
            }
            //PRE-FLOP
            //if we've looped back to the original raiser and player CurrentBet = table CurrentBet, action is over
            else if (isPreflop && table.getSeats()[currPlayerIndex].getCurrentBet() == table.getCurrentBet() && table.getCurrentBet() > stakes[1])
                actionOver = true;
                //PRE-FLOP
                //if we're at the big blind and the table currentBet = the big blind, it is a limped pot, action is over
            else if (isPreflop && table.getSeats()[currPlayerIndex].getCurrentBet() == stakes[1] && currPlayerIndex != table.getBigBlind())
                actionOver = true;
                //POST-FLOP
                //if we've looped back to original raiser and player currentBet = table currentBet, action is over
            else if (!isPreflop && table.getSeats()[currPlayerIndex].getCurrentBet() == table.getCurrentBet() && table.getCurrentBet() > 0)
                actionOver = true;
                //POST-FLOP
                //if we've checked around to the firstToAct player, action is over
            else if (!isPreflop && currPlayerIndex == firstToActIndex && table.getCurrentBet() == 0 && !firstAction)
                actionOver = true;
                //else take an action input from the player
            else {
                //callback method for preflop action callback
                //returns an int (bet size)
                invokePreFlopActionCallback(table.getSeats()[currPlayerIndex], currPlayerIndex, table.getCurrentBet());

                CompletableFuture<PlayerActionResponse> future = new CompletableFuture<>();
                playerActionResponse = future;

                try {
                    PlayerActionResponse playerActionResponse = future.get();
                    char action = playerActionResponse.getAction();
                    float bet = playerActionResponse.getBetAmount();
                    float minBet = (table.getCurrentBet() - previousBet) + table.getCurrentBet();

                    //if they fold
                    if (action == 'F' && bet == 0) {
                        table.setSeatedFoldCount(table.getSeatedFoldCount()+1);
                        table.getSeats()[currPlayerIndex].setInHand(false);
                    }
                    //if they check
                    else if(action == 'C' && bet == 0) {
                        continue;
                    }
                    //if they bet:
                    else if (action == 'B' && bet >= minBet) {
                        //update player chip count
                        table.getSeats()[currPlayerIndex].setChipCount
                                (table.getSeats()[currPlayerIndex].getChipCount() + table.getSeats()[currPlayerIndex].getCurrentBet() - bet);
                        //update the pot size of the table
                        table.setPot
                                (table.getPot() + (bet - table.getSeats()[currPlayerIndex].getCurrentBet()));
                        //update the player's current bet
                        table.getSeats()[currPlayerIndex].setCurrentBet(bet);

                        //check if the bet amount is greater than currentBet
                        if(bet > table.getCurrentBet()) {
                            //if it is, set the current bet to previous bet and current bet to the player's bet
                            previousBet = table.getCurrentBet();
                            table.setCurrentBet(bet);
                            minBet = (table.getCurrentBet() - previousBet) + table.getCurrentBet();
                        }
                    }
                    else {
                        System.out.println("wrong action or bet input");
                    }

                    //loops over seats to get the next player to act
                    currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
                    //loops until a non-empty seat with non-folded player
                    //TODO second part of while might be wrong, have to test
                    while (table.getSeats()[currPlayerIndex] == null || !(table.getSeats()[currPlayerIndex].isInHand())) {
                        currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
                    }
                    //first action has now taken place, set to false
                    firstAction = false;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void invokePreFlopActionCallback(Player player, int seat, float currentBet) {
        if(tableCallback != null) {
            tableCallback.onPreFlopAction(player, seat, currentBet);
        }
    }


    //TODO: handle cases for when the hand completes before showdown
    //TODO: example, everyone folds pre flop, and as such player never gets assigned a handRanking.
    public void completeHand(Table table) {
        // Initializing the best made hand rank
        HandRanking max_rank = null;
        Player[] players = table.getSeats();
        for (Player player : players) {
            if (player == null || !player.isInHand()) { continue;
            }
            else if (max_rank == null) max_rank = player.getHand().getHandRanking();
            else if (player.getHand().getHandRanking().getRanking() > max_rank.getRanking())
                max_rank = player.getHand().getHandRanking();
        }
        // Appending all players with hand rank equal to the max rank
        ArrayList<Player> potential_winners = new ArrayList();
        for (Player player : players) {
            if (player != null && player.isInHand() &&
                    player.getHand().getHandRanking() == max_rank) potential_winners.add(player);
        }
        // Initialize winners and find winner(s) within potential_winners array list
        ArrayList<Player> winners = new ArrayList<>();
        for (Player player : potential_winners) {
            if (winners.size() == 0) {winners.add(player); continue;}
            int hand_pos = 0;
            boolean decided = false;
            // Comparing cards at index hand_pos within the current player in potential_winners
            // and the first player in winners
            while (hand_pos < 5 && !decided) {
                int currWinnerVal = winners.get(0).getHand().getFiveCardHand()[hand_pos].getVal();
                int currPlayerVal = player.getHand().getFiveCardHand()[hand_pos].getVal();
                if (currWinnerVal > currPlayerVal) decided = true;
                else if (currWinnerVal == currPlayerVal) hand_pos ++;
                else {
                    winners.clear();
                    decided = true;
                }
            }
            // decided is true in two cases: we have found a better hand or a worse hand.
            // Winners array list is updated to just contain the current player in potential_winners
            // if their hand is better. Otherwise, we continue to
            // the next iteration since a player with a worse hand will not be a winner.
            if (decided && winners.size() == 0) {
                winners.add(player);
            }
            // if we iterated through the whole hand and decided is false, then we know the current
            // player in potential_winners has an equal strength hand to the current winner(s), so
            // that player is added.
            else if (hand_pos == 5) winners.add(player);
        }
        // Chip stacks of players in winners are updated to contain pot/winners.size()
        // additional chips.
        for (Player winner : winners) {
            winner.setChipCount(winner.getChipCount() + (table.getPot() / winners.size()));
        }
    }
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

    public Player getPlayerAtSeat(Table table, int seat) {
        return table.getSeats()[seat];
    }
}
