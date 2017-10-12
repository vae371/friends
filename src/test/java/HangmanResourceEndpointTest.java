import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.testing.junit.ResourceTestRule;
import main.resource.HangmanResource;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * Created by x on 10/11/2017.
 */
public class HangmanResourceEndpointTest {
    HangmanResource hangmanResource = new HangmanResource("34.205.78.200", 11211, "/webster-dictionary.txt");

    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(hangmanResource)
            .build();

    // make sure the client get the right response
    @Test
    public void helloWorldDropwizard() {
        String uuid = UUID.randomUUID().toString();
        String word = "hello";
        hangmanResource.init(uuid, word);

        Response response = null;
        for (int i = 0; i < word.length(); i++) {
            ObjectNode request = resources.getObjectMapper().createObjectNode();
            request.put("uuid", uuid);
            request.put("guess", word.substring(i, i + 1));
            response = resources.client().target("/hangman/guess")
                    .request()
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));
        }

        Assert.assertEquals(response.getStatus(), 200);

        ObjectNode res = response.readEntity(ObjectNode.class);
        Assert.assertEquals(res.get("result").asInt(), 1);
    }
}
