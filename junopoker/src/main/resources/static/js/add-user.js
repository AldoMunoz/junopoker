//Script that prompts a user for the name and chip count when they click a seat on the table
function showModal() {
    const modal = document.getElementById('addUserModal');
    modal.style.display = 'block';
}

function closeModal() {
    const modal = document.getElementById('addUserModal');
    modal.style.display = 'none';
}

function handleButtonClick() {
    showModal();
}

function handleSubmit() {
    const nameInput = document.getElementById('name');
    const chipCountInput = document.getElementById('chipCount');
    const name = nameInput.value.trim();
    const chipCount = chipCountInput.value.trim();

    // TODO throw error for invalid input
    if (name && chipCount) {
        const data = {
            name: name,
            chipCount: chipCount,
        };

        // Assuming you have the 'seatId' attribute set for each button (e.g., 'seat-1', 'seat-2', etc.)
        const seatId = document.getElementById('addUserModal').dataset.seatId;
        const seatButton = document.querySelector(`[data-seat-id="${seatId}"]`);

        fetch('http://localhost:8080/poker', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => {
            // Handle the server response if needed

            // Update the button image with the new image source
            seatButton.innerHTML = `<img src="/images/player-icon.png" alt="Error">`;

            closeModal();
        }).catch(error => {
            // Handle any errors that occurred during the request
        });
    }
}

const promptButtons = document.querySelectorAll('.seat-buttons');
promptButtons.forEach(button => button.addEventListener('click', handleButtonClick));

const closeBtn = document.querySelector('.close');
closeBtn.addEventListener('click', closeModal);

const submitBtn = document.getElementById('submitBtn');
submitBtn.addEventListener('click', handleSubmit);