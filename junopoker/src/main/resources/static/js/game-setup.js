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
    //check if table exists
    fetch("checkTableData")
        .then(response => response.json())
        .then(data=> {
            //if it does, create a websocket connection for user
            if(data.status === "exists") {
                console.log("Table exists in HttpSession");
                //fetches table data to populate headers
                fetch("/getTableData")
                    .then(response => response.json())
                    .then(tableData => {
                        $('#game-type').text(`Game Type: ${tableData.gameType}`);
                        $('#stakes').text(`Stakes: ${tableData.stakes[0]}/${tableData.stakes[1]}`);
                    })
                establishWebSocketConnection();
            }
            //if not create a default table (1/2 NLH)
            else if (data.status === "notExists") {
                console.log("Table data does not exist in HttpSession, creating table...");
                //creates default table object
                const defaultTableData = {
                    gameType: "Texas Hold'em",
                    stakes: [1, 2]
                };
                //store the table data in the HttpSession
                fetch("/storeTableData", {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(defaultTableData)
                }).then(() => {
                    console.log("Table data created and stored in HttpSession");
                    // Establish WebSocket connection
                    establishWebSocketConnection();

                    // Populate the <h4> headers with the selected game type and stakes
                    $('#game-type').text(`Game Type: Texas Hold'em`);
                    $('#stakes').text(`Stakes: 1/2`);
                }).catch(error => {
                    console.error('Error occurred while storing table data:', error);
                });
            }
        })
        .catch(error => {
            console.error('Error occurred while checking table data:', error);
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

    //seats is array of table seats
    const seats = await fetchTableSeats();
    //populate the table view with players
    populateTable(seats);
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

    //view logic for when a new player takes a seat
    if(message.type === "SIT") sitTableEvent(message);

    //view logic for when player stands up from seat
    else if(message.type === "STAND") standTableEvent(message);
    //view logic for when blinds are moved
    else if(message.type === "MOVE_BLINDS") {

    }
    //logic for when error occurred, most likely in payload body
    else console.log("error occurred");

}

function sitTableEvent(message) {
    //hide the seat button that the player selected
    const seatDiv = $(`#seat-${message.seat}`);
    seatDiv.show();
    seatDiv.empty();
    //replace that with a player icon
    const newDiv = $("<div class='player-info'></div>");
    newDiv.append(`<p class="player-usernames">${message.player.username}</p>`);
    newDiv.append(`<p class="player-chip-counts">${message.player.chipCount}</p>`);
    newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
    seatDiv.append(newDiv);
}
function standTableEvent(message) {
    //replace the player icon with seat button, and then hide it from view
    //we hide it so that seated players can't see or select another seat
    const seatDiv = $(`#seat-${message.seat}`);
    seatDiv.empty();
    seatDiv.append(`<button class="seat-buttons" data-button-number="${message.seat}" onclick="openAddPlayerModal(${message.seat})"><img src="/images/grey-button.png" alt="Error"></button>`)
    if (isSeated === true) {
        seatDiv.hide();
    }
    else seatDiv.show();
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



/*ASYNCHRONOUS FUNCTIONS
/*-----------------------------------*/

//asynchronous function that calls TableController method getSeats()
//fetches and returns array of table seats
async function fetchTableSeats() {
    try {
        const response = await fetch("/getSeats");
        const seats = await response.json();
        return seats;
    } catch (error) {
        console.error('Error occurred while fetching table seats:', error);
        return [];
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
            const newDiv = $("<div class='player-info'></div>");
            newDiv.append(`<p class="player-usernames">${seats[i].username}</p>`);
            newDiv.append(`<p class="player-chip-counts">${seats[i].chipCount}</p>`);
            newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
            seatDiv.append(newDiv);
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