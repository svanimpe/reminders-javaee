package svanimpe.reminders.json;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Calendar;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import svanimpe.reminders.domain.Location;
import svanimpe.reminders.domain.Reminder;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class ReminderReader implements MessageBodyReader<Reminder>
{
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return Reminder.class.isAssignableFrom(type);
    }

    @Override
    public Reminder readFrom(Class<Reminder> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        Reminder reminder = new Reminder();
        
        try {
            JsonObject jsonReminder = Json.createReader(entityStream).readObject();
            if (jsonReminder.containsKey("title")) {
                reminder.setTitle(jsonReminder.getString("title"));
            }
            if (jsonReminder.containsKey("date")) {
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(jsonReminder.getJsonNumber("date").longValue());
                reminder.setDate(date);
            }
            if (jsonReminder.containsKey("location")) {
                Location location = new Location();
                JsonObject jsonLocation = jsonReminder.getJsonObject("location");
                if (jsonLocation.containsKey("latitude")) {
                    location.setLatitude(jsonLocation.getJsonNumber("latitude").doubleValue());
                }
                if (jsonLocation.containsKey("longitude")) {
                    location.setLongitude(jsonLocation.getJsonNumber("longitude").doubleValue());
                }
                reminder.setLocation(location);
            }

        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }
        
        return reminder;
    }
}
