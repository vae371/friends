# hangman
hangman game implemented with Dropwizard and JQuery

For the html, css and hangman image, I used and modified templates from codePen

For Js, I mainly used JQuery. Implemented features to improve user experience like auto focus, auto clean input field and bind enter key with guess button

Used Jetty as embeded web container to save the trouble for clients to run the program

Used Json to deal with data

Used memCached to save the trouble configuring database

Used yml file for configuration

Java Coding style: https://google.github.io/styleguide/javaguide.html

How to run:

Open as a Java project and run the src/main/HangmanApplication with two arguments: server and hangman.yml

Or install maven and run “mvn clean package” in the root folder where pom.xml resides to get jar file in target folder, then put jar file and hangman.yml in the same folder and run “java -cp hangman-1.0-SNAPSHOT.jar main.HangmanApplication server hangman.yml”

How to play:

After running, enter http://localhost:8080 in browser, and have fun
