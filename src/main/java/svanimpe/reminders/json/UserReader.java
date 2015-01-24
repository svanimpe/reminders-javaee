package svanimpe.reminders.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class UserReader implements MessageBodyReader<User>
{
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return User.class.isAssignableFrom(type);
    }

    @Override
    public User readFrom(Class<User> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        User user = new User();
        
        try {
            JsonObject jsonUser = Json.createReader(entityStream).readObject();

            if (jsonUser.containsKey("username")) {
                user.setUsername(jsonUser.getString("username"));
            }
            
            if (jsonUser.containsKey("fullName")) {
                user.setFullName(jsonUser.getString("fullName"));
            }
            
            if (jsonUser.containsKey("password")) {
                user.setPassword(jsonUser.getString("password"));
            }

            JsonArray roles = jsonUser.getJsonArray("roles");
            if (roles != null) {
                for (int i = 0; i < roles.size(); i++) {
                    try {
                        Role role = Role.valueOf(roles.getString(i).toUpperCase());
                        user.getRoles().add(role);
                    } catch (IllegalArgumentException ex) {
                        // Invalid role name.
                        throw new BadRequestException("USER_ROLES");
                    }
                }
            }
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }
        
        return user;
    }
}
