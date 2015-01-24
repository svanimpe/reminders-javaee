package svanimpe.reminders.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import svanimpe.reminders.domain.Reminder;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

import static svanimpe.reminders.util.Utilities.mergeMessages;

@Path("lists")
@Transactional(dontRollbackOn = {BadRequestException.class, ForbiddenException.class, NotFoundException.class})
@RequestScoped
public class Lists
{
    @PersistenceContext
    private EntityManager em;
    
    @Resource
    private Validator validator;
    
    @Context
    private SecurityContext context;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<List> getLists()
    {
        TypedQuery<List> q = em.createNamedQuery("List.findByOwner", List.class);
        q.setParameter("owner", em.find(User.class, context.getUserPrincipal().getName()));
        return q.getResultList();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addList(List list)
    {
        // If the list doesn't have an owner, set it to the current user.
        if (list.getOwner() == null) {
            list.setOwner(em.find(User.class, context.getUserPrincipal().getName()));
        }
        
        // Only admins can create lists for other users.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }
        
        Set<ConstraintViolation<List>> violations = validator.validate(list);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.persist(list);
        return Response.created(URI.create("/lists/" + list.getId())).build();
    }
    
    @GET
    @Path("{listid}")
    @Produces(MediaType.APPLICATION_JSON)
    public List getList(@PathParam("listid") long id)
    {
        List list = em.find(List.class, id);
        
        if (list == null) {
            throw new NotFoundException();
        }

        // Only admins can read other user's lists.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        return list;
    }

    @PUT
    @Path("{listid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateList(@PathParam("listid") long id, InputStream in)
    {
        List list = em.find(List.class, id);
  
        if (list == null) {
            throw new NotFoundException();
        }

        // Only admins can update other user's lists.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        em.detach(list);

        try {
            JsonObject update = Json.createReader(in).readObject();
            if (update.containsKey("title")) {
                list.setTitle(update.getString("title"));
            }
            if (update.containsKey("owner")) {
                User newOwner = em.find(User.class, update.getString("owner"));
                if (newOwner == null) {
                    throw new NotFoundException();
                }
                
                // Only admins can change the owner of a list.
                if (!newOwner.equals(list.getOwner()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
                    throw new ForbiddenException();
                } else {
                    list.setOwner(newOwner);
                }
            } 
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }

        Set<ConstraintViolation<List>> violations = validator.validate(list);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.merge(list);
    }

    @Inject
    private Reminders remindersResource;
    
    @DELETE
    @Path("{listid}")
    public void removeList(@PathParam("listid") long id) throws IOException
    {
        List list = em.find(List.class, id);
        
        if (list == null) {
            throw new NotFoundException();
        }

        // Only admins can delete other user's lists.
        if (!list.getOwner().getUsername().equals(context.getUserPrincipal().getName()) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }
        
        TypedQuery<Reminder> q = em.createNamedQuery("Reminder.findByList", Reminder.class).setParameter("list", list);
        for (Reminder reminder : q.getResultList()) {
            remindersResource.removeReminder(list.getId(), reminder.getId());
        }
        em.remove(list);
    }
}
