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

        $.ajax({
            url: '/createTable',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(tableData),
            success: function (data) {
                // Handle the response from the server if needed
            },
            error: function (error) {
                console.error('Error occurred:', error);
            }
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
        showGameSetupModal();
    }
});