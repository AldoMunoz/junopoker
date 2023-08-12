'use strict'
// Global Variables
let currentButtonNumber = -1;
let stompClient = null;

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


//Create a new WebSocket connection
function establishWebSocketConnection() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

//Subscribe user to "seated-players" and "poker-events"
async function onConnected() {
    stompClient.subscribe("/topic/seatedPlayers", seatedPlayers);
    stompClient.subscribe("/topic/pokerEvents", pokerEvents);

    const seats = await fetchTableSeats();
    console.log(seats);

    for (let i = 0; i < seats.length; i++) {
        if (seats[i] === null) ;
        else {
            const seatDiv = $(`#seat-${i + 1}`);
            seatDiv.empty();
            const newDiv = $("<div class='player-info'></div>");
            newDiv.append(`<p class="player-usernames">${seats[i].username}</p>`);
            newDiv.append(`<p class="player-chip-counts">${seats[i].chipCount}</p>`);
            newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
            seatDiv.append(newDiv);
        }
    }

    console.log("Subscribed user to 'seated-players' and 'poker-events'")
}

async function fetchTableSeats() {
    try {
        const response = await fetch("/getSeats");
        const seats = await response.json();
        return seats;
    } catch (error) {
        console.error('Error occurred while fetching table seats:', error);
        return []; // Return an empty array or handle the error appropriately
    }
}

//Displays error message if WebSocket Connection is unsuccessful
function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!')
}

//Handle seated Player Payloads
async function seatedPlayers(payload) {
    let message = JSON.parse(payload.body);

    if(message.type === "SIT") {
        const seatDiv = $(`#seat-${message.seat}`);
        seatDiv.show();
        seatDiv.empty();
        const newDiv = $("<div class='player-info'></div>");
        newDiv.append(`<p class="player-usernames">${message.player.username}</p>`);
        newDiv.append(`<p class="player-chip-counts">${message.player.chipCount}</p>`);
        newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
        seatDiv.append(newDiv);
    }
    else if(message.type === "STAND") {
        //TODO
    }
    else {
        console.log("error occurred")
    }


    /*
    let seats = await fetchTableSeats();

    for (let i = 0; i < seats.length; i++) {
        if (seats[i] === null) {
            //Get rid of all the other seat buttons
            const seatDiv = $(`#seat-${i+1}`);
            seatDiv.hide();
        }
        else {
            const seatDiv = $(`#seat-${i+1}`);
            seatDiv.show();
            seatDiv.empty();
            const newDiv = $("<div class='player-info'></div>");
            newDiv.append(`<p class="player-usernames">${seats[i].username}</p>`);
            newDiv.append(`<p class="player-chip-counts">${seats[i].chipCount}</p>`);
            newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
            seatDiv.append(newDiv);
        }
    }*/
}
function pokerEvents(payload) {
    console.log("Entered pokerEvents with payload.")
    let message = JSON.parse(payload.body);
}

//Stores player data in an object
//Stores the Player in the Table
//creates Websocket subscription specifically for that player
function submitPlayerData() {
    const usernameInput = $("#username").val().trim();
    const chipCountInput = parseInt($("#chipCount").val());

    if (usernameInput && chipCountInput) {
        const player = {
            username: usernameInput,
            chipCount: chipCountInput,
            holeCards: null,
            hand: null,
            inHand: false,
            currentBet: 0,
            isActive: false
        };
        const seat = currentButtonNumber - 1;

        const data = {
            player: player,
            seat: seat
        }
        // Make a Fetch API POST request to your Spring Boot controller
        fetch("/createPlayer", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => {
            // Check if the response is successful (status code 2xx)
            if (!response.ok) {
                throw new Error('Network response was not ok');
                console.log("error occurred creating player")
            }
            return response.json();
        }).then(data => {
            //method used to subscribe user to player-events topic
            subscribeToPlayerTopic(usernameInput, player, seat);

            // Close the modal
            closeAddPlayerModal();
        }).catch(error => {
            console.error('Error occurred:', error);
        });
    }
}

//Subscribe user to player-events/${username}
//Send data to "addUser" about new player
function subscribeToPlayerTopic(usernameInput, player) {
    // Subscribe to the player-specific topic
    stompClient.subscribe(`/topic/playerEvents/${usernameInput}`, playerEvents);

    if(player && stompClient) {
        let playerRequest = {
            type: "SIT",
            player: player,
            seat: currentButtonNumber
        };
        stompClient.send("/app/addUser", {}, JSON.stringify(playerRequest));
        stompClient.send("/app/hideSitButtons", {}, JSON.stringify());
    }
    else console.log("Something went wrong before Websocket could send")
}

function playerEvents(payload) {
    //TODO
}


//Function to handle the "Setup" button click
function submitTableData() {
    const gameType = $('#gameType').val();
    const bigBlind = parseInt($('#bigBlind').val());
    const smallBlind = parseInt($('#smallBlind').val());

    if (gameType && !isNaN(bigBlind) && !isNaN(smallBlind)) {
        const tableData = {
            gameType: gameType,
            stakes: [smallBlind, bigBlind]
        };

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

/*Open and Close Modals*/

// Function to show the game setup modal
function openGameSetupModal() {
    $("#gameSetupModal").show();
}

// Function to close the game setup modal
function closeGameSetupModal() {
    $("#gameSetupModal").hide();
}
// Function to open the player modal
function openAddPlayerModal(buttonNumber) {
    // Show the modal
    $("#addUserModal").show();
    // Set a data attribute on the modal to store the button number
    currentButtonNumber = buttonNumber;
}
// Function to close the player modal
function closeAddPlayerModal() {
    // Hide the modal
    $("#addUserModal").hide();
}
//Handle user leaving the page
$(window).on('beforeunload', function() {
    // Disconnect WebSocket connection
    if (stompClient !== null) {
        stompClient.disconnect();
        console.log('WebSocket connection disconnected');
    }
});