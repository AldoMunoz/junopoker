const sliderContainer = $(".slider-container");
const sliderHandle = $(".slider-handle");
const sliderValue = $("#slider-value");

let isDragging = false;

function getSliderPercentage(event) {
    const containerRect = sliderContainer[0].getBoundingClientRect();
    const sliderWidth = sliderHandle.outerWidth();
    const offsetX = event.clientX - containerRect.left - sliderWidth / 2;
    const trackWidth = containerRect.width - sliderWidth;
    const percentage = (offsetX / trackWidth) * 100;
    return Math.max(0, Math.min(100, percentage));
}

function updateSlider(percentage) {
    const handlePosition = `${percentage}%`;
    sliderHandle.css("left", handlePosition);
    sliderContainer.find(".slider-track").css("width", `${percentage}%`);
    sliderValue.val(Math.round(percentage) + "%");
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
    updateSlider(percentage);
});

// Initialize the slider to 50% (you can set any initial value you want)
const initialSliderValue = 0;
updateSlider(initialSliderValue);