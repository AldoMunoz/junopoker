let currentButtonNumber;
function openAddPlayerModal(buttonNumber) {
    // Show the modal
    $("#addUserModal").css("display", "block");
    // Set a data attribute on the modal to store the button number
    currentButtonNumber = buttonNumber;
}
// Function to close the modal
function closeAddPlayerModal() {
    // Hide the modal
    $("#addUserModal").css("display", "none");
}
// Function to submit data from the modal
function submitData() {
    const usernameInput = $("#username").val().trim();
    const chipCountInput = parseInt($("#chipCount").val());

    if (usernameInput && chipCountInput) {
        const playerData = {
            username: usernameInput,
            chipCount: chipCountInput,
        };

        // Make a Fetch API POST request to your Spring Boot controller
        fetch('/createPlayer', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(playerData)
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
            /*
            const button = $(`.seat-buttons[data-button-number="${currentButtonNumber}"]`);
            const img = button.find("img");
            img.attr("src", "/images/player-icon.png");
            */

            // Close the modal
            closeAddPlayerModal();
        }).catch(error => {
            console.error('Error occurred:', error);
        });
    }
}
