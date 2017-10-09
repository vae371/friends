var urlBase = "http://localhost:8080/api";
var uuid;
var hasWin;

$(document).keypress(function (e) {
    if (e.which == 13) {
        $("#guessButton").click();
    }
});

function printCharSet(guessField) {
    var charSet = document.getElementById("charSet");
    charSet.innerHTML = "";
    for (var i = 0; i < guessField.length; i++) {
        var field = document.createTextNode(guessField[i]);
        charSet.appendChild(field);
    }
}

var guessClick = function () {
    var guess = $('#guessInput').val();
    $('#guessInput').val('');
    $('#guessInput').focus();

    if (hasWin != null) {
        if (hasWin == true) {
            window.alert("You win!");
        }
        if (hasWin == false) {
            window.alert("Uh...I guess you are already dead.");
        }
        return;
    }

    if (guess.length == 0 || guess.charCodeAt(0) < 97 || guess.charCodeAt(0) > 122) {
        window.alert("please guess a lowercase letter");
        return;
    }

    $.ajax({
        type: "POST",
        url: urlBase + "/hangman/guess",
        data: JSON.stringify({"uuid": uuid, "guess": guess}),
        success: function (data) {
            printCharSet(data.guessFiled);
            $("#wrongs").text("Wrong Letters: " + data.wrongs);
            $("#hangman").attr("src", "http://www.writteninpencil.de/Projekte/Hangman/hangman" + data.wrongs.length + ".png");
            hasWin = data.hasWin;
            if (hasWin != null) {
                if (hasWin == true) {
                    window.alert("You win!");
                } else {
                    window.alert("Uh...I guess you are already dead.");
                }
            }
        },
        dataType: "json",
        contentType: "application/json"
    });
}

function init() {
    $.get(urlBase + "/hangman/init", function (data) {
        uuid = data;
        console.log(uuid);
    });
}

window.onload = init;