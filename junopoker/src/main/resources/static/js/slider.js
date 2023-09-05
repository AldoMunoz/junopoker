const sliderContainer = $(".slider-container");
const sliderHandle = $(".slider-handle");
const sliderValue = $("#slider-value");
const betButton = $("#bet");

let isDragging = false;

let minValue = 0;
let maxValue = 0;
let smallBlind = 0;
let lastSliderPercentage = 0;
let potSize = 0;

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

function updateSlider(percentage) {
    const handlePosition = `${percentage}%`;
    sliderHandle.css("left", handlePosition);
    sliderContainer.find(".slider-track").css("width", `${percentage}%`);

    lastSliderPercentage = percentage;

    const bet = getBetValueFromPercentage(percentage);
    return bet;
}

function getBetValueFromPercentage(percentage) {
    const betValue = minValue + (percentage/100) * (maxValue - minValue);
    const formattedValue = betValue.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 });

    return formattedValue;
}

function updateBetButton(bet) {
    betButton.text(`Bet: ${bet}`);
}

function updateInputBox(bet) {
    sliderValue.val(bet);
}

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
function changeBetSize(size) {
    const betSize = (size/100)*potSize;

    if(betSize > minValue) {
        const newPercentage = ((betSize - minValue) / (maxValue - minValue)) * 100;
        const bet = updateSlider(newPercentage);
        updateBetButton(bet);
        updateInputBox(bet);
    }
}

function onInputChange(input) {
    let inputArray = input.split('.');

    let duplicate = false;
    // Check if there is more than one period
    if (inputArray.length > 2) {
        duplicate = true;

        // Remove the last element (the extra period)
        inputArray.pop();

        // Join the array elements back together with periods
        input = inputArray.join('.');
    }

    if(input < minValue) {
        const bet= updateSlider(0);
        updateBetButton(bet);
        if(duplicate) updateInputBox(input);
    }
    else if(input > maxValue) {
        const bet = updateSlider(100);
        updateBetButton(bet);
        updateInputBox(bet);
    }
        //if the input hasn't changed, do nothing
    //for example if user changed input from "4.00" to "4.0"
    else if(input == (lastSliderPercentage / 100) * (maxValue - minValue) + minValue) {
        if (duplicate) updateInputBox(input);
    }
    else {
        const percentage = ((input - minValue) / (maxValue - minValue)) * 100;
        const bet = updateSlider(percentage);
        updateBetButton(bet);
        updateInputBox(bet);
    }
}
function onFold()  {
    const response = {
        action: 'F',
        betAmount: 0
    }
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}
function onCheck() {
    const response = {
        action: 'C',
        betAmount: 0
    }
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}
function onCall() {
    const button = $("#call");

    const bet = getBetValue(button);

    const response = {
        action: 'B',
        betAmount: bet
    }
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));

}
function onBet() {
    const button = $("#bet");

    const bet = getBetValue(button);

    const response = {
        action: 'B',
        betAmount: bet
    }
    stompClient.send("/app/playerActionEvent", {}, JSON.stringify(response));
}
function getBetValue(button) {
    const buttonText = button.text();
    let numberMatch = buttonText.match(/[\d\.]+/);

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


function setMinValue (value) {
    minValue = value;
}

function setMaxValue (value) {
    maxValue = value;
}

function setSmallBlind(value) {
    smallBlind = value;
}

function setPotSize(value) {
    potSize = value;
}

sliderHandle.on("mousedown", (event) => {
    isDragging = true;
    event.preventDefault();
});

$(document).on("mouseup", () => {
    isDragging = false;
});

$(document).on("mousemove", (event) => {
    if (!isDragging) return;

    const percentage = getSliderPercentage(event);
    const bet = updateSlider(percentage);
    updateBetButton(bet);
    updateInputBox(bet);
});