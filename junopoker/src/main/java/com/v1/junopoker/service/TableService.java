package com.v1.junopoker.service;

import com.v1.junopoker.callback.TableCallback;
import com.v1.junopoker.dto.PlayerActionResponse;
import com.v1.junopoker.factory.DeckServiceFactory;
import com.v1.junopoker.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
        if (!table.isGameRunning()) {
            setBlinds(table);
            table.setGameRunning(true);
        }

        //game will run while there are at least 2 people seated at the table
        while (table.getSeatedPlayerCount() > 1) {
            sleepTimer(4000);

            table.setHandOver(false);
            moveBlinds(table);
            trackStartingStacks(table);
            initiatePot(table);
            dealCards(table);

            preFlopBetting(table);
            //if action is complete
            //showdown, runout, complete hand, clear table, continue
            if(table.isActionComplete()) {
                showdown(table);
                dealRunout(table);
                completeHand(table);
                clearTable(table);
                continue;
            }
            else if(table.isHandOver()) {
                completeHand(table);
                clearTable(table);
                continue;
            }

            cleanUp(table);
            dealFlop(table);
            postFlopBetting(table);
            if(table.isActionComplete()) {
                showdown(table);
                dealRunout(table);
                completeHand(table);
                clearTable(table);
                continue;
            }
            else if(table.isHandOver()) {
                completeHand(table);
                clearTable(table);
                continue;
            }

            cleanUp(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            if(table.isActionComplete()) {
                showdown(table);
                dealRunout(table);
                completeHand(table);
                clearTable(table);
                continue;
            }
            else if(table.isHandOver()) {
                completeHand(table);
                clearTable(table);
                continue;
            }

            cleanUp(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            showdown(table);
            completeHand(table);
            clearTable(table);
        }
    }

    //sets the BB and SB
    public void setBlinds (Table table) {
        for (int i = 0; i < table.getSeats().length; i++) {
            if(table.getSeats()[i] == null);
            else {
                if(table.getSmallBlindIndex() == -1) table.setSmallBlindIndex(i);
                else {
                    table.setBigBlindIndex(i);
                    break;
                }
            }
        }
    }

    //moves the BB and SB to the next player
    //sets or moves the button
    private void moveBlinds(Table table) {
        //SB is set to person who was just BB
        table.setSmallBlindIndex(table.getBigBlindIndex());

        int seatCount = table.getSeatCount();
        boolean bigBlindSet = false;
        //rotates clockwise using modulus until the next player is found, assigns them the BB
        for (int i = (table.getBigBlindIndex()+1) % seatCount; i < seatCount; i = (i+1) % seatCount) {
            if (table.getSeats()[i] == null);
            //first player found after big blind = new big blind
            else if(!bigBlindSet){
                table.setBigBlindIndex(i);
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

    //tracks each players starting stack at the beginning of each hand (used for side pots and all-in tracking)
    private void trackStartingStacks(Table table) {
        for (int i = 0; i < table.getSeatCount(); i++) {
            if(table.getSeats()[i] != null) {
                table.getSeats()[i].setStartingStackThisHand(table.getSeats()[i].getChipCount());
            }
        }
    }

    //collects blinds and adds them to the pot
    private void initiatePot (Table table) {
        Player[] seats = table.getSeats();
        int smallBlindIndex = table.getSmallBlindIndex();
        int bigBlindIndex = table.getBigBlindIndex();
        BigDecimal[] stakes = table.getStakes();

        BigDecimal sbAmount = stakes[0];
        BigDecimal bbAmount = stakes[1];

        //BB collection
        if (seats[bigBlindIndex].getChipCount().compareTo(stakes[1]) > 0) {
            //add big blind to the pot
            table.setPot(table.getPot().add(stakes[1]));

            //deduct big blind from the player's stack
            seats[bigBlindIndex].setChipCount((seats[bigBlindIndex].getChipCount().subtract(stakes[1])).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        //edge case, if the players stack size is less than the blind
        else {
            bbAmount = seats[bigBlindIndex].getChipCount();

            table.setPot((table.getPot().add(seats[bigBlindIndex].getChipCount())).setScale(2, BigDecimal.ROUND_HALF_UP));

            seats[bigBlindIndex].setChipCount(BigDecimal.valueOf(0));
        }

        //SB collection
        if (seats[smallBlindIndex].getChipCount().compareTo(stakes[0]) > 0) {
            //add small blind to the pot
            table.setPot((table.getPot().add(stakes[0])).setScale(2, BigDecimal.ROUND_HALF_UP));

            //deduct small blind from the player's stack
            seats[smallBlindIndex].setChipCount((seats[smallBlindIndex].getChipCount().subtract(stakes[0])).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        //edge case, if the players stack size is less than the blind
        else {
            sbAmount = seats[smallBlindIndex].getChipCount();

            table.setPot((table.getPot().add(seats[smallBlindIndex].getChipCount())).setScale(2, BigDecimal.ROUND_HALF_UP));

            seats[smallBlindIndex].setChipCount(BigDecimal.valueOf(0));
        }

        invokeInitPotCallback(table.getSmallBlindIndex(), table.getBigBlindIndex(), sbAmount, bbAmount, table.getPot());
    }

    //deals cards preflop to all players
    private void dealCards (Table table) {
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

    //deals the flop out
    private void dealFlop (Table table) {
        DeckService deckService = new DeckService();

        //add 3 cards to the board
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));

        //assign hand ranking for each player and send hand rankings to the controller
        getHandRankings(table);

        //callback method to send the flop to the controller
        invokeDealBoardCardsCallback(table.getBoard());
    }

    //deals the turn or river card
    private void dealTurnOrRiver (Table table) {
        DeckService deckService = deckServiceFactory.createDeckService();

        //TODO this might be too memory intensive, might be better to create separate callback method for a turn/river card
        //draws a card and adds it to an array (makes it compatible with the callback method)
        Card card = deckService.drawCard(table.getDeck());
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(card);

        //adds 1 card to the board
        table.getBoard().add(card);

        //assign hand ranking for each player and send hand rankings to the controller
        getHandRankings(table);

        //callback method to send the card to the controller in the form or an ArrayList of length 1
        invokeDealBoardCardsCallback(cards);
    }

    //deals with the pre-flop betting rounds
    private void preFlopBetting (Table table) {
        //creating local fields for SB, BB, stakes, seats
        int smallBlind = table.getSmallBlindIndex();
        int bigBlind = table.getBigBlindIndex();
        BigDecimal[] stakes = table.getStakes();
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
            table.setBigBlindIndex(table.getSmallBlindIndex());
            table.setSmallBlindIndex(temp);
        }
    }
    private void postFlopBetting(Table table) {
        //set fields
        int smallBlind = table.getSmallBlindIndex();
        Player[] seats = table.getSeats();

        table.setCurrentStreetPot(BigDecimal.valueOf(0));

        //Identify who will be first to act
        int currPlayerIndex = smallBlind;
        while (seats[currPlayerIndex] == null) {
            currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
        }

        //handle betting round actions from players
        playerAction(table, currPlayerIndex, false);
    }

    //handles player actions during the betting round until the betting round is over
    private void playerAction(Table table, int currPlayerIndex, boolean isPreFlop) {
        int firstToActIndex = currPlayerIndex;
        boolean actionOver = false;
        boolean firstAction = true;
        int seatedPlayerCount = table.getSeatedPlayerCount();
        BigDecimal[] stakes = table.getStakes();
        BigDecimal previousBet = BigDecimal.valueOf(0);

        BigDecimal minBet;
        //TODO math might be wrong
        if(isPreFlop) minBet = table.getCurrentBet().subtract(previousBet).add(table.getCurrentBet());
        else minBet = table.getStakes()[1];

        //loop will continue until all action for the round is over
        while (!actionOver) {
            //if everyone has folded (except for 1 player), the whole hand is over, skip to complete hand
            if (table.getSeatedFoldCount() == seatedPlayerCount - 1) {
                table.setHandOver(true);
                break;
            }
            //PRE-FLOP
            //if we've looped back to the original raiser and player currentBet = table CurrentBet, action is over
            else if (isPreFlop &&
                    table.getSeats()[currPlayerIndex].getCurrentBet() == table.getCurrentBet() &&
                    table.getCurrentBet().compareTo(stakes[1]) > 0)
                actionOver = true;

            //PRE-FLOP
            //if we're not at the big blind and the table currentBet = the big blind, it is a limped pot, action is over
            else if (isPreFlop &&
                    table.getSeats()[currPlayerIndex].getCurrentBet() == stakes[1] &&
                    table.getCurrentBet() == stakes[1] &&
                    currPlayerIndex != table.getBigBlindIndex())
                actionOver = true;

            //POST-FLOP
            //if we've looped back to original raiser and player currentBet = table currentBet, action is over
            else if (!isPreFlop &&
                    table.getSeats()[currPlayerIndex].getCurrentBet() == table.getCurrentBet() &&
                    table.getCurrentBet().compareTo(BigDecimal.valueOf(0)) > 0)
                actionOver = true;

            //POST-FLOP
            //if we've checked around to the firstToAct player, action is over
            else if (!isPreFlop &&
                    currPlayerIndex == firstToActIndex &&
                    table.getCurrentBet().compareTo(BigDecimal.valueOf(0)) == 0 &&
                    !firstAction)
                actionOver = true;

            //else take an action input from the player
            else {
                //callback method for preflop action callback
                //returns an int (bet size)
                invokePreFlopActionCallback(table.getSeats()[currPlayerIndex], currPlayerIndex, table.getCurrentBet(), table.getPot(), minBet);

                CompletableFuture<PlayerActionResponse> future = new CompletableFuture<>();
                playerActionResponse = future;

                try {
                    PlayerActionResponse playerActionResponse = future.get();
                    char action = playerActionResponse.getAction();
                    BigDecimal bet = playerActionResponse.getBetAmount();

                    //if they fold:
                    if (action == 'F') {
                        table.setSeatedFoldCount(table.getSeatedFoldCount()+1);
                        table.getSeats()[currPlayerIndex].setInHand(false);

                        invokeEndPlayerActionCallback('F', table.getSeats()[currPlayerIndex].getUsername(), currPlayerIndex, null, null, null, null, isPreFlop);
                    }
                    //if they check:
                    else if(action == 'C') {
                        invokeEndPlayerActionCallback('C', table.getSeats()[currPlayerIndex].getUsername(), currPlayerIndex, null, null, null, null, isPreFlop);
                    }
                    //if they are all-in
                    else if(action == 'A') {
                        //update and track player fields
                        table.getSeats()[currPlayerIndex].setChipCount(BigDecimal.valueOf(0));
                        //TODO math might be wrong
                        table.getSeats()[currPlayerIndex].setAmountBetThisHand(table.getSeats()[currPlayerIndex].getAmountBetThisHand().add(
                                (bet.subtract(table.getSeats()[currPlayerIndex].getCurrentBet()))));
                        table.getSeats()[currPlayerIndex].setAllIn(true);

                        //update the pot size of the table
                        table.setPot((table.getPot().add(bet)).setScale(2, BigDecimal.ROUND_HALF_UP));

                        //sets the player's current bet to the bet they just made
                        table.getSeats()[currPlayerIndex].setCurrentBet(bet);

                        //update the pot size of the current street
                        table.setCurrentStreetPot((table.getCurrentStreetPot().add(bet.subtract(table.getSeats()[currPlayerIndex].getCurrentBet()))).setScale(2, BigDecimal.ROUND_HALF_UP));

                        //check if the bet amount is greater than currentBet
                        if(bet.compareTo(table.getCurrentBet()) > 0) {
                            //if it is, set the current bet to previous bet and current bet to the player's bet
                            previousBet = table.getCurrentBet();
                            table.setCurrentBet(bet);
                            minBet = (table.getCurrentBet().subtract(previousBet)).add(table.getCurrentBet());
                        }

                        //increment the tables all-in count by 1, mark the player as all-in
                        table.setAllInCount(table.getAllInCount()+1);

                        //sends message to the controller to update the user's stack size, bet display, and the pot size
                        invokeEndPlayerActionCallback('A', table.getSeats()[currPlayerIndex].getUsername(), currPlayerIndex, bet, table.getSeats()[currPlayerIndex].getChipCount(), table.getPot(),  table.getCurrentStreetPot(), isPreFlop);
                    }
                    //if they bet:
                    else if (action == 'B' && bet.compareTo(minBet) >= 0) {
                        //update and track player fields
                        table.getSeats()[currPlayerIndex].setChipCount
                                (table.getSeats()[currPlayerIndex].getChipCount().add(table.getSeats()[currPlayerIndex].getCurrentBet()).subtract(bet));
                        //TODO math might be wrong
                        table.getSeats()[currPlayerIndex].setAmountBetThisHand(table.getSeats()[currPlayerIndex].getAmountBetThisHand().add(bet));
                        //check if player is all in
                        if(table.getSeats()[currPlayerIndex].getChipCount().compareTo(BigDecimal.valueOf(0)) == 0) {
                            table.getSeats()[currPlayerIndex].setAllIn(true);
                            table.setAllInCount(table.getAllInCount() + 1);
                        }

                        //update the pot size of the table
                        table.setPot((table.getPot().add(bet.subtract(table.getSeats()[currPlayerIndex].getCurrentBet()))).setScale(2, BigDecimal.ROUND_HALF_UP));

                        //change the player's current bet to the bet they just made
                        table.getSeats()[currPlayerIndex].setCurrentBet(bet);

                        //update the pot size of the current street
                        table.setCurrentStreetPot((bet.add(table.getCurrentStreetPot())).setScale(2, BigDecimal.ROUND_HALF_UP));

                        //check if the bet amount is greater than currentBet
                        if(bet.compareTo(table.getCurrentBet()) > 0) {
                            //if it is, set the current bet to previous bet and current bet to the player's bet
                            previousBet = table.getCurrentBet();
                            table.setCurrentBet(bet);
                            minBet = (table.getCurrentBet().subtract(previousBet)).add(table.getCurrentBet());
                        }

                        //sends message to the controller to update the user's stack size, bet display, and the pot size
                        invokeEndPlayerActionCallback('B', table.getSeats()[currPlayerIndex].getUsername(), currPlayerIndex, bet, table.getSeats()[currPlayerIndex].getChipCount(), table.getPot(), table.getCurrentStreetPot(), isPreFlop);
                    }
                    //if they calL:
                    else if(action == 'P') {
                        //update player chip count
                        table.getSeats()[currPlayerIndex].setChipCount(table.getSeats()[currPlayerIndex].getChipCount().subtract(bet));
                        //TODO math might be wrong
                        table.getSeats()[currPlayerIndex].setAmountBetThisHand(table.getSeats()[currPlayerIndex].getAmountBetThisHand().add(bet));
                        //check if player is all in
                        if(table.getSeats()[currPlayerIndex].getChipCount().compareTo(BigDecimal.valueOf(0)) == 0) {
                            table.setAllInCount(table.getAllInCount() + 1);
                            table.getSeats()[currPlayerIndex].setAllIn(true);
                        }

                        //update the pot size of the table
                        table.setPot((table.getPot().add(bet)).setScale(2, BigDecimal.ROUND_HALF_UP));

                        //change the current bet to the bet that was just mad
                        table.getSeats()[currPlayerIndex].setCurrentBet(table.getSeats()[currPlayerIndex].getCurrentBet().add(bet));

                        //update the pot size of the current street
                        table.setCurrentStreetPot((table.getCurrentStreetPot().add(bet)).setScale(2, BigDecimal.ROUND_HALF_UP));

                        //sends message to the controller to update the user's stack size, bet display, and the pot size
                        invokeEndPlayerActionCallback('P', table.getSeats()[currPlayerIndex].getUsername(), currPlayerIndex, bet, table.getSeats()[currPlayerIndex].getChipCount(), table.getPot(),  table.getCurrentStreetPot(), isPreFlop);
                    }
                    else {
                        System.out.println("wrong action or bet input");
                    }

                    //if all the active players are all in, action is over
                    if(table.getAllInCount() == table.getSeatedPlayerCount() - table.getSeatedFoldCount()) {
                        table.setActionComplete(true);
                        break;
                    }

                    //move currPlayerIndex over by 1
                    currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
                    //loops until a non-empty seat with non-folded player
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

    //resets all values that the table tracks after each street, sends a callback method to represent the reset values in the front-end
    private void cleanUp(Table table) {
        //clean up table currentBet and player current bets
        table.setCurrentBet(BigDecimal.valueOf(0));
        table.setCurrentStreetPot(BigDecimal.valueOf(0));
        for (Player player : table.getSeats()) {
            if (player != null) player.setCurrentBet(BigDecimal.valueOf(0));
        }
        invokeCleanUpCallback(table.isHandOver());
    }

    //sends a hashmap of the seats and players who are still in the hand to the front end
    private void showdown(Table table) {
        //if there's only one player in the hand, continue
        if(table.getSeatedPlayerCount() - table.getSeatedFoldCount() == 1);
        //else find the players who are still in the hand and their seats
        else {
            HashMap<Integer, Player> indexAndPlayer = new HashMap<>();
            for (int i = 0; i < table.getSeats().length; i++) {
                if (table.getSeats()[i] != null && table.getSeats()[i].isInHand()) {
                    indexAndPlayer.put(i, table.getSeats()[i]);
                }
            }
            invokeShowdownCallback(indexAndPlayer);
        }
    }

    //calculates how many community cards need to be dealt, and then deals them with a sleep timer for dramatic effect
    private void dealRunout (Table table) {
        //if all in pre-flop, deal flop, turn, and river
        if(table.getBoard().size() == 0) {
            sleepTimer(3000);
            dealFlop(table);
            sleepTimer(3000);
            dealTurnOrRiver(table);
            sleepTimer(3000);
            dealTurnOrRiver(table);
        }
        //else if all in on flop, deal turn and river
        else if(table.getBoard().size() == 3) {
            sleepTimer(3000);
            dealTurnOrRiver(table);
            sleepTimer(3000);
            dealTurnOrRiver(table);
        }
        //else if all in on turn, deal river
        else if(table.getBoard().size() == 4) {
            sleepTimer(3000);
            dealTurnOrRiver(table);
        }
    }
    private void completeHand(Table table) {
        ArrayList<Player> winners = new ArrayList<>();
        HashMap<Integer, Player> indexAndWinner = new HashMap<>();

        // Only going through the if when there are at least two winners, more efficient
        if (table.getSeatedFoldCount() != table.getSeatedPlayerCount() - 1) {
            HandRanking max_rank = null;
            Player[] players = table.getSeats();
            //loop to find the highest hand rank among the players seated and in the hand
            for (Player player : players) {
                if (player == null || !player.isInHand());
                else if (max_rank == null)
                    max_rank = player.getHand().getHandRanking();
                else if (player.getHand().getHandRanking().getRanking() > max_rank.getRanking())
                    max_rank = player.getHand().getHandRanking();
            }
            //Appends all players with hand rank equal to the max rank
            ArrayList<Player> potential_winners = new ArrayList<>();
            for (Player player : players) {
                if (player != null && player.isInHand() &&
                        player.getHand().getHandRanking() == max_rank)
                    potential_winners.add(player);
            }
            // Initialize winners and find winner(s) within potential_winners array list
            for (Player player : potential_winners) {
                if (winners.size() == 0) {
                    winners.add(player);
                    indexAndWinner.put(getSeatOfPlayer(table, player.getUsername()), player);
                    continue;
                }
                int hand_pos = 0;
                boolean decided = false;
                // Comparing cards at index hand_pos within the current player in potential_winners
                // and the first player in winners
                while (hand_pos < 5 && !decided) {
                    int currWinnerVal = winners.get(0).getHand()
                            .getFiveCardHand()[hand_pos].getVal();
                    int currPlayerVal = player.getHand().getFiveCardHand()[hand_pos].getVal();
                    if (currWinnerVal > currPlayerVal)
                        decided = true;
                    else if (currWinnerVal == currPlayerVal)
                        hand_pos++;
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
                    indexAndWinner.put(getSeatOfPlayer(table, player.getUsername()), player);
                }
                // if we iterated through the whole hand and decided is false, then we know the current
                // player in potential_winners has an equal strength hand to the current winner(s), so
                // that player is added.
                else if (hand_pos == 5) {
                    winners.add(player);
                    indexAndWinner.put(getSeatOfPlayer(table, player.getUsername()), player);
                }
            }
            // Chip stacks of players in winners are updated to contain pot/winners.size()
            // additional chips.
        }
        //case for a single winner
        else {
            for (Player player : table.getSeats()) {
                if (player != null && player.isInHand()) {
                    winners.add(player);
                    indexAndWinner.put(getSeatOfPlayer(table, player.getUsername()), player);
                }
            }
        }
        // allocation of winnings
        for (Player winner : winners) {
            winner.setChipCount((winner.getChipCount().add(table.getPot().divide(BigDecimal.valueOf(winners.size())))).setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        //invoke callback method for ending the hand (clean up front end)
        invokeCompleteHandCallback(indexAndWinner);

        //hand is now over
        table.setHandOver(true);
    }

    //Goes through the list of players and reassigns Hand value after turn and river
    private void getHandRankings(Table table) {
        //iterates through players, gets their hand ranking, and sets it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                //creates new Hand and assigns it to the player
                Hand hand = new Hand(table.getSeats()[i].getHoleCards(), table.getBoard());
                table.getSeats()[i].setHand(hand);

                //assigns a handRanking to the player
                HandService handService = new HandService();
                table.getSeats()[i].getHand().setHandRanking(handService.findHandRanking(hand));

                //sends a message with a string of the hand ranking to the front end
                invokeHandRankingCallback(handService.toString(table.getSeats()[i].getHand().getFiveCardHand(),
                        table.getSeats()[i].getHand().getHandRanking()), table.getSeats()[i].getUsername());
            }
        }
    }

    //Resets Player and Table fields
    private void clearTable(Table table) {
        DeckService deckService = new DeckService();

        //resets player hands and hand statuses
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                table.getSeats()[i].setHand(null);
                table.getSeats()[i].setInHand(true);
                table.getSeats()[i].setAllIn(false);
            }
        }
        //joins the deck together
        table.setBoard(new ArrayList<Card>());
        deckService.joinDeck(table.getDeck());

        //reset the table fields
        table.setPot(BigDecimal.valueOf(0));
        table.setSeatedFoldCount(0);
        table.setActionComplete(false);
        table.setAllInCount(0);

        //heads-up edge case to switch blinds correctly
        if (table.getSeatedPlayerCount() == 2) {
            int sb = table.getSmallBlindIndex();
            table.setSmallBlindIndex(table.getBigBlindIndex());
            table.setBigBlindIndex(sb);
        }

        invokeCleanUpCallback(table.isHandOver());
    }

    //given a certain seat index, find the player at that seat
    public Player getPlayerAtSeat(Table table, int seat) {
        return table.getSeats()[seat];
    }


    //given a certain player username, find which seat they are sitting at
    private int getSeatOfPlayer(Table table, String username) {
        for (int i = 0; i < table.getSeats().length; i++) {
            if(table.getSeats()[i] != null && table.getSeats()[i].getUsername().equals(username))
                return i;
        }
        return -1;
    }

    //method that adds a delay between actions
    private void sleepTimer(int milliseconds) {
        try {
            Thread.sleep(milliseconds); // Sleep for 4 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //callback function used to send position info to the front-end
    private void invokeButtonCallback(int buttonIndex) {
        if(tableCallback != null) {
            tableCallback.onButtonSet(buttonIndex);
        }
    }
    private void invokeInitPotCallback(int sbIndex, int bbIndex, BigDecimal sbAmount, BigDecimal bbAmount, BigDecimal potSize) {
        if(tableCallback != null) {
            tableCallback.onPotInit(sbIndex, bbIndex, sbAmount, bbAmount, potSize);
        }
    }
    private void invokeDealHoleCardsCallback(String username, int seat, Card[] holeCards) {
        if(tableCallback != null) {
            tableCallback.onHoleCardsDealt(username, seat, holeCards);
        }
    }
    private void invokeDealBoardCardsCallback(ArrayList<Card> cards) {
        if(tableCallback != null) {
            tableCallback.onBoardCardsDealt(cards);
        }
    }
    public void invokeCleanUpCallback(boolean isHandOver) {
        if(tableCallback != null) {
            tableCallback.onCleanUp(isHandOver);
        }
    }
    private void invokePreFlopActionCallback(Player player, int seat, BigDecimal currentBet, BigDecimal potSize, BigDecimal minBet) {
        if(tableCallback != null) {
            tableCallback.onPreFlopAction(player, seat, currentBet, potSize, minBet);
        }
    }
    private void invokeEndPlayerActionCallback(char action, String username, int seatIndex, BigDecimal betAmount, BigDecimal stackSize, BigDecimal potSize, BigDecimal currentStreetPotSize, boolean isPreFlop) {
        if(tableCallback != null) {
            tableCallback.onEndPlayerAction(action, username, seatIndex, betAmount, stackSize, potSize, currentStreetPotSize, isPreFlop);
        }
    }

    private void invokeShowdownCallback(HashMap<Integer, Player> indexAndPlayer) {
        if(tableCallback != null) {
            tableCallback.onShowdown(indexAndPlayer);
        }
    }
    private void invokeCompleteHandCallback(HashMap<Integer, Player> indexAndWinner) {
        if(tableCallback != null) {
            tableCallback.onCompleteHand(indexAndWinner);
        }
    }
    private void invokeHandRankingCallback(String handRanking, String username) {
        if(tableCallback != null) {
            tableCallback.onHandRanking(handRanking, username);
        }
    }
}
