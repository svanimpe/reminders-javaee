package svanimpe.reminders.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import svanimpe.reminders.domain.Reminder;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ReminderWriter implements MessageBodyWriter<Reminder>
{
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Reminder.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Reminder reminder, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    @Override
    public void writeTo(Reminder reminder, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {    
        JsonObjectBuilder jsonReminder = Json.createObjectBuilder();
        jsonReminder.add("id", reminder.getId());
        jsonReminder.add("title", reminder.getTitle());
        if (reminder.getDate() != null) {
            jsonReminder.add("date", reminder.getDate().getTimeInMillis());
        }
        if (reminder.getLocation() != null) {
            JsonObjectBuilder jsonLocation = Json.createObjectBuilder();
            jsonLocation.add("latitude", reminder.getLocation().getLatitude());
            jsonLocation.add("longitude", reminder.getLocation().getLongitude());
            jsonReminder.add("location", jsonLocation);
        }
        jsonReminder.add("image", reminder.getImage() != null);

        try (JsonWriter writer = Json.createWriter(entityStream)) {
            writer.writeObject(jsonReminder.build());
        }
    }
}
