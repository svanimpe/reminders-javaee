package svanimpe.reminders.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import svanimpe.reminders.domain.List;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ListListWriter implements MessageBodyWriter<java.util.List<List>>
{
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        if (!java.util.List.class.isAssignableFrom(type)) {
            return false;
        }

        if (genericType instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) genericType).getActualTypeArguments();
            return arguments.length == 1 && arguments[0].equals(List.class);
        } else {
            return false;
        }
    }

    @Override
    public long getSize(java.util.List<List> lists, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    @Override
    public void writeTo(java.util.List<List> lists, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
        JsonArrayBuilder jsonListList = Json.createArrayBuilder();
        
        for (List list : lists) {        
            JsonObjectBuilder jsonList = Json.createObjectBuilder();
            jsonList.add("id", list.getId());
            jsonList.add("title", list.getTitle());
            
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("RemindersPU");
            EntityManager em = emf.createEntityManager();
            Query q = em.createNamedQuery("List.findSize").setParameter("list", list);
            jsonList.add("size", (Long) q.getSingleResult());
            em.close();
            emf.close();
            
            jsonListList.add(jsonList);
        }

        try (JsonWriter writer = Json.createWriter(entityStream)) {
            writer.writeArray(jsonListList.build());
        }
    }
}
