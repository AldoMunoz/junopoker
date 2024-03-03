'use strict'

//logic for when player submits modal after selecting a seat button
//Stores player data in an object
//Stores the Player in the Table
//creates Websocket subscription specifically for that player
function submitPlayerData() {
    //parse inputs
    const usernameInput = $("#username").val().trim();
    const chipCountInput = parseInt($("#chipCount").val());

    if (usernameInput && chipCountInput) {
        //create Player object
        const player = {
            username: usernameInput,
            chipCount: chipCountInput,
            holeCards: null,
            hand: null,
            inHand: false,
            currentBet: 0,
            isActive: false
        };

        //create payload that will be sent to the backend
        const playerRequest = {
            player: player,
            seatIndex: currentButtonNumber,
            tableID: $("#table-id").val()
        };
        // Make a Fetch API POST request to controller
        fetch("/createPlayer", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(playerRequest)
        }).then(response => {
            // Check if the response is successful (status code 2xx)
            if (!response.ok) {
                console.log("error occurred creating player")
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(() => {
            //if fetch call is successful, the following logic wil execute:

            //method used to subscribe user to player-events topic
            subscribeToPlayerTopic(usernameInput, player, currentButtonNumber);

            // Close the modal
            closeAddPlayerModal();
        }).catch(error => {
            console.error('Error occurred:', error);
        });
    }
}

//Subscribe user to personal channel player-events/${username}
//subscription for private events (player cards, show player actions when it's their turn. etc.)
//Send data to "addUser" about new player
function subscribeToPlayerTopic(usernameInput, player, seatIndex) {
    // Subscribe to the player-specific topic
    //all messages sent to /topic/playerEvents/${} will be redirected to "playerEvents(payload) method below"
    stompClient.subscribe(`/topic/playerEvents/${usernameInput}`, playerEvents);
    isSeated = true;
    console.log("Set is seated to true");

    if(player && stompClient) {
        //create PlayerRequest object to be sent to the front end
        let playerRequest = {
            type: "SIT",
            player: player,
            seatIndex: seatIndex,
            tableID: $("#table-id").val()
        };
        //sends message to all users that a player has taken a seat
        stompClient.send("/app/tableEvents", {}, JSON.stringify(playerRequest));
        //sends message to the player to hide other seat buttons
        stompClient.send("/app/playerEvents", {}, JSON.stringify(playerRequest));
    }
    else console.log("Something went wrong before Websocket could send")
}


//handle all messages sent to /topic/playerEvents/${}
async function playerEvents(payload) {
    //parse message body
    let message = JSON.parse(payload.body);

    //function for when player takes a seat
    if(message.type === "SIT") await sitPlayerEvent(message);
    //function for when player stands from table
    else if(message.type === "STAND") await standPlayerEvent(message);
    //function for populate empty seats with seat button
    else if(message.type === "SEATS") populateSeatsEvent(message);
    //function for dealing player's private hold cards
    else if (message.type === "DEAL_PRE") privateDealHoleCardsEvent(message);
    //function for setting up and displaying player's private action HUD
    else if (message.type === "PLAYER_ACTION") privatePlayerActionEvent(message);
    //function for displaying player's hand ranking
    else if(message.type === "HAND_RANKING") handRankingEvent(message);
    //function for displaying rebuy modal
    else if(message.type === "REBUY") rebuyEvent(message);
    //function to display cards on hover after a player folds
    else if (message.type === "FOLD") foldEvent(message);
}
async function sitPlayerEvent(message) {
    console.log("Sit ", message);

    //loop to hide all seat buttons once the player sits
    let seatedPlayerCount = 0;
    for (let i = 0; i < message.seats.length; i++) {
        if (message.seats[i] === null) {
            const seatDiv = $(`#seat-${i}`);
            seatDiv.hide();
        }
        //count the amount of seated players at the table
        else seatedPlayerCount++;
    }

    //show the settings bar
    const settingsBar = $('.settings-bar');
    settingsBar.css("display", "flex");
    //associates the settings bar with a specific seat
    //so, for example, the controller knows who to remove when a player clicks the "stand" button
    settingsBar.attr("data-seat", message.seatIndex);

    stompClient.send("/app/countPlayers", {}, $("#table-id").val());
}
async function standPlayerEvent(message) {
    //console.log("Stand ", message);

    //const seats = await fetchTableSeats();
    //populateTable(seats);

    //hide settings bar
    const settingsBar = $('.settings-bar');
    settingsBar.css("display", "none");

    //send request to the controller to get the seats at the table
    const request = {
        type: "SEATS",
        tableId: $("#table-id").val(),
        username: message.username
    };
    stompClient.send("/app/getSeats", {}, request);
}

function populateSeatsEvent(message) {
    console.log("Seats ", message);

    //TODO populate the table with seat buttons

    //unsubscribe user
    stompClient.unsubscribe(`/topic/playerEvents/${message.username}`);
}
function privateDealHoleCardsEvent(message) {
    //console.log("Private deal hole cards ", message);
    console.log("Private deal cards message: ", message);
    const holeCardsDiv = $(`#seat-${message.seat} .hole-cards`)
    holeCardsDiv.empty();
    holeCardsDiv.append(`<img src="/images/cards/${message.cards[0]}.png" alt="Card 1">`)
    holeCardsDiv.append(`<img src="/images/cards/${message.cards[1]}.png" alt="Card 2">`)
    holeCardsDiv.css("opacity", "100");
    holeCardsDiv.show();

    $("#hand-ranking").text("");
    $("#hand-ranking").hide();
}

function privatePlayerActionEvent(message) {
    console.log("Private player action ", message);

    //set min bet value, max bet value, pot size, and the seat in slider.js;
    setMinValue(message.minBet);
    setMaxValue(message.player.chipCount + message.player.currentBet);
    setPotSize(message.potSize);
    setSeat(message.seat);
    setCurrentBet(message.currentBet);

    //clear the basic actions div, apart from the fold button, which is always required
    const basicActionsDiv = $(".basic-actions");
    basicActionsDiv.children().not("#fold").remove();
    //boolean to decide if the slider should be displayed or not
    let limitActions = false;

    //if the current bet is greater than the player's stack, they can fold or go all-in
    if(message.currentBet >= message.player.chipCount + message.player.currentBet) {
        const ChipCount = new BigNumber(message.player.chipCount);
        const PlayerCurrentBet = new BigNumber(message.player.currentBet);

        //add "all-in" button
        basicActionsDiv.append(`<button id="all-in" onclick="onAllIn()">All-In: ${ChipCount.plus(PlayerCurrentBet)}</button>`);
        limitActions = true;
    }
    //if the min bet would be greater than the player's stack, they can fold, call, or go all-in
    else if (message.minBet > message.player.chipCount && message.player.currentBet !== message.currentBet) {
        const CurrentBet = new BigNumber(message.currentBet.toString());
        const PlayerCurrentBet = new BigNumber(message.player.currentBet.toString());
        const ChipCount = new BigNumber(message.player.chipCount.toString());

        //add "call" and "all-in" buttons
        basicActionsDiv.append(`<button id="call" onclick="onCall()">Call: ${CurrentBet.minus(PlayerCurrentBet)}</button>`)
        basicActionsDiv.append(`<button id="all-in" onclick="onAllIn()">All-In: ${ChipCount.plus(PlayerCurrentBet)}</button>`)
        limitActions = true;
    }
    //if the player's bet is not equal to the table bet, they can fold, call, or raise
    else if(message.player.currentBet !== message.currentBet) {
        const CurrentBet = new BigNumber(message.currentBet.toString());
        const PlayerCurrentBet = new BigNumber(message.player.currentBet.toString());

        //add "call" and "raise" buttons
        basicActionsDiv.append(`<button id="call" onclick="onCall()">Call: ${CurrentBet.minus(PlayerCurrentBet)}</button>`)
        basicActionsDiv.append(`<button id="raise" onclick="onBet()">Raise: </button>`)
        setBetButtonType('r');
        limitActions = false;
    }
    //if both the table's current bet and the player's current bet == 0, they can fold, check, or call
    //or if the player's current bet is equal to the table's current bet (preflop)
    else if ((message.player.currentBet === 0 && message.currentBet === 0) || (message.player.currentBet === message.currentBet)) {
        //add "check" and "bet" buttons
        basicActionsDiv.append(`<button id="check" onclick="onCheck()">Check</button>`);
        basicActionsDiv.append(`<button id="bet" onclick="onBet()">Bet: </button>`)
        setBetButtonType('b');
        limitActions = false;
    }

    //default the slider and bet value to min bet (0%)
    const bet = updateSlider(0);
    updateBetButton(bet);
    updateInputBox(bet);

    //display the action bar
    if (limitActions === true) {
        $(".custom-bet").css("display", "none");
        $(".bet-sizes").css("display", "none");
        $(".action-bar").css("display", "flex");
    }
    else {
        $(".custom-bet").css("display", "flex");
        $(".bet-sizes").css("display", "flex");
        $(".action-bar").css("display", "flex");
    }
}

function handRankingEvent(message) {
    //console.log("Hand Ranking Event: ", message);

    $("#hand-ranking").text(message.handRanking);
    $("#hand-ranking").show();
}

//Displays rebuy modal for the player
function rebuyEvent(message) {
    console.log("Rebuy", message);

    //Pass seat index to the modal
    $("#rebuyModal").data("seatIndex", message.seatIndex);
    $("#rebuyModal").data("tableId", message.tableId);

    //open modal
    $("#rebuyModal").show();
}

//Makes the player's hole cards visible on-hover after folding
function foldEvent(message) {
    //console.log("Fold event ", message);

    //find the player's cards
    const seatDiv = $(`#seat-${message.seat}`);
    const cardsDiv = seatDiv.find(".hole-cards");
    //display the cards on the page with 0 opacity (invisible)
    cardsDiv.css("opacity", "0");
    cardsDiv.css("display", "flex");

    cardsDiv.hover(
        //on hover, display the cards at 50% opacity
        function() {
            cardsDiv.css("opacity", "0.5");
        },
        //on exit, display the cards at 0% opacity
        function () {
            cardsDiv.css("opacity", "0");
        }
    )
}

//Opens the player modal
function openAddPlayerModal(buttonNumber) {
    // Show the modal
    $("#addUserModal").show();
    // Set a data attribute on the modal to store the button number
    currentButtonNumber = buttonNumber;
    console.log("Seat value in openAddPlayerModal", currentButtonNumber);
}
//Closes the player modal
function closeAddPlayerModal() {
    // Hide the modal
    $("#addUserModal").hide();
}
//Closes the rebuy modal and unsubscribes the player from their personal WebSocket connection
function closeRebuyModal() {
    //TODO unsubscribe the player from their personal WebSocket connection

    const request = {
        seatIndex: $("#rebuyModal").data("seatIndex"),
        tableId: $("#rebuyModal").data("tableId")
    }

    isSeated = false;
    console.log("Set is seated to false");
    $("#rebuyModal").hide();
    stompClient.send("/app/standPlayerAtSeat", {}, JSON.stringify(request));
}

//Adds the player back into the game at the earliest convenience
function completeRebuy() {
    //hide the modal immediately to prevent double buy-in
    $("#rebuyModal").hide();

    let rebuyAmount = new BigNumber($("#rebuyAmount").val());

    //Create DTO to send to controller with the rebuy amount and the seat index of the player
    const rebuyDTO = {
        type: "REBUY",
        rebuyAmount: rebuyAmount.toString(),
        seatIndex: $("#rebuyModal").data("seatIndex"),
        tableId: $("#rebuyModal").data("tableId")
    };

    //sends message to controller with rebuy information
    stompClient.send("/app/rebuy", {}, JSON.stringify(rebuyDTO));
}