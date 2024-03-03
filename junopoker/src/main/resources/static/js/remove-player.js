function standPlayer() {
    isSeated = false;
    const standRequest = {
        type: "STAND",
        seatIndex: $('.settings-bar').attr('data-seat'),
        tableId: $("#table-id").val()
    };

    stompClient.send("/app/standPlayerAtSeat", {}, JSON.stringify(standRequest));
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