function removePlayer() {
    const settingsBar = $('.settings-bar');
    const seat = settingsBar.attr('data-seat');
    console.log("Seat value in removePlayer()", seat);

    const playerRequest = {
        type: "STAND",
        player: null,
        seatIndex: seat,
        tableID: $("#table-id").val()
    };

    stompClient.send("/app/removePlayer", {}, JSON.stringify(playerRequest));
    isSeated = false;

    /*
    fetch(`/removePlayerAtSeat?seat=${seat}&tableID=${$("#table-id").val()}`)
        .then(response => response.json())
        .then(player => {
            const request = {
                type: "STAND",
                player: player,
                seat: seat
            }

            isSeated = false
            stompClient.send("/app/tableEvents", {}, JSON.stringify(request));
            stompClient.send("/app/playerEvents", {}, JSON.stringify(request));
        });

     */
}