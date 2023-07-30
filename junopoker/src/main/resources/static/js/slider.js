//Script for handling movement of the slider by the user
const sliderContainer = document.querySelector(".slider-container");
const sliderTrack = document.querySelector(".slider-track");
const sliderHandle = document.querySelector(".slider-handle");
const sliderValue = document.getElementById("slider-value");

let isDragging = false;

function getSliderPercentage(event) {
    const containerRect = sliderContainer.getBoundingClientRect();
    const sliderWidth = sliderHandle.offsetWidth;
    const offsetX = event.clientX - containerRect.left - sliderWidth / 2;
    const trackWidth = containerRect.width - sliderWidth;
    const percentage = (offsetX / trackWidth) * 100;
    return Math.max(0, Math.min(100, percentage));
}

function updateSlider(percentage) {
    const handlePosition = `calc(${percentage}% + 10px)`;
    sliderHandle.style.left = handlePosition;
    sliderTrack.style.width = `${percentage}%`;
    sliderValue.value = Math.round(percentage) + "%";
}

sliderHandle.addEventListener("mousedown", (event) => {
    isDragging = true;
    event.preventDefault();
});

document.addEventListener("mouseup", () => {
    isDragging = false;
});

document.addEventListener("mousemove", (event) => {
    if (!isDragging) return;

    const percentage = getSliderPercentage(event);
    updateSlider(percentage);
});

// Initialize the slider to 50% (you can set any initial value you want)
const initialSliderValue = 0;
updateSlider(initialSliderValue);
