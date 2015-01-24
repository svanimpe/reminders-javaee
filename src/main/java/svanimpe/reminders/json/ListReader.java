package svanimpe.reminders.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import svanimpe.reminders.domain.List;
import svanimpe.reminders.domain.User;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class ListReader implements MessageBodyReader<List>
{
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return List.class.isAssignableFrom(type);
    }

    @Override
    public List readFrom(Class<List> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        List list = new List();
        
        try {
            JsonObject jsonList = Json.createReader(entityStream).readObject();
            
            if (jsonList.containsKey("title")) {
                list.setTitle(jsonList.getString("title"));
            }
            if (jsonList.containsKey("owner")) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("RemindersPU");
                EntityManager em = emf.createEntityManager();
                list.setOwner(em.find(User.class, jsonList.getString("owner")));
                em.close();
                emf.close();
            }
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }
        
        return list;
    }
}
