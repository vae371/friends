package main.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import net.spy.memcached.MemcachedClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Path("/hangman")
public class HangmanResource {
    static MemcachedClient memcachedClient;
    List<String> wordList;
    Random random = new Random();
    ObjectMapper objectMapper = new ObjectMapper();

    public HangmanResource(String memcacheServerIp, int memcacheServerPort, String words) throws IOException {
        if (memcachedClient == null) {
            memcachedClient = new MemcachedClient(new InetSocketAddress[]{new InetSocketAddress(memcacheServerIp, memcacheServerPort)});
        }
        wordList = Arrays.asList(words.toLowerCase().split(" "));
    }

    @POST
    @Path("/guess")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ObjectNode guess(ObjectNode requestBody) throws IOException {
        // data from http request body
        String uuid = requestBody.get("uuid").textValue();
        char guess = requestBody.get("guess").textValue().charAt(0);

        // retrieve data from cache
        //TODO check null
        ObjectNode objectNode = (ObjectNode) objectMapper.readTree((byte[]) memcachedClient.get(uuid));

        // already game over?
        if (!objectNode.get("hasWin").isNull()) {
            return objectNode;
        }

        String word = objectNode.get("word").textValue();
        String wrongs = objectNode.get("wrongs").textValue();
        ArrayNode guessFiled = (ArrayNode) objectNode.get("guessFiled");

        // game logic
        boolean allComplete = true;
        boolean guessRight = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == guess) {
                guessFiled.set(i, new TextNode(guess + ""));
                guessRight = true;
            }
            if (guessFiled.get(i).textValue().equals("_ ")) {
                allComplete = false;
            }
        }

        if (!guessRight) {
            wrongs += guess;
            objectNode.put("wrongs", wrongs);
        }

        if (wrongs.length() == 6) {
            objectNode.put("hasWin", false);
        }
        if (allComplete) {
            objectNode.put("hasWin", true);
        }

        // save to cache
        memcachedClient.set(uuid, 60 * 60, objectMapper.writeValueAsBytes(objectNode));

        objectNode.remove("word");
        return objectNode;
    }

    @GET
    @Path("/init")
    public String init() throws JsonProcessingException {
        // params
        String uuid = UUID.randomUUID().toString();
        String word = wordList.get(random.nextInt(wordList.size()));
        String wrongs = "";
        ArrayNode guessFiled = objectMapper.createArrayNode();
        for (int i = 0; i < word.length(); i++) {
            guessFiled.add("_ ");
        }

        // save in cache
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("word", word);
        objectNode.put("wrongs", wrongs);
        objectNode.put("guessFiled", guessFiled);
        objectNode.putNull("hasWin");
        memcachedClient.set(uuid, 60 * 60, objectMapper.writeValueAsBytes(objectNode));

        return uuid;
    }
}
