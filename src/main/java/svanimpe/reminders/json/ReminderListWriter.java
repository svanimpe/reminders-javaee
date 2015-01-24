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
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import svanimpe.reminders.domain.Reminder;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ReminderListWriter implements MessageBodyWriter<java.util.List<Reminder>>
{
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        if (!java.util.List.class.isAssignableFrom(type)) {
            return false;
        }

        if (genericType instanceof ParameterizedType) {
            Type[] arguments = ((ParameterizedType) genericType).getActualTypeArguments();
            return arguments.length == 1 && arguments[0].equals(Reminder.class);
        } else {
            return false;
        }
    }

    @Override
    public long getSize(java.util.List<Reminder> reminders, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    @Override
    public void writeTo(java.util.List<Reminder> reminders, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
        JsonArrayBuilder jsonReminderList = Json.createArrayBuilder();
        
        for (Reminder reminder : reminders) {        
            JsonObjectBuilder jsonReminder = Json.createObjectBuilder();
            jsonReminder.add("id", reminder.getId());
            jsonReminder.add("title", reminder.getTitle());
            if (reminder.getDate() != null) {
                jsonReminder.add("date", reminder.getDate().getTimeInMillis());
            }
            jsonReminderList.add(jsonReminder);
        }

        try (JsonWriter writer = Json.createWriter(entityStream)) {
            writer.writeArray(jsonReminderList.build());
        }
    }
}
