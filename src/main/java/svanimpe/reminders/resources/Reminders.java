package svanimpe.reminders.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Set;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import svanimpe.reminders.domain.List;
import svanimpe.reminders.domain.Location;
import svanimpe.reminders.domain.Reminder;
import svanimpe.reminders.domain.Role;

import static svanimpe.reminders.util.Utilities.IMAGES_BASE_DIR;
import static svanimpe.reminders.util.Utilities.MAX_IMAGE_SIZE_IN_MB;
import static svanimpe.reminders.util.Utilities.mergeMessages;

@Path("lists/{listid}/reminders")
@Transactional(dontRollbackOn = {BadRequestException.class, ForbiddenException.class, NotFoundException.class})
@RequestScoped
public class Reminders
{
    @PersistenceContext
    private EntityManager em;

    @Resource
    private Validator validator;

    @Context
    private SecurityContext context;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<Reminder> getRemindersInList(@PathParam("listid") long listId)
    {
        List list = em.find(List.class, listId);
        
        if (list == null) {
            throw new NotFoundException();
        }

        // Only admins can read another user's reminders.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        TypedQuery<Reminder> q = em.createNamedQuery("Reminder.findByList", Reminder.class).setParameter("list", list);
        return q.getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReminderToList(@PathParam("listid") long listId, Reminder reminder)
    {
        List list = em.find(List.class, listId);

        if (list == null) {
            throw new NotFoundException();
        }

        // Only admins can add reminders to another user's lists.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        reminder.setList(list);

        Set<ConstraintViolation<Reminder>> violations = validator.validate(reminder);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.persist(reminder);
        return Response.created(URI.create("/lists/" + listId + "/reminders/" + reminder.getId())).build();
    }

    @GET
    @Path("{reminderid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Reminder getReminder(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId)
    {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        // Only admins can read another user's reminders.
        if (!reminder.getList().getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        return reminder;
    }
    
    @PUT
    @Path("{reminderid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateReminder(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId, InputStream in)
    {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        // Only admins can update another user's reminders.
        if (!reminder.getList().getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }
        
        em.detach(reminder);
        
        try {
            JsonObject reminderUpdate = Json.createReader(in).readObject();
            if (reminderUpdate.containsKey("list")) {
                List list = em.find(List.class, reminderUpdate.getJsonNumber("list").longValue());
                
                // Only admins can move reminders to another user's list.
                if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
                    throw new ForbiddenException();
                } else {
                    reminder.setList(list);
                }
            }
            if (reminderUpdate.containsKey("title")) {
                reminder.setTitle(reminderUpdate.getString("title"));
            }
            if (reminderUpdate.containsKey("date")) {
                if (reminderUpdate.isNull("date")) {
                    reminder.setDate(null);
                } else {
                    Calendar date = Calendar.getInstance();
                    date.setTimeInMillis(reminderUpdate.getJsonNumber("date").longValue());
                    reminder.setDate(date);
                }
            }
            if (reminderUpdate.containsKey("location")) {
                if (reminderUpdate.isNull("location")) {
                    reminder.setLocation(null);
                    System.out.println("--- SET TO NULL");
                } else {
                    Location location = new Location();
                    JsonObject jsonLocation = reminderUpdate.getJsonObject("location");
                    if (jsonLocation.containsKey("latitude")) {
                        location.setLatitude(jsonLocation.getJsonNumber("latitude").doubleValue());
                    }
                    if (jsonLocation.containsKey("longitude")) {
                        location.setLongitude(jsonLocation.getJsonNumber("longitude").doubleValue());
                    }
                    reminder.setLocation(location);
                }
            }
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }
        
        Set<ConstraintViolation<Reminder>> violations = validator.validate(reminder);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }
        
        em.merge(reminder);
    }
    
    @DELETE
    @Path("{reminderid}")
    public void removeReminder(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) throws IOException
    {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        // Only admins can delete another user's reminders.
        if (!reminder.getList().getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }
        
        if (reminder.getImage() != null) {
            Files.deleteIfExists(IMAGES_BASE_DIR.resolve(reminder.getImage()));
        }
        em.remove(reminder);
    }
    
    @GET
    @Path("{reminderid}/image")
    @Produces("image/jpeg")
    public InputStream getImage(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) throws IOException
    {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId || reminder.getImage() == null) {
            throw new NotFoundException();
        }

        java.nio.file.Path path = IMAGES_BASE_DIR.resolve(reminder.getImage());
        if (!Files.exists(path)) {
            throw new InternalServerErrorException("Could not load image " + reminder.getImage());
        }

        return Files.newInputStream(path);
    }
    
    @PUT
    @Path("{reminderid}/image")
    @Consumes("image/jpeg")
    public void setImage(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId, @HeaderParam("Content-Length") long fileSize, InputStream in) throws IOException
    {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId) {
            throw new NotFoundException();
        }

        // Only admins can update another user's images.
        if (!context.getUserPrincipal().getName().equals(reminder.getList().getOwner().getUsername()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        // Make sure the file is not larger than the maximum allowed size.
        if (fileSize > 1024 * 1024 * MAX_IMAGE_SIZE_IN_MB) {
            throw new BadRequestException("REMINDER_IMAGE");
        }

        // Save the image. By default, {reminderid}.jpg is used as the filename.
        Files.copy(in, IMAGES_BASE_DIR.resolve(reminder.getId() + ".jpg"), StandardCopyOption.REPLACE_EXISTING);
        reminder.setImage(reminder.getId() + ".jpg");
    }
    
    @DELETE
    @Path("{reminderid}/image")
    public void removeImage(@PathParam("listid") long listId, @PathParam("reminderid") long reminderId) throws IOException
    {
        Reminder reminder = em.find(Reminder.class, reminderId);

        if (reminder == null || reminder.getList().getId() != listId || reminder.getImage() == null) {
            throw new NotFoundException();
        }

        // Only admins can delete another user's images.
        if (!context.getUserPrincipal().getName().equals(reminder.getList().getOwner().getUsername()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        Files.deleteIfExists(IMAGES_BASE_DIR.resolve(reminder.getImage()));
        reminder.setImage(null);
    }
}