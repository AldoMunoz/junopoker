let currentButtonNumber;
let stompClient;

// Function to establish WebSocket connection and handle subscription
function establishWebSocketConnection(usernameInput, player, seat) {
    stompClient = Stomp.over(new SockJS('/ws'))
    stompClient.connect({}, function(frame) {
        // Subscribe to the player-specific topic
        stompClient.subscribe(`/topic/player-events/${usernameInput}`, function (event) {
            console.log("Websocket connection established for seated player.");

            // Send a message to add the user/player to the table
            stompClient.send("/app/add-user",
                {},
                JSON.stringify({player: player, seat: seat}));

            // Handle the received player event here
            const playerEvent = JSON.parse(event.body);
            // You can update your UI or perform other actions based on playerEvent
        });
    });
}

// Function to open the player modal
function openAddPlayerModal(buttonNumber) {
    // Show the modal
    $("#addUserModal").show();
    // Set a data attribute on the modal to store the button number
    currentButtonNumber = buttonNumber;
}

// Function to close the modal
function closeAddPlayerModal() {
    // Hide the modal
    $("#addUserModal").hide();
}

// Function to submit data from the modal
function submitData() {
    const usernameInput = $("#username").val().trim();
    const chipCountInput = parseInt($("#chipCount").val());

    if (usernameInput && chipCountInput) {
        const player = {
            username: usernameInput,
            chipCount: chipCountInput,
        };
        const seat = currentButtonNumber - 1;

        const data = {
            player: player,
            seat: seat
        }

        // Make a Fetch API POST request to your Spring Boot controller
        fetch('/createPlayer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => {
            // Check if the response is successful (status code 2xx)
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
            // Establish WebSocket connection and handle subscription
            establishWebSocketConnection(usernameInput, player, seat);

            // If the request is successful, change the image in the button
            /*const seatDiv = $(`#seat-${currentButtonNumber}`);
            seatDiv.empty();
            const newDiv = $("<div class='player-info'></div>");
            newDiv.append(`<p class="player-usernames">${usernameInput}</p>`);
            newDiv.append(`<p class="player-chip-counts">${chipCountInput}</p>`);
            newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
            seatDiv.append(newDiv);*/
            // Close the modal
            closeAddPlayerModal();
        }).catch(error => {
            console.error('Error occurred:', error);
        });
    }
}