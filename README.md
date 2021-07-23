# friends
implemented with Dropwizard and JQuery

Used Jetty as embedded web container to save the trouble for clients to run the program

Used Json to deal with http request/response data

Used mysql to persist data

Used yml file for configuration

Java Coding style: https://google.github.io/styleguide/javaguide.html

How to run:

Open as a Java project and run the src/main/HangmanApplication with two arguments: server and hangman.yml

Or install maven and run “mvn clean package” in the root folder where pom.xml resides to get jar file in target folder, then put jar file and hangman.yml and dictionary file in the same folder and run “java -cp hangman-1.0-SNAPSHOT.jar main.MyApplication server hangman.yml”

How to play:

After running, enter http://localhost:8080 in browser, and have fun
