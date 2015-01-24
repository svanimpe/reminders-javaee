package svanimpe.reminders.resources;

import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

/*
 * See the API docs for more info on how to use this resource.
 */
@Path("credentials")
@Transactional(dontRollbackOn = {BadRequestException.class})
public class Credentials
{
    @PersistenceContext
    private EntityManager em;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String checkCredentials(InputStream in)
    {
        try {
            JsonObject credentials = Json.createReader(in).readObject();

            String username = null;
            String password = null;
            
            if (credentials.containsKey("username")) {
                username = credentials.getString("username");
            }
            
            if (credentials.containsKey("password")) {
                password = credentials.getString("password");
            }

            if (username == null) {
                // Username is required.
                throw new BadRequestException("CREDENTIALS_USERNAME");
            }

            if (password == null) {
                // Password is required.
                throw new BadRequestException("CREDENTIALS_PASSWORD");
            }

            User existingUser = em.find(User.class, username);

            if (existingUser == null) {
                return "[]";
            }

            User tempUser = new User();
            tempUser.setPassword(password);

            if (!existingUser.getPassword().equals(tempUser.getPassword())) {
                return "[]";
            }

            JsonArrayBuilder roles = Json.createArrayBuilder();
            for (Role role : existingUser.getRoles()) {
                roles.add(role.name());
            }
            return roles.build().toString();
            
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }
    }
}
