package main.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import net.spy.memcached.MemcachedClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/hangman")
public class HangmanResource {
    public static MemcachedClient memcachedClient;
    static List<String> wordList;
    Random random = new Random();
    ObjectMapper objectMapper = new ObjectMapper();

    public HangmanResource(String memcacheServerIp, int memcacheServerPort, String dictonaryPath) {
        // singleton pattern for cache connection and word list
        if (memcachedClient == null) {
            try {
                memcachedClient = new MemcachedClient(new InetSocketAddress[]{new InetSocketAddress(memcacheServerIp, memcacheServerPort)});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (wordList == null) {
            // load txt file inside jar and only allow words with length in range 3-4 to reduce game difficulty
            try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(dictonaryPath)))) {
                wordList = br.lines().filter(s -> s.matches("[a-zA-Z]+") && s.length() > 2 && s.length() < 5).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @POST
    @Path("/guess")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ObjectNode guess(ObjectNode requestBody) {
        String uuid = requestBody.get("uuid").textValue();

        // retrieve from cache
        ObjectNode objectNode = null;
        try {
            objectNode = (ObjectNode) objectMapper.readTree((byte[]) memcachedClient.get(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String word = objectNode.get("word").textValue();
        String wrongs = objectNode.get("wrongs").textValue();
        ArrayNode rightLetters = (ArrayNode) objectNode.get("rightLetters");

        // game over?
        if (!objectNode.get("result").isNull()) {
            return objectNode;
        }

        boolean hasWin = true;
        boolean guessRight = false;
        char guess = requestBody.get("guess").textValue().charAt(0);

        // compute if guess is right and if user has won
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess) {
                rightLetters.set(i, new TextNode(guess + ""));
                guessRight = true;
            }
            if (rightLetters.get(i).textValue().equals("_ ")) {
                hasWin = false;
            }
        }

        if (!guessRight) {
            wrongs += guess;
            objectNode.put("wrongs", wrongs);
        }

        if (hasWin) {
            objectNode.put("result", 1);
        }

        if (wrongs.length() == 10) {
            objectNode.put("result", 2);
        }

        // save
        try {
            memcachedClient.set(uuid, 60 * 60, objectMapper.writeValueAsBytes(objectNode));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // word will not be included in response to avoid cheating
        objectNode.remove("word");
        return objectNode;
    }

    public void init(String uuid, String word) {
        String wrongs = "";

        ArrayNode rightLetters = objectMapper.createArrayNode();
        for (int i = 0; i < word.length(); i++) {
            rightLetters.add("_ ");
        }

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("word", word);
        objectNode.put("wrongs", wrongs);
        objectNode.put("rightLetters", rightLetters);
        objectNode.putNull("result");

        try {
            memcachedClient.set(uuid, 60 * 60, objectMapper.writeValueAsBytes(objectNode));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/init")
    public String init() throws JsonProcessingException {
        String uuid = UUID.randomUUID().toString();
        String randomWord = wordList.get(random.nextInt(wordList.size()));

        // link uuid with word and save in cache
        init(uuid, randomWord);

        return uuid;
    }
}
