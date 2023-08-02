let currentButtonNumber;
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
        const seat = currentButtonNumber-1;

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
            // If the request is successful, change the image in the button
            const seatDiv = $(`#seat-${currentButtonNumber}`);
            seatDiv.empty();
            const newDiv = $("<div class='player-info'></div>");
            newDiv.append(`<p class="player-usernames">${usernameInput}</p>`);
            newDiv.append(`<p class="player-chip-counts">${chipCountInput}</p>`);
            newDiv.append(`<img src="/images/player-icon.png" alt="Player Icon">`);
            seatDiv.append(newDiv);
            // Close the modal
            closeAddPlayerModal();
        }).catch(error => {
            console.error('Error occurred:', error);
        });
    }
}
