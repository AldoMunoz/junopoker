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

}