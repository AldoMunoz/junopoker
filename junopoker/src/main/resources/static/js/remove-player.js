function removePlayer() {
    const settingsBar = $('.settings-bar');
    const seat = settingsBar.data('seat');
    console.log(seat);

    fetch(`/removePlayerAtSeat?seat=${seat}`)
        .then(response => response.json())
        .then(player => {
            const request = {
                type: "STAND",
                player: player,
                seat: seat
            }

            stompClient.send("/app/tableEvents", {}, JSON.stringify(request));
            stompClient.send("/app/playerEvents", {}, JSON.stringify(request));
        });
}