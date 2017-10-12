import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import main.resource.HangmanResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by x on 10/11/2017.
 */
public class HangmanResourceTest {
    HangmanResource hangmanResource;
    ObjectMapper objectMapper;

    @Before
    public void setup() {
        hangmanResource = new HangmanResource("34.205.78.200", 11211, "/webster-dictionary.txt");
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testWinningLogic() {
        // save the uuid and word
        String uuid = UUID.randomUUID().toString();
        String word = "hello";
        hangmanResource.init(uuid, word);

        // keep guess the right letter
        ObjectNode response = null;
        for (int i = 0; i < word.length(); i++) {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("uuid", uuid);
            request.put("guess", word.substring(i, i + 1));
            response = hangmanResource.guess(request);
        }

        // assert result==1 which will be translated wining in front end
        Assert.assertTrue(response.get("result").asInt() == 1);
    }
}
