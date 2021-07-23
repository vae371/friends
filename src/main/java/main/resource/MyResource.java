package main.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Synchronized;
import main.dao.MyDAO;
import main.pojo.Caption;
import main.pojo.UserProgress;
import main.pojo.UserSession;
import org.checkerframework.checker.units.qual.C;
import org.jdbi.v3.core.Jdbi;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class MyResource {
    private final long oneWeekTime = 7 * 24 * 3600 * 1000;
    private static Jdbi jdbi;
    private Map<String, UserSession> session = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    private int j = 0;

    public MyResource(Jdbi jdbi) {
        // singleton pattern
        if (this.jdbi == null) {
            this.jdbi = jdbi;
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(ObjectNode requestBody, @CookieParam("sid") Cookie cookie) {
        j++;
        MyDAO dao = jdbi.onDemand(MyDAO.class);

        // login using password
        String username = Optional.of(requestBody).map(x -> x.get("username")).map(JsonNode::textValue).orElse(null);
        String password = Optional.of(requestBody).map(x -> x.get("password")).map(JsonNode::textValue).orElse(null);
        if (username == null || password == null) {
            return Response.status(403).build();
        }

        if (dao.getAccount(username) == null) {
            dao.createAccount(username, password);
        } else if (dao.getAccount(username, password) == null) {
            return Response.status(403).build();
        }

        // save in session
        String sid = UUID.randomUUID().toString();
        session.put(sid, new UserSession(username, System.currentTimeMillis()));

        UserProgress userProgress = dao.getProgress(username);
        if (userProgress == null) {
            userProgress = new UserProgress(10, username,0);
        }

        return Response.ok().cookie(new NewCookie(new Cookie("sid", sid))).entity(dao.getCaption(userProgress.getLast_pos())).build();
    }

    @GET
    @Path("/caption")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response caption(@QueryParam("id") int id, @CookieParam("sid") Cookie cookie) {
        MyDAO dao = jdbi.onDemand(MyDAO.class);

        // remove expired session
        session.entrySet().removeIf(x -> (System.currentTimeMillis() - x.getValue().getTime() > oneWeekTime));

        // get user from session
        UserSession userSession = Optional.ofNullable(cookie).map(Cookie::getValue).map(session::get).orElse(null);
        if (userSession == null) {
            return Response.status(403).build();
        }

        // get caption
        Caption caption = dao.getCaption(id);

        // update progress
        if (caption != null) {
            dao.updateProgress(userSession.getUsername(), id);
            return Response.ok().entity(caption).build();
        } else {
            return Response.status(400).entity(objectMapper.createObjectNode().put("error", "INVALID_CAPTION_ID")).build();
        }
    }
}
