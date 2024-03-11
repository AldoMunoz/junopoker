'use strict'
// Global Variables
//whenever a user takes a seat, this number will correspond to the seat they chose

let currentButtonNumber = -1;
//WebSocket connection
let stompClient = null;
let isSeated = false;

//When user opens the page:
//Create new table if one doesn't exist
//Create WebSocket connection for the user
$(document).ready(function () {
    fetch("/doesTableExist")
        .then(response => response.json())
        .then(tableExists => {
            console.log("TableExists:", tableExists);
            if(tableExists) {
                console.log("Table exists in HttpSession");

                //fetches table data to populate headers
                fetch( `/getTableData`)
                    .then(response => response.json())
                    .then(tableData => {
                        $("#table-id").val(tableData.tableID);

                        //set the small blind = 1;
                        setSmallBlind(tableData.stakes[0]);

                        $('#game-type').text(`Game Type: ${tableData.gameType}`);
                        $('#stakes').text(`Stakes: ${tableData.stakes[0]}/${tableData.stakes[1]}`);
                    })
                establishWebSocketConnection();
            }
            //else if table doesn't exist
            else {
                console.log("Creating new table...")
                //creates default table object
                const defaultTableData = {
                    gameType: "NLH",
                    stakes: [1, 2]
                };

                //store the table data in the HttpSession
                fetch("/storeTableData", {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(defaultTableData)
                }).then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.text(); // Assuming the response contains text
                }).then(tableID => {
                    console.log("Table data created and stored in HttpSession");
                    //store the table id
                    $("#table-id").val(tableID);

                    //set the small blind = 1;
                    setSmallBlind(1);

                    // Populate the <h4> headers with the selected game type and stakes
                    $('#game-type').text(`Game Type: NLH`);
                    $('#stakes').text(`Stakes: 1/2`);

                    // Establish WebSocket connection
                    establishWebSocketConnection();
                }).catch(error => {
                    console.error('Error occurred while storing table data:', error);
                });
            }
        });
});


/*WEB SOCKET CONNECTION
/*-----------------------------------*/

//Creates a new WebSocket connection
function establishWebSocketConnection() {
    let socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    //calls method depending on if websocket connection is successful or erroneous
    stompClient.connect({}, onConnected, onError);
}

//Subscribe user to "seated-players" and "poker-events"
async function onConnected() {
    //subscribes users to tableEvents, public topic that will impact the view of all players
    //all messages sent to /topic/table events will be redirected to "tableEvents(payload) method below"
    stompClient.subscribe("/topic/tableEvents", tableEvents);
    stompClient.send("/app/getSeats", {}, $("#table-id").val());
}

//Displays error message if WebSocket Connection is unsuccessful
function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!')
}



/*TABLE EVENTS
/*-----------------------------------*/

//Handle seated Player payloads
function tableEvents(payload) {
    //parse the body of the message
    let message = JSON.parse(payload.body);

    //view logic for display all players at the table
    if(message.type === "SEATS") seatsEvent(message);
    //view logic for when a new player takes a seat
    else if(message.type === "SIT") sitTableEvent(message);
    //view logic for when player stands up from seat
    else if(message.type === "STAND") standTableEvent(message);
    //view logic for when button is set
    else if(message.type === "MOVE_BUTTON") moveButtonEvent(message);
    //view logic for when blinds are set and collected
    else if(message.type === "INIT_POT") initPotEvent(message);
    //view logic for displaying player hole cards
    else if(message.type === "DEAL_PRE") dealHoleCardsEvent(message);
    //view logic for highlighting which player's turn it is.
    else if (message.type === "PLAYER_ACTION") playerActionEvent(message);
    //view logic for un-highlighting which players turn it is
    else if(message.type === "END_PLAYER_ACTION") endPlayerActionEvent(message);
    //view logic for showdown
    else if (message.type === "SHOWDOWN") showdownEvent(message);
    //sends information in order to calculate view logic for all-in equity
    else if(message.type === "CALC_EQUITY") calculateEquityEvent(message);
    //view logic for when the hand has ended
    else if (message.type === "COMPLETE_HAND") completeHandEvent(message);
    //view logic for dealing the flop
    else if (message.type === "BOARD_CARDS") dealBoardCardsEvent(message);
    //view logic for adding on chips
    else if (message.type === "ADD_ON") addOnEvent(message);
    //view logic for cleaning up the table in between streets
    else if(message.type === "CLEAN_UP") cleanUpEvent(message);
    //logic for when error occurred, most likely in payload body
    else console.log("error occurred");
}

function seatsEvent(message) {
    console.log("seats: ", message);
    $("#table-id").val(message.tableID)
    console.log("Globally stored table ID", message.tableID);
    populateTable(message.seats);
}

function sitTableEvent(message) {
    console.log("sit", message);

    //hide the seat button that the player selected
    const seatDiv = $(`#seat-${message.seatIndex}`);
    seatDiv.show();
    seatDiv.empty();

    const playerInfoDiv = $("<div class='player-info'></div>");

    const playerIconDiv = $("<div class='player-icon'></div>");
    playerIconDiv.append(`<img class="icon-img" src="/images/player-icon.png" alt="Player Icon">`);

    const playerPanelDiv = $("<div class='player-panel'></div>");
    playerPanelDiv.append(`<img class="panel-img" src="/images/player-info.png" alt="Player Info">`)
    playerPanelDiv.append(`<p class="player-usernames">${message.player.username}</p>`);
    playerPanelDiv.append(`<p class="player-chip-counts" id="chip-count-${message.seatIndex}">${message.player.chipCount}</p>`);

    const holeCardDiv =$("<div class='hole-cards'></div>");

    const playerActionsDiv = $("<div class='player-actions'></div>");
    playerActionsDiv.append(`<p></p>`);

    playerInfoDiv.append(playerIconDiv);
    playerInfoDiv.append(playerPanelDiv);
    playerInfoDiv.append(holeCardDiv);
    playerInfoDiv.append(playerActionsDiv);

    seatDiv.append(playerInfoDiv);
    isSeated = true;
}
function standTableEvent(message) {
    console.log("stand", message);

    //replaces the player icon with seat button, and then hide it from view
    //we hide it so that seated players can't see or select another seat
    const seatDiv = $(`#seat-${message.seatIndex}`);
    seatDiv.empty();
    seatDiv.append(`<button class="seat-buttons" data-button-number="${message.seatIndex}" onclick="openAddPlayerModal(${message.seatIndex})"><img src="/images/grey-button.png" alt="Error"></button>`)
    if (isSeated === true) {
        seatDiv.hide();
    }
    else seatDiv.show();
}
function moveButtonEvent(message) {
    console.log("move button", message);

    const button = $('#dealer-button')
    switch(message.button) {
        case 0:
            button.css('display', 'flex');
            button.css('top', '18%');
            button.css('left', '63%');
            break;
        case 1:
            button.css('display', 'flex');
            button.css('top', '38%');
            button.css('left', '72%');
            break;
        case 2:
            button.css('display', 'flex');
            button.css('top', '74%');
            button.css('left', '62%');
            break;
        case 3:
            button.css('display', 'flex');
            button.css('top', '74%');
            button.css('left', '42%');
            break;
        case 4:
            button.css('display', 'flex');
            button.css('top', '56%');
            button.css('left', '25%');
            break;
        case 5:
            button.css('display', 'flex');
            button.css('top', '18%');
            button.css('left', '43%');
            break;
    }
}

function initPotEvent(message) {
    console.log("initiate pot", message);

    const oldBBChipCount = parseFloat($(`#chip-count-${message.bigBlind}`).text());
    const newBBChipCount = oldBBChipCount - message.bbAmount;
    $(`#chip-count-${message.bigBlind}`).text(newBBChipCount);


    //show display bet pop up
    //fill it with bet amount
    const bbBetDisplay = $(`#bet-display-${message.bigBlind}`);
    bbBetDisplay.css("display", "flex");
    bbBetDisplay.find('p').text(message.bbAmount);

    const oldSBChipCount = parseFloat($(`#chip-count-${message.smallBlind}`).text());
    const newSBChipCount = oldSBChipCount - message.sbAmount;
    $(`#chip-count-${message.smallBlind}`).text(newSBChipCount);


    //show display bet pop up
    //fill it with bet amount
    const sbBetDisplay = $(`#bet-display-${message.smallBlind}`);
    sbBetDisplay.css("display", "flex");
    sbBetDisplay.find('p').text(message.sbAmount);

    //Display Total Pot text and populate it with the pot size
    const totalPot = $("#total-pot");
    totalPot.css("display", "flex");
    totalPot.text(`Total Pot: ${message.potSize.toLocaleString()}`);
}

function dealHoleCardsEvent(message) {
    //console.log(deal hole cards", message);

    const holeCardsDiv = $(`#seat-${message.seatIndex} .hole-cards`)
    holeCardsDiv.empty();
    holeCardsDiv.append(`<img src="/images/cards/card-back.png" alt="Card 1">`)
    holeCardsDiv.append(`<img src="/images/cards/card-back.png" alt="Card 1">`)
    holeCardsDiv.show();
}

function playerActionEvent(message) {
    //console.log("player action event", message);

    const seatDiv = $(`#seat-${message.seat}`);
    const playerInfoImg = seatDiv.find(".panel-img");

    playerInfoImg.attr("src", "/images/player-info-on-turn.png");
}
function endPlayerActionEvent(message) {
    //console.log("End player action event: ", message);

    //find the player's seat div and unhighlight their player panel
    const seatDiv = $(`#seat-${message.seat}`);
    const playerInfoImg = seatDiv.find(".panel-img");
    playerInfoImg.attr("src", "/images/player-info.png");

    //if they folded
    if(message.action === "F") {
        //hide their cards
        const cardsDiv = seatDiv.find(".hole-cards");
        cardsDiv.hide();

        //creates and sends message to controller
        //allows the user who folded to see their cards on hover until the hand ends
        const request = {
            type: "FOLD",
            username: message.username,
            seat: message.seat
        }
        //sends request to controller to modify the display of the cards for the folded player
        stompClient.send("/app/foldEvent", {}, JSON.stringify(request));
    }
    //if they check
    else if (message.action == "C")  {
        //nothing happens (except for display bubble)
    }
    //if they called (P stands for "pay")
    else if(message.action === "P") {
        //update player's bet display
        const betDisplayDiv = $(`#bet-display-${message.seat}`);
        const betElement = betDisplayDiv.find(".player-bet-display");
        const previousBet = parseFloat(betElement.text());
        const newBet = previousBet + message.bet;
        betElement.text(newBet);
        betDisplayDiv.show();

        //update player's chip count
        const chipCountElement = seatDiv.find(".player-chip-counts");
        chipCountElement.text(message.stackSize);

        //update the pot size
        const potElement = $("#total-pot");
        potElement.text("Total Pot: " + message.potSize);
    }
    //if they bet
    else if (message.action === "B") {
        //update player's bet display
        const betDisplayDiv = $(`#bet-display-${message.seat}`);
        const betElement = betDisplayDiv.find(".player-bet-display");
        betElement.text(message.bet);
        betDisplayDiv.show();

        //update player's chip count
        const chipCountElement = seatDiv.find(".player-chip-counts");
        chipCountElement.text(message.stackSize);

        //update the pot size
        const potElement = $("#total-pot");
        potElement.text("Total Pot: " + message.potSize);
    }
    else if (message.action === "A") {
        //update player's bet display
        const betDisplayDiv = $(`#bet-display-${message.seat}`);
        const betElement = betDisplayDiv.find(".player-bet-display");
        //const allInAmount = parseFloat(betElement.text()) + message.bet;
        betElement.text(message.bet);

        //update player's chip count
        const chipCountElement = seatDiv.find(".player-chip-counts");
        chipCountElement.text(message.stackSize);

        //update the pot size
        const potElement = $("#total-pot");
        potElement.text("Total Pot: " + message.potSize);
    }
    else {
        console.log("Error occurred in END PLAYER ACTION")
    }
    //display and/or update the current street pot size:
    if(message.currentStreetPotSize > 0 && message.preFlop === false) {
        $("#csp-text").text(`$ ${message.currentStreetPotSize}`);
        $("#current-street-pot").css("display", "flex");
    }

    //display an action bubble under the player icon briefly displaying what action the user took
    displayActionBubble(message.seat, message.action);
}

function displayActionBubble(seat, action) {
    const seatDiv = $(`#seat-${seat}`);
    const playerActionsDiv = seatDiv.find(".player-actions");
    const pTag = playerActionsDiv.find("p");

    switch(action) {
        case "F":
            pTag.text("Fold");
            playerActionsDiv.css("background-color", "grey");
            break;
        case "C":
            pTag.text("Check");
            playerActionsDiv.css("background-color", "grey");
            break;
        case "P":
            pTag.text("Call");
            playerActionsDiv.css("background-color", "blue");
            break;
        case "B":
            pTag.text("Bet")
            playerActionsDiv.css("background-color", "orange");
            break;
        case "A":
            pTag.text("All-In")
            playerActionsDiv.css("background-color", "red");
            break;
            //TODO create a function that displays an all in button on the table
    }

    //display the action bubble on the screen
    playerActionsDiv.css("display", "block");

    // Use setTimeout to revert the changes after one second (1000 milliseconds)
    setTimeout(function() {
        //stops displaying the action bubble, resets the text to empty
        playerActionsDiv.css("display", "none");
        pTag.text("");
    }, 1000);
}

//displays the whole cards of the players still in the hand
function showdownEvent(message) {
    //console.log("SHOWDOWN", message);

    //loops through hash map and publicly displays the hole cards of each player currently in the hand
    for (let index in message.indexAndPlayer) {
        const holeCardsDiv = $(`#seat-${index} .hole-cards`)
        holeCardsDiv.empty();
        holeCardsDiv.append(`<img src="/images/cards/${message.indexAndPlayer[index].holeCards[0]}.png" alt="Card 1">`)
        holeCardsDiv.append(`<img src="/images/cards/${message.indexAndPlayer[index].holeCards[1]}.png" alt="Card 1">`)
        holeCardsDiv.show();
    }
}

//calculate equity of each player using the API and then displays each player's equity by their hand
function calculateEquityEvent(message) {
    //console.log("Calculate equity event: ", message);



}

//Completes closing actions after the hand has ended
function completeHandEvent(message) {
    console.log("Complete Hand message: ", message);

    //hides the bet displays for each player
    hideBetDisplays();

    //Find the winners
    //Change their player icon to display the updated stack size
    for(let index in message.indexAndPlayer) {
        if(message.indexAndPlayer.hasOwnProperty(index)) {
            $(`#chip-count-${index}`).text(message.indexAndPlayer[index].chipCount);
            console.log("Updated chip count");
        }
    }

    //Set Total Pot display = 0;
    $("#total-pot").text("Total Pot: 0")

    // Execute winner animations and then proceed to the next loop
    animateWinners(message.indexAndPlayer).then(() => {
        //Remove each player's hole cards
        for (let i = 0; i < 6; i++) {
            const seatDiv = $(`#seat-${i}`);
            const holeCardsDiv = seatDiv.find(".hole-cards");
            holeCardsDiv.empty();
        }
        //Clear the board
        $("#board").empty();
        //Clear and hide current pot size display
        $("#csp-text").text();
        $("#current-street-pot").css("display", "none");
    });
}

// Create a Promise for the winner animations
function animateWinners(indexAndPlayer) {
    return new Promise(resolve => {
        const winnerPromises = Object.keys(indexAndPlayer).map(index => {
            return new Promise(winnerResolve => {
                const seatDiv = $(`#seat-${index}`);
                const playerActionsDiv = seatDiv.find(".player-actions");
                const pTag = playerActionsDiv.find("p");

                pTag.text("WINNER");
                playerActionsDiv.css("background-color", "green")
                //Display the action bubble on the screen
                playerActionsDiv.css("display", "block");

                // Use setTimeout to revert the changes after three seconds (3000 milliseconds)
                setTimeout(function() {
                    // Stops displaying the action bubble, resets the text to empty
                    playerActionsDiv.css("display", "none");
                    pTag.text("");
                    winnerResolve(); // Resolve the individual winner animation
                }, 3000);

            });
        });

        // Wait for all winner animations to complete
        Promise.all(winnerPromises).then(() => {
            resolve(); // Resolve the parent promise once all winners are done
        });
    });
}

//Reset and hide the bet displays and current street pot display
function hideBetDisplays() {
    for(let i = 0; i < 5; i++) {
        const betDisplayDiv = $(`#bet-display-${i}`);
        const betElement = betDisplayDiv.find(".player-bet-display");
        betElement.text("");
        betDisplayDiv.hide();
    }
    $("#csp-text").text();
    $("#current-street-pot").css("display", "none");
}

//Displays the flop cards
function dealBoardCardsEvent(message) {
    //console.log("Deal hole cards event", message);

    for (let i = 0; i < message.cards.length; i++) {
        const flopCard = new  $('<img>');
        flopCard.attr("src", `/images/cards/${message.cards[i]}.png`);
        flopCard.attr("alt", `Board Card ${i}`);

        $("#board").append(flopCard);
    }
}

function addOnEvent(message) {
    //console.log("Add on: ", message);

    const seatDiv = $(`#seat-${message.seatIndex}`);
    const chipCountElement = seatDiv.find(".player-chip-counts");
    chipCountElement.text(message.rebuyAmount);
}

//cleans up view information between hands
function cleanUpEvent(message) {
    //console.log("Clean up event", message);

    //hides the bet displays for each player
    if (message.handOver === true) {
        $("#total-pot").css("display", "none")
        $("#total-pot").text(`Total Pot: `)
    }
    hideBetDisplays();
}

/*HANDLE TABLE DATA SUBMISSION
/*-----------------------------------*/

//Function to handle the "Setup" button click
function submitTableData() {
    //parse data
    const gameType = $('#gameType').val();
    const bigBlind = parseInt($('#bigBlind').val());
    const smallBlind = parseInt($('#smallBlind').val());

    if (gameType && !isNaN(bigBlind) && !isNaN(smallBlind)) {
        //create Table object
        const tableData = {
            gameType: gameType,
            stakes: [smallBlind, bigBlind]
        };

        //fetches setTableData() method in TableController
        fetch('/setTableData', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(tableData)
        })
            .then(response => {
                // Check if the response is successful (status code 2xx)
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                // Handle the response from the server if needed
            })
            .catch(error => {
                console.error('Error occurred:', error);
            });

        // Populate the <h4> headers with the selected game type and stakes
        $('#game-type').text(`Game Type: ${gameType}`);
        $('#stakes').text(`Stakes: ${smallBlind}/${bigBlind}`);

        // Close the modal after processing the data
        closeGameSetupModal();
    }
}

function populateTable(seats) {
    //populate view with either seat button or player icon
    for (let i = 0; i < seats.length; i++) {
        //if seat is empty, show join seat button
        if (seats[i] === null)  {
            const seatDiv = $(`#seat-${i}`);
            seatDiv.show();
        }
        //if seat is taken, show player icon with correct player information
        else {
            const seatDiv = $(`#seat-${i}`);
            seatDiv.empty();

            const playerInfoDiv = $("<div class='player-info'></div>");

            const playerIconDiv = $("<div class='player-icon'></div>");
            playerIconDiv.append(`<img class="icon-img" src="/images/player-icon.png" alt="Player Icon">`);
            const playerPanelDiv = $("<div class='player-panel'></div>");
            playerPanelDiv.append(`<img class="panel-img" src="/images/player-info.png" alt="Player Info">`)
            playerPanelDiv.append(`<p class="player-usernames">${seats[i].username}</p>`);
            playerPanelDiv.append(`<p class="player-chip-counts" id="chip-count-${i}">${seats[i].chipCount}</p>`);

            const holeCardDiv =$("<div class='hole-cards'></div>");

            const playerActionsDiv = $("<div class='player-actions'></div>");
            playerActionsDiv.append(`<p></p>`);

            playerInfoDiv.append(playerIconDiv);
            playerInfoDiv.append(playerPanelDiv);
            playerInfoDiv.append(holeCardDiv);
            playerInfoDiv.append(playerActionsDiv);

            seatDiv.append(playerInfoDiv);
        }
    }
}



/*OPEN AND CLOSE MODALS
/*-----------------------------------*/
// Function to show the game setup modal
function openGameSetupModal() {
    $("#gameSetupModal").show();
}

// Function to close the game setup modal
function closeGameSetupModal() {
    $("#gameSetupModal").hide();
}



/*HANDLE USER EXITING PAGE
/*-----------------------------------*/

//Handle user leaving the page
$(window).on('beforeunload', function() {
    // Disconnect WebSocket connection
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log('WebSocket connection disconnected');
    }
});