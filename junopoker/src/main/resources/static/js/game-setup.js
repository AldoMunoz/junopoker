// Function to show the game setup modal
function showGameSetupModal() {
    $("#gameSetupModal").show();
}

// Function to close the game setup modal
function closeGameSetupModal() {
    $("#gameSetupModal").hide();
}

// Function to handle the "Start Game" button click
function handleSetupButtonClick() {
    const gameType = $('#gameType').val();
    const bigBlind = parseInt($('#bigBlind').val());
    const smallBlind = parseInt($('#smallBlind').val());

    if (gameType && !isNaN(bigBlind) && !isNaN(smallBlind)) {
        const tableData = {
            gameType: gameType,
            stakes: [smallBlind, bigBlind]
        };

        fetch('/createTable', {
            method: 'POST',
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

// Add an event listener to the "Start Game" button
$('#setupBtn').on('click', handleSetupButtonClick);

// Show the game setup modal when the page loads, but only if no data is saved in localStorage
$(window).on('load', function () {
    const savedData = localStorage.getItem('gameSetup');
    if (!savedData) {
        const tableData = {
            gameType: "Texas Hold'em",
            stakes: [1, 2]
        };
        fetch('/createTable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(tableData)
        }).then(response => {
                // Check if the response is successful (status code 2xx)
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            }).then(data => {
                // Handle the response from the server if needed
                // Now that the table is created, establish the WebSocket connection
                const socket = new SockJS('/ws');
                const stompClient = Stomp.over(socket);

                stompClient.connect({}, function (frame) {
                    console.log('WebSocket connection established:', frame);
                    stompClient.subscribe('/topic/poker-events', function (event) {
                        const pokerEvent = JSON.parse(event.body);
                        // Handle the received poker event here for spectators
                    });
                });
            }).catch(error => {
                console.error('Error occurred:', error);
            });
        // Populate the <h4> headers with the selected game type and stakes
        $('#game-type').text(`Game Type: Texas Hold'em`);
        $('#stakes').text(`Stakes: 1/2`);
    }
    else {
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('WebSocket connection established:', frame);

            stompClient.subscribe('/topic/poker-events', function (event) {
                const pokerEvent = JSON.parse(event.body);
                // Handle the received poker event here for spectators
            });
        });
    }
});