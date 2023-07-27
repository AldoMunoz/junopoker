//Script that prompts a user for the name and chip count when they click a seat on the table
function showModal() {
    const modal = document.getElementById('myModal');
    modal.style.display = 'block';
}

function closeModal() {
    const modal = document.getElementById('myModal');
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

    //TODO throw error for invalid input
    if (name && chipCount) {
        const data = {
            name: name,
            chipCount: chipCount,
        };
        fetch('http://localhost:8080/poker', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response => {
                // Handle the server response if needed
                closeModal();
            }).catch(error => {
                // Handle any errors that occurred during the request
            });
    }
}

const promptButtons = document.querySelectorAll('.overlay-button');
promptButtons.forEach(button => button.addEventListener('click', handleButtonClick));

const closeBtn = document.querySelector('.close');
closeBtn.addEventListener('click', closeModal);

const submitBtn = document.getElementById('submitBtn');
submitBtn.addEventListener('click', handleSubmit);