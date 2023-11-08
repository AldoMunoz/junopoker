const sliderContainer = $(".slider-container");
const sliderHandle = $(".slider-handle");
const sliderValue = $("#slider-value");

let isDragging = false;
let minValue = 0;
let maxValue = 0;
let smallBlind = 0;
let lastSliderPercentage = 0;
let potSize = 0;
let seatNo = -1;
let betButtonType = ''

function getSliderPercentage(event) {
    const containerRect = sliderContainer[0].getBoundingClientRect();
    const sliderWidth = sliderHandle.outerWidth();
    const offsetX = event.clientX - containerRect.left - sliderWidth / 2;
    const trackWidth = containerRect.width - sliderWidth;
    let percentage = (offsetX / trackWidth) * 100;

    percentage = Math.min(100, Math.max(0, percentage));
    lastSliderPercentage = percentage;

    return Math.max(0, Math.min(100, percentage));
}

//updates the slider position depending on either where the user is dragging, or what the value of the input box is
function updateSlider(percentage) {
    const handlePosition = `${percentage}%`;
    sliderHandle.css("left", handlePosition);
    sliderContainer.find(".slider-track").css("width", `${percentage}%`);

    lastSliderPercentage = percentage;

    const bet = getBetValueFromPercentage(percentage);
    return bet;
}

//Converts a given bet value to a percentage of the max bet that player can make
function getBetValueFromPercentage(percentage) {
    const betValue = minValue + (percentage/100) * (maxValue - minValue);
    const formattedValue = betValue.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 });

    return formattedValue;
}

//changes the text in the "Bet" or the "Raise" button
function updateBetButton(bet) {
    if (betButtonType === 'b') $("#bet").text(`Bet: ${bet}`);
    else if (betButtonType === 'r') $("#raise").text(`Raise: ${bet}`);
    else $("#raise").text("ERROR");
}

//changes the value in the input box
function updateInputBox(bet) {
    sliderValue.val(bet);
}

//increments the slider and bet button by 1 small blind upon click
function incrementBet() {
    // Get the current slider percentage
    let newPercentage = lastSliderPercentage;

    // Calculate the new slider percentage by adding smallBlind
    newPercentage += (smallBlind * 100 / (maxValue - minValue));

    // Ensure that the new percentage stays within bounds (0% to 100%)
    newPercentage = Math.min(100, Math.max(0, newPercentage));

    // Update the slider position and value
    const bet = updateSlider(newPercentage);
    updateBetButton(bet);
    updateInputBox(bet);
}

//decrements the slider and bet button by 1 small blind upon click
function decrementBet() {
    // Get the current slider percentage
    let newPercentage = lastSliderPercentage;

    // Calculate the new slider percentage by subtracting smallBlind
    newPercentage -= (smallBlind * 100 / (maxValue - minValue));

    // Ensure that the new percentage stays within bounds (0% to 100%)
    newPercentage = Math.min(100, Math.max(0, newPercentage));

    lastSliderPercentage = newPercentage;

    // Update the slider position and value
    const bet = updateSlider(newPercentage);
    updateBetButton(bet);
    updateInputBox(bet);
}

//Changes the buttons and sliders when the user clicks on one of the default bet sizes
function changeBetSize(size) {
    const betSize = (size/100)*potSize;

    if(betSize > minValue) {
        const newPercentage = ((betSize - minValue) / (maxValue - minValue)) * 100;
        const bet = updateSlider(newPercentage);
        updateBetButton(bet);
        updateInputBox(bet);
    }
}

//handles when user manually inputs a bet value
function onInputChange(input) {
    let inputArray = input.split('.');

    //Check and corrects the input box if there is more than one period
    let duplicate = false;
    if (inputArray.length > 2) {
        duplicate = true;

        // Remove the last element (the extra period)
        inputArray.pop();

        // Join the array elements back together with periods
        input = inputArray.join('.');
    }

    //if the input in the box is less than the minimum value a user can bet:
    if(input < minValue) {
        //set slider to 0, adjust buttons accordingly
        const bet= updateSlider(0);
        updateBetButton(bet);
        //I think this checks if the input amount has not changed
        //so if the user input changes from "2" to "2.", do nothing to the slider or the bet button
        if(duplicate) updateInputBox(input);
    }
    //if the input value is greater than the maximum value a user can bet:
    else if(input > maxValue) {
        //default the input back down to the max value
        const bet = updateSlider(100);
        updateBetButton(bet);
        updateInputBox(bet);
    }
    //if the input hasn't changed, do nothing
    //for example if user changed input from "4.00" to "4.0"
    else if(input == (lastSliderPercentage / 100) * (maxValue - minValue) + minValue) {
        if (duplicate) updateInputBox(input);
    }
    //else, just update the slider and the bet button to show the correct values
    else {
        const percentage = ((input - minValue) / (maxValue - minValue)) * 100;
        const bet = updateSlider(percentage);
        updateBetButton(bet);
        updateInputBox(bet);
    }
}

//Sends a response to the back-end after the user clicks "fold" button
function onFold()  {
    //create response object
    const response = {
        action: 'F',
        betAmount: 0
    }

    hideActionBar();

    //send WebSocket response to back end
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}
function onCheck() {
    //create response object
    const response = {
        action: 'C',
        betAmount: 0
    }

    hideActionBar();

    //send WebSocket response to back end
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}
function onCall() {
    //retrieve the call amount from the call button
    const button = $("#call");
    const bet = getBetValue(button);

    //create response object
    const response = {
        action: 'P',
        betAmount: bet,
    }

    hideActionBar();

    //send WebSocket response to back end
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}

function onBet() {
    //retrieve the bet amount from the "bet" or "raise" button
    let button = null;
    if(betButtonType == 'b') button = $("#bet");
    if(betButtonType == 'r') button = $("#raise");

    const bet = getBetValue(button);

    //create response object
    const response = {
        action: 'B',
        betAmount: bet,
    }

    hideActionBar();

    //send WebSocket response to back end
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}
function onAllIn() {
    let button = $('#all-in');
    const bet = getBetValue(button);

    //TODO finish this at some point
}

//extracts the text from the "bet" button to get the float value of the bet
function getBetValue(button) {
    //gets the text of the button and strips all non-integers and "."
    const buttonText = button.text();
    let numberMatch = buttonText.match(/[\d\.]+/);

    //if it is a number:
    if (numberMatch) {
        // Extracted number as a string
        let numberString = numberMatch[0];

        // Parse the number to a float
        let parsedNumber = parseFloat(numberString);

        return parsedNumber;
    } else {
        return -1;
    }
}

//hides the action bar after the user chooses an action
function hideActionBar() {
    const actionBar = $(".action-bar")
    actionBar.css("display", "none");
}

//sets the smallest value a user can bet
function setMinValue (value) {
    minValue = value;
}

//sets the largest value a user can bet
function setMaxValue (value) {
    maxValue = value;
}

//sets the small blind
function setSmallBlind(value) {
    smallBlind = value;
}

//sets the pot size
function setPotSize(value) {
    potSize = value;
}

//sets the seat number of the player who is currently acting
function setSeat(seat) {
    seatNo = seat;
}

//sets
function setBetButtonType(type) {
    if(type === 'r') betButtonType = "r";
    else if (type === 'b') betButtonType = "b";
    else return "-1";
}

sliderHandle.on("mousedown", (event) => {
    isDragging = true;
    event.preventDefault();
});

$(document).on("mouseup", () => {
    isDragging = false;
});

//updates values in relation to the slider until the user releases cursor
$(document).on("mousemove", (event) => {
    if (!isDragging) return;

    const percentage = getSliderPercentage(event);
    const bet = updateSlider(percentage);
    updateBetButton(bet);
    updateInputBox(bet);
});