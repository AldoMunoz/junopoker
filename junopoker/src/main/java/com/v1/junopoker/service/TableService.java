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
    // Class that creates a new DeckService when needed (for decoupling purposes)
    private final DeckServiceFactory deckServiceFactory;
    // Interface for callback methods for all TableService methods that require user
    // interaction,
    // Callbacks are sent to the controller, and passed on to the UI via WebSocket
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

    // adds a player to the players list, gives them a chip count and a specific
    // seat
    public void addPlayer(Table table, Player player, int seat) {
        if (table.getSeats()[seat] == null)
            table.getSeats()[seat] = player;
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() + 1);
    }

    // removes player at the given seat
    public void removePlayer(Table table, int seat) {
        table.getSeats()[seat] = null;
        table.setSeatedPlayerCount(table.getSeatedPlayerCount() - 1);
    }

    // gathers and executes all the functions needed to run ring poker game
    public void runGame(Table table) {
        // starts the game and sets the blinds
        if (!table.isGameRunning()) {
            setBlinds(table);
            table.setGameRunning(true);
        }

        // game will run while there are at least 2 people seated at the table
        while (table.getSeatedPlayerCount() > 1) {
            // Add a 3-second delay between hand rounds
            try {
                Thread.sleep(3000); // Sleep for 3000 milliseconds (3 seconds)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            table.setHandOver(false);
            moveBlinds(table);
            initiatePot(table);
            dealCards(table);
            preFlopBetting(table);
            if (table.isHandOver()) {
                completeHand(table);
                clearTable(table);
                continue;
            }
            cleanUp(table);
            dealFlop(table);
            postFlopBetting(table);
            if (table.isHandOver()) {
                completeHand(table);
                clearTable(table);
                continue;
            }
            cleanUp(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            if (table.isHandOver()) {
                completeHand(table);
                clearTable(table);
                continue;
            }
            cleanUp(table);
            dealTurnOrRiver(table);
            postFlopBetting(table);
            completeHand(table);
            clearTable(table);
        }
    }

    // sets the BB and SB
    public void setBlinds(Table table) {
        for (int i = 0; i < table.getSeats().length; i++) {
            if (table.getSeats()[i] == null)
                ;
            else {
                if (table.getSmallBlindIndex() == -1)
                    table.setSmallBlindIndex(i);
                else {
                    table.setBigBlindIndex(i);
                    break;
                }
            }
        }
    }

    // moves the BB and SB to the next player
    // sets or moves the button
    public void moveBlinds(Table table) {
        // SB is set to person who was just BB
        table.setSmallBlindIndex(table.getBigBlindIndex());

        int seatCount = table.getSeatCount();
        boolean bigBlindSet = false;
        // rotates clockwise using modulus until the next player is found, assigns them
        // the BB
        for (int i = (table.getBigBlindIndex() + 1) % seatCount; i < seatCount; i = (i + 1) % seatCount) {
            if (table.getSeats()[i] == null)
                ;
            // first player found after big blind = new big blind
            else if (!bigBlindSet) {
                table.setBigBlindIndex(i);
                bigBlindSet = true;
            }
            // next player found after big blind = button
            else {
                table.setDealerButton(i);
                invokeButtonCallback(table.getDealerButton());
                break;
            }
        }
    }

    // callback function used to send position info to the front-end
    private void invokeButtonCallback(int buttonIndex) {
        if (tableCallback != null) {
            tableCallback.onButtonSet(buttonIndex);
        }
    }

    // collects blinds and adds them to the pot
    public void initiatePot(Table table) {
        Player[] seats = table.getSeats();
        int smallBlindIndex = table.getSmallBlindIndex();
        int bigBlindIndex = table.getBigBlindIndex();
        int[] stakes = table.getStakes();

        float sbAmount = stakes[0];
        float bbAmount = stakes[1];

        // BB collection
        if (seats[bigBlindIndex].getChipCount() > stakes[1]) {
            table.setPot(table.getPot() + stakes[1]);
            seats[bigBlindIndex].setChipCount(seats[bigBlindIndex].getChipCount() - stakes[1]);
        }
        // edge case, if the players stack size is less than the blind
        else {
            bbAmount = seats[bigBlindIndex].getChipCount();
            table.setPot(table.getPot() + seats[bigBlindIndex].getChipCount());
            seats[bigBlindIndex].setChipCount(0);
        }

        // SB collection
        if (seats[smallBlindIndex].getChipCount() > stakes[0]) {
            table.setPot(table.getPot() + stakes[0]);
            seats[smallBlindIndex].setChipCount(seats[smallBlindIndex].getChipCount() - stakes[0]);
        }
        // edge case, if the players stack size is less than the blind
        else {
            sbAmount = seats[smallBlindIndex].getChipCount();
            table.setPot(table.getPot() + seats[smallBlindIndex].getChipCount());
            seats[smallBlindIndex].setChipCount(0);
        }

        invokeInitPotCallback(table.getSmallBlindIndex(), table.getBigBlindIndex(), sbAmount, bbAmount, table.getPot());
    }

    private void invokeInitPotCallback(int sbIndex, int bbIndex, double sbAmount, double bbAmount, double potSize) {
        if (tableCallback != null) {
            tableCallback.onPotInit(sbIndex, bbIndex, sbAmount, bbAmount, potSize);
        }
    }

    // deals cards preflop to all players
    public void dealCards(Table table) {
        // the deck will be organized in a random order before the round starts
        DeckService deckService = new DeckService();
        deckService.shuffleCards(table.getDeck());

        // deals cards to every seat with an active player in it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                // creates an arraylist where the two dealt cards will be added
                Card[] cards = new Card[2];
                cards[0] = deckService.drawCard(table.getDeck());
                cards[1] = deckService.drawCard(table.getDeck());
                // the array is set as the player's hole cards
                table.getSeats()[i].setHoleCards(cards);
                table.getSeats()[i].setInHand(true);

                invokeDealHoleCardsCallback(table.getSeats()[i].getUsername(), i, cards);
            }
        }
    }

    private void invokeDealHoleCardsCallback(String username, int seat, Card[] holeCards) {
        if (tableCallback != null) {
            tableCallback.onHoleCardsDealt(username, seat, holeCards);
        }
    }

    // deals the flop out
    public void dealFlop(Table table) {
        DeckService deckService = new DeckService();

        // add 3 cards to the board
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));
        table.getBoard().add(deckService.drawCard(table.getDeck()));

        // assign hand ranking for each player and send hand rankings to the controller
        getHandRankings(table);

        // callback method to send the flop to the controller
        invokeDealBoardCardsCallback(table.getBoard());
    }

    private void invokeDealBoardCardsCallback(ArrayList<Card> cards) {
        if (tableCallback != null) {
            tableCallback.onBoardCardsDealt(cards);
        }
    }

    // deals the turn or river card
    public void dealTurnOrRiver(Table table) {
        DeckService deckService = deckServiceFactory.createDeckService();

        // TODO this might be too memory intensive, might be easier to create seperate
        // callback method for a turn/river card
        // draws a card and adds it to an array (makes it compatible with the callback
        // method)
        Card card = deckService.drawCard(table.getDeck());
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(card);

        // adds 1 card to the board
        table.getBoard().add(card);

        // assign hand ranking for each player and send hand rankings to the controller
        getHandRankings(table);

        // callback method to send the card to the controller in the form or an
        // ArrayList of length 1
        invokeDealBoardCardsCallback(cards);
    }

    // deals with the pre-flop betting rounds
    public void preFlopBetting(Table table) {
        // creating local fields for SB, BB, stakes, seats
        int smallBlind = table.getSmallBlindIndex();
        int bigBlind = table.getBigBlindIndex();
        int[] stakes = table.getStakes();
        Player[] seats = table.getSeats();
        // set current bet for player in SB and BB position
        table.getSeats()[smallBlind].setCurrentBet(stakes[0]);
        table.getSeats()[bigBlind].setCurrentBet(stakes[1]);

        // find the first player to act
        int currPlayerIndex = (bigBlind + 1) % table.getSeatCount();
        while (seats[currPlayerIndex] == null) {
            currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
        }

        // set the current table bet to the big blind
        table.setCurrentBet(stakes[1]);

        // handle betting round actions from players
        playerAction(table, currPlayerIndex, true);

        // switch sb and bb if game is heads up
        if (table.getSeatedPlayerCount() == 2) {
            int temp = bigBlind;
            table.setBigBlindIndex(table.getSmallBlindIndex());
            table.setSmallBlindIndex(temp);
        }
    }

    public void postFlopBetting(Table table) {
        // set fields
        int smallBlind = table.getSmallBlindIndex();
        Player[] seats = table.getSeats();

        // Identify who will be first to act
        int currPlayerIndex = smallBlind;
        while (seats[currPlayerIndex] == null) {
            currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
        }

        // handle betting round actions from players
        playerAction(table, currPlayerIndex, false);
    }

    public void cleanUp(Table table) {
        // clean up table currentBet and player current bets
        table.setCurrentBet(0);
        for (Player player : table.getSeats()) {
            if (player != null)
                player.setCurrentBet(0);
        }
        invokeCleanUpCallback(table.isHandOver());
    }

    public void invokeCleanUpCallback(boolean isHandOver) {
        if (tableCallback != null) {
            tableCallback.onCleanUp(isHandOver);
        }
    }

    // handles player actions during the betting round until the betting round is
    // over
    public void playerAction(Table table, int currPlayerIndex, boolean isPreFlop) {
        int firstToActIndex = currPlayerIndex;
        boolean actionOver = false;
        boolean firstAction = true;
        int seatedPlayerCount = table.getSeatedPlayerCount();
        int[] stakes = table.getStakes();
        float previousBet = 0;

        float minBet;
        if (isPreFlop)
            minBet = (table.getCurrentBet() - previousBet) + table.getCurrentBet();
        else
            minBet = table.getStakes()[1];

        // loop will continue until all action for the round is over
        while (!actionOver) {
            // if everyone has folded (except for 1 player), the whole hand is over, skip to
            // complete hand
            if (table.getSeatedFoldCount() == seatedPlayerCount - 1) {
                table.setHandOver(true);
                break;
            }
            // skips any empty seats or folded players
            /*
             * if (table.getSeats()[currPlayerIndex] == null ||
             * !table.getSeats()[currPlayerIndex].isInHand()) {
             * currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
             * }
             */
            // PRE-FLOP
            // if we've looped back to the original raiser and player CurrentBet = table
            // CurrentBet, action is over
            else if (isPreFlop &&
                    table.getSeats()[currPlayerIndex].getCurrentBet() == table.getCurrentBet() &&
                    table.getCurrentBet() > stakes[1])
                actionOver = true;

            // PRE-FLOP
            // if we're not at the big blind and the table currentBet = the big blind, it is
            // a limped pot, action is over
            else if (isPreFlop &&
                    table.getSeats()[currPlayerIndex].getCurrentBet() == stakes[1] &&
                    table.getCurrentBet() == stakes[1] &&
                    currPlayerIndex != table.getBigBlindIndex())
                actionOver = true;

            // POST-FLOP
            // if we've looped back to original raiser and player currentBet = table
            // currentBet, action is over
            else if (!isPreFlop &&
                    table.getSeats()[currPlayerIndex].getCurrentBet() == table.getCurrentBet() &&
                    table.getCurrentBet() > 0)
                actionOver = true;

            // POST-FLOP
            // if we've checked around to the firstToAct player, action is over
            else if (!isPreFlop &&
                    currPlayerIndex == firstToActIndex && table.getCurrentBet() == 0 &&
                    !firstAction)
                actionOver = true;

            // else take an action input from the player
            else {
                // callback method for preflop action callback
                // returns an int (bet size)
                invokePreFlopActionCallback(table.getSeats()[currPlayerIndex], currPlayerIndex, table.getCurrentBet(),
                        table.getPot(), minBet);

                CompletableFuture<PlayerActionResponse> future = new CompletableFuture<>();
                playerActionResponse = future;

                try {
                    PlayerActionResponse playerActionResponse = future.get();
                    char action = playerActionResponse.getAction();
                    float bet = playerActionResponse.getBetAmount();

                    // if they fold:
                    if (action == 'F' && bet == 0) {
                        table.setSeatedFoldCount(table.getSeatedFoldCount() + 1);
                        table.getSeats()[currPlayerIndex].setInHand(false);

                        invokeEndPlayerActionCallback('F', table.getSeats()[currPlayerIndex].getUsername(),
                                currPlayerIndex, 0, 0, 0);
                    }
                    // if they check:
                    else if (action == 'C' && bet == 0) {
                        invokeEndPlayerActionCallback('C', table.getSeats()[currPlayerIndex].getUsername(),
                                currPlayerIndex, 0, 0, 0);
                    }
                    // if they bet:
                    else if (action == 'B' && bet >= minBet) {
                        // update player chip count
                        table.getSeats()[currPlayerIndex].setChipCount(table.getSeats()[currPlayerIndex].getChipCount()
                                + table.getSeats()[currPlayerIndex].getCurrentBet() - bet);
                        // update the pot size of the table
                        table.setPot(table.getPot() + (bet - table.getSeats()[currPlayerIndex].getCurrentBet()));
                        // update the player's current bet
                        table.getSeats()[currPlayerIndex].setCurrentBet(bet);

                        // check if the bet amount is greater than currentBet
                        if (bet > table.getCurrentBet()) {
                            // if it is, set the current bet to previous bet and current bet to the player's
                            // bet
                            previousBet = table.getCurrentBet();
                            table.setCurrentBet(bet);
                            minBet = (table.getCurrentBet() - previousBet) + table.getCurrentBet();
                        }
                        // sends message to the controller to update the user's stack size, bet display,
                        // and the pot size
                        invokeEndPlayerActionCallback('B', table.getSeats()[currPlayerIndex].getUsername(),
                                currPlayerIndex, bet, table.getSeats()[currPlayerIndex].getChipCount(), table.getPot());
                    }
                    // if they calL:
                    else if (action == 'P') {
                        // update player chip count
                        table.getSeats()[currPlayerIndex]
                                .setChipCount(table.getSeats()[currPlayerIndex].getChipCount() - bet);
                        // update the pot size of the table
                        table.setPot(table.getPot() + bet);
                        // update the player's current bet
                        table.getSeats()[currPlayerIndex]
                                .setCurrentBet(table.getSeats()[currPlayerIndex].getCurrentBet() + bet);

                        // sends message to the controller to update the user's stack size, bet display,
                        // and the pot size
                        invokeEndPlayerActionCallback('P', table.getSeats()[currPlayerIndex].getUsername(),
                                currPlayerIndex, bet, table.getSeats()[currPlayerIndex].getChipCount(), table.getPot());
                    } else {
                        System.out.println("wrong action or bet input");
                    }

                    // loops over seats to get the next player to act
                    currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
                    // loops until a non-empty seat with non-folded player
                    while (table.getSeats()[currPlayerIndex] == null
                            || !(table.getSeats()[currPlayerIndex].isInHand())) {
                        currPlayerIndex = (currPlayerIndex + 1) % table.getSeatCount();
                    }
                    // first action has now taken place, set to false
                    firstAction = false;
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void invokePreFlopActionCallback(Player player, int seat, float currentBet, float potSize, float minBet) {
        if (tableCallback != null) {
            tableCallback.onPreFlopAction(player, seat, currentBet, potSize, minBet);
        }
    }

    private void invokeEndPlayerActionCallback(char action, String username, int seatIndex, float betAmount,
            float stackSize, float potSize) {
        if (tableCallback != null) {
            tableCallback.onEndPlayerAction(action, username, seatIndex, betAmount, stackSize, potSize);
        }
    }

    public void completeHand(Table table) {
        // Initializing the best made hand rank
        ArrayList<Player> winners = new ArrayList<>();
        // Only going through the if when there are at least two winners, more efficient
        if (table.getSeatedFoldCount() != table.getSeatedPlayerCount() - 1) {
            HandRanking max_rank = null;
            Player[] players = table.getSeats();
            for (Player player : players) {
                if (player == null || !player.isInHand()) {
                    continue;
                } else if (max_rank == null)
                    max_rank = player.getHand().getHandRanking();
                else if (player.getHand().getHandRanking().getRanking() > max_rank.getRanking())
                    max_rank = player.getHand().getHandRanking();
            }
            // Appending all players with hand rank equal to the max rank
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
                    continue;
                }
                int hand_pos = 0;
                boolean decided = false;
                // Comparing cards at index hand_pos within the current player in
                // potential_winners
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
                // Winners array list is updated to just contain the current player in
                // potential_winners
                // if their hand is better. Otherwise, we continue to
                // the next iteration since a player with a worse hand will not be a winner.
                if (decided && winners.size() == 0) {
                    winners.add(player);
                }
                // if we iterated through the whole hand and decided is false, then we know the
                // current
                // player in potential_winners has an equal strength hand to the current
                // winner(s), so
                // that player is added.
                else if (hand_pos == 5)
                    winners.add(player);
            }
            // Chip stacks of players in winners are updated to contain pot/winners.size()
            // additional chips.
        }
        // else is a general case for a single winner
        else {
            for (Player player : table.getSeats()) {
                if (player != null && player.isInHand()) {
                    winners.add(player);
                }
            }
        }
        // allocation of winnings
        for (Player winner : winners) {
            winner.setChipCount(winner.getChipCount() + (table.getPot() / winners.size()));
        }

        // invoke callback method for ending the hand (clean up front end)
        invokeCompleteHandCallback(table.getSeats());

        // hand is now over
        table.setHandOver(true);
    }

    private void invokeCompleteHandCallback(Player[] seats) {
        if (tableCallback != null) {
            tableCallback.onCompleteHand(seats);
        }
    }

    // Goes through the list of players and reassigns Hand value after flop, turn,
    // and river
    public void getHandRankings(Table table) {
        // iterates through players, gets their hand ranking, and sets it
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                // creates new Hand and assigns it to the player
                Hand hand = new Hand(table.getSeats()[i].getHoleCards(), table.getBoard());
                table.getSeats()[i].setHand(hand);

                HandService handService = new HandService();
                table.getSeats()[i].getHand().setHandRanking(handService.findHandRanking(hand));

                // TODO callback method to each individual player with their hand ranking
                String toStringHand = handService.toString(table.getSeats()[i].getHand().getFiveCardHand(),
                        table.getSeats()[i].getHand().getHandRanking());
                System.out.println(toStringHand);
            }
        }
    }

    // Resets Player and Table fields
    public void clearTable(Table table) {
        DeckService deckService = new DeckService();

        // resets player hands and hand statuses
        for (int i = 0; i < table.getSeatCount(); i++) {
            if (table.getSeats()[i] != (null)) {
                table.getSeats()[i].setHand(null);
                table.getSeats()[i].setInHand(true);
            }
        }
        // joins the deck together
        table.setBoard(new ArrayList<Card>());
        deckService.joinDeck(table.getDeck());

        // reset the table fields
        table.setPot(0);
        table.setSeatedFoldCount(0);

        // heads-up edge case
        if (table.getSeatedPlayerCount() == 2) {
            int sb = table.getSmallBlindIndex();
            table.setSmallBlindIndex(table.getBigBlindIndex());
            table.setBigBlindIndex(sb);
        }

        invokeCleanUpCallback(table.isHandOver());
    }

    public Player getPlayerAtSeat(Table table, int seat) {
        return table.getSeats()[seat];
    }
}
