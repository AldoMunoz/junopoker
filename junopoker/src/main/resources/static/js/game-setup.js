// Function to show the game setup modal
function showGameSetupModal() {
    const modal = document.getElementById('gameSetupModal');
    modal.style.display = 'block';
}

// Function to close the game setup modal
function closeGameSetupModal() {
    const modal = document.getElementById('gameSetupModal');
    modal.style.display = 'none';
}

// Function to handle the "Start Game" button click
function handleSetupButtonClick() {
    const gameTypeInput = document.getElementById('gameType');
    const bigBlindInput = document.getElementById('bigBlind');
    const smallBlindInput = document.getElementById('smallBlind');

    const gameType = gameTypeInput.value;
    const bigBlind = parseInt(bigBlindInput.value);
    const smallBlind = parseInt(smallBlindInput.value);

    if (gameType && !isNaN(bigBlind) && !isNaN(smallBlind)) {
        // Save the game setup information to localStorage
        const tableData = {
            gameType: gameType,
            stakes: [smallBlind, bigBlind]
        };
        localStorage.setItem('gameSetup', JSON.stringify(tableData));

        fetch('/createTable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(tableData),
        }).then(response => {
            // Check if the response is successful (status code 2xx)
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        }).then(data => {
                // Handle the response from the server if needed
        }).catch(error => {
                console.error('Error occurred:', error);
        });

        // Populate the <h4> headers with the selected game type and stakes
        const gameTypeHeader = document.getElementById('game-type');
        const stakesHeader = document.getElementById('stakes');

        gameTypeHeader.textContent = `Game Type: ${gameType}`;
        stakesHeader.textContent = `Stakes: ${smallBlind}/${bigBlind}`;

        // Close the modal after processing the data
        closeGameSetupModal();
    }
}

// Add an event listener to the "Start Game" button
const setupBtn = document.getElementById('setupBtn');
setupBtn.addEventListener('click', handleSetupButtonClick);

// Check if game setup data exists in localStorage and populate the headers accordingly
document.addEventListener('DOMContentLoaded', () => {
    const savedData = localStorage.getItem('gameSetup');
    if (savedData) {
        const data = JSON.parse(savedData);
        const gameTypeHeader = document.getElementById('game-type');
        const stakesHeader = document.getElementById('stakes');

        gameTypeHeader.textContent = `Game Type: ${data.gameType}`;
        stakesHeader.textContent = `Stakes: ${data.smallBlind}/${data.bigBlind}`;
    }
});

// Show the game setup modal when the page loads, but only if no data is saved in localStorage
window.onload = () => {
    const savedData = localStorage.getItem('gameSetup');
    if (!savedData) {
        showGameSetupModal();
    }
};