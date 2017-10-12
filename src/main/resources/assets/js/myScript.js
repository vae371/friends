var urlBase = "/api";
var uuid;
var result;
var wrongs = "";

var wins = localStorage.getItem("wins") === null ? "0" : localStorage.getItem("wins");
var loses = localStorage.getItem("loses") === null ? "0" : localStorage.getItem("loses");

var turns = document.getElementById("turns");
turns.innerHTML = "You have " + wins + " wins and " + loses + " loses";

// bind guess button with enter key
$(document).keypress(function (e) {
    if (e.which == 13) {
        $("#guessButton").click();
    }
});

// represent right guessed letters
function printCharSet(rightLetters) {
    var charSet = document.getElementById("charSet");
    charSet.innerHTML = "";
    for (var i = 0; i < rightLetters.length; i++) {
        var field = document.createTextNode(rightLetters[i]);
        charSet.appendChild(field);
    }
}

var showResult = function (res) {
    if (res == 1) {
        window.alert("You win!")
    }
    if (res == 2) {
        window.alert("Uhh.. you are dead now.");
    }
}

var guessClick = function () {
    var guess = $('#guessInput').val();

    // clear input and refocus
    $('#guessInput').val('');
    $('#guessInput').focus();

    if (result != null) {
        showResult(result);
        return;
    }

    if (guess.length == 0 || guess.charCodeAt(0) < 97 || guess.charCodeAt(0) > 122) {
        window.alert("please guess a lowercase letter");
        return;
    }

    if (wrongs.includes(guess)) {
        window.alert("you already guessed this letter and it is wrong");
        return;
    }

    $.ajax({
        type: "POST",
        url: urlBase + "/hangman/guess",
        data: JSON.stringify({"uuid": uuid, "guess": guess}),
        success: function (data) {
            printCharSet(data.rightLetters);

            wrongs = data.wrongs;

            var len = wrongs.length;
            $("#wrongs").text(len + " Wrong Letters: " + wrongs);

            if (len > 6) {
                len = 6;
            }
            $("#hangman").attr("src", "http://www.writteninpencil.de/Projekte/Hangman/hangman" + len + ".png");

            result = data.result;
            wins = parseInt(wins);
            loses = parseInt(loses);
            if (result != null) {
                if (result == 1) {
                    wins++;
                }
                if (result == 2) {
                    loses++;
                }
                localStorage.setItem("loses", loses + "");
                localStorage.setItem("wins", wins + "");
                turns.innerHTML = "You have " + wins + " wins and " + loses + " loses";
                showResult(result);
            }
        },
        dataType: "json",
        contentType: "application/json"
    });
}

var clearClick = function () {
    localStorage.setItem("loses", "0");
    localStorage.setItem("wins", "0");
    turns.innerHTML = "You have 0 wins and 0 loses";
}

function init() {
    $.get(urlBase + "/hangman/init", function (data) {
        uuid = data;
        console.log(uuid);
    });
}

window.onload = init;