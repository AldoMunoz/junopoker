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
        //get the seat the user selected
        const seat = currentButtonNumber;

        //create payload that will be sent to the backend
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
            //if fetch call is successful, the following logic wil execute:

            //method used to subscribe user to player-events topic
            subscribeToPlayerTopic(usernameInput, player, seat);

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
function subscribeToPlayerTopic(usernameInput, player) {
    // Subscribe to the player-specific topic
    //all messages sent to /topic/playerEvents/${} will be redirected to "playerEvents(payload) method below"
    stompClient.subscribe(`/topic/playerEvents/${usernameInput}`, playerEvents);
    isSeated = true;

    if(player && stompClient) {
        //create PlayerRequest object to be sent to the front end
        let playerRequest = {
            type: "SIT",
            player: player,
            seat: currentButtonNumber
        };
        //sends message to all users that a player has taken a seat
        stompClient.send("/app/tableEvents", {}, JSON.stringify(playerRequest));
        //sends message to the player themselves, used to hide other seat buttons
        stompClient.send("/app/playerEvents", {}, JSON.stringify(playerRequest));
    }
    else console.log("Something went wrong before Websocket could send")
}


//handle all messages sent to /topic/playerEvents/${}
async function playerEvents(payload) {
    //parse message body
    let message = JSON.parse(payload.body);

    //logic for when player takes a seat
    if(message.type == "SIT") {
        //fetch array of table seats
        const seats = await fetchTableSeats();

        //loop to hide all seat buttons once the player sits
        let seatedPlayerCount = 0;
        for (let i = 0; i < seats.length; i++) {
            if (seats[i] === null) {
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
        settingsBar.attr("data-seat", message.seat);

        if(seatedPlayerCount > 1) {
           const response = await fetch("/startGame");
           const table = await response.json();
           stompClient.send("/app/startGame", {}, JSON.stringify(table))
        }
    }
    //logic for when player stands from table
    else if(message.type === "STAND") {
        const seats = await fetchTableSeats();
        populateTable(seats);

        //hide settings bar
        const settingsBar = $('.settings-bar');
        settingsBar.css("display", "none");
        //unsubscribe user for /topic/playerEvents/${}
        stompClient.unsubscribe(`/topic/playerEvents/${message.player.username}`);
    }
}

// Function to open the player modal
function openAddPlayerModal(buttonNumber) {
    // Show the modal
    $("#addUserModal").show();
    // Set a data attribute on the modal to store the button number
    currentButtonNumber = buttonNumber;
    console.log("Seat value in openAddPlayerModal", currentButtonNumber);
}
// Function to close the player modal
function closeAddPlayerModal() {
    // Hide the modal
    $("#addUserModal").hide();
}