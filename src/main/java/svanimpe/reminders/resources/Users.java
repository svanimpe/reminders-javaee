package svanimpe.reminders.resources;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
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
import javax.ws.rs.DefaultValue;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import svanimpe.reminders.domain.List;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;
import svanimpe.reminders.validation.OnPasswordUpdate;

import static svanimpe.reminders.util.Utilities.IMAGES_BASE_DIR;
import static svanimpe.reminders.util.Utilities.MAX_PROFILE_PICTURE_SIZE_IN_MB;
import static svanimpe.reminders.util.Utilities.mergeMessages;

@Path("users")
@Transactional(dontRollbackOn = {BadRequestException.class, ForbiddenException.class, NotFoundException.class})
@RequestScoped
public class Users
{
    @PersistenceContext
    private EntityManager em;

    @Resource
    private Validator validator;

    @Context
    private SecurityContext context;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public java.util.List<User> getAllUsers(@QueryParam("from") @DefaultValue("0") int from, @QueryParam("results") @DefaultValue("20") int results)
    {
        TypedQuery<User> q = em.createNamedQuery("User.findAll", User.class);
        q.setFirstResult(from);
        q.setMaxResults(results);
        return q.getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUser(User user)
    {
        Set<ConstraintViolation<User>> violations = validator.validate(user, OnPasswordUpdate.class);
        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        if (em.find(User.class, user.getUsername()) != null) {
            throw new BadRequestException("USER_USERNAME");
        }

        // Because adding users currently does not require authentication, we set the roles to
        // Role.USER and do not allow new admins to be created.
        user.getRoles().clear();
        user.getRoles().add(Role.USER);

        em.persist(user);

        return Response.created(URI.create("/users/" + user.getUsername())).build();
    }

    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("username") String username)
    {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        return user;
    }

    @PUT
    @Path("{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateUser(@PathParam("username") String username, InputStream in)
    {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        em.detach(user);

        boolean passwordChanged = false;

        try {
            JsonObject userUpdate = Json.createReader(in).readObject();

            if (userUpdate.containsKey("fullName")) {
                if (userUpdate.isNull("fullName")) {
                    user.setFullName(null);
                } else {
                    user.setFullName(userUpdate.getString("fullName"));
                }
            }

            JsonArray roles = userUpdate.getJsonArray("roles");
            if (roles != null) {

                // Only admins can change roles.
                if (!context.isUserInRole(Role.ADMINISTRATOR.name())) {
                    throw new ForbiddenException();
                }

                user.getRoles().clear();
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

            if (userUpdate.containsKey("password")) {
                user.setPassword(userUpdate.getString("password"));
                passwordChanged = true;
            }
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }

        Set<ConstraintViolation<User>> violations;
        if (passwordChanged) {
            violations = validator.validate(user, OnPasswordUpdate.class);
        } else {
            violations = validator.validate(user);
        }

        if (!violations.isEmpty()) {
            throw new BadRequestException(mergeMessages(violations));
        }

        em.merge(user);
    }

    @Inject
    private Lists listsResource;
    
    @DELETE
    @Path("{username}")
    public void removeUser(@PathParam("username") String username) throws IOException
    {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        TypedQuery<List> q = em.createNamedQuery("List.findByOwner", List.class).setParameter("owner", user);
        for (List list : q.getResultList()) {
            listsResource.removeList(list.getId());
        }
        Files.deleteIfExists(IMAGES_BASE_DIR.resolve(username + ".png"));
        em.remove(user);
    }

    @GET
    @Path("{username}/picture")
    @Produces("image/png")
    public InputStream getProfilePicture(@PathParam("username") String username) throws IOException
    {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        java.nio.file.Path path = IMAGES_BASE_DIR.resolve(user.getProfilePicture());
        if (!Files.exists(path)) {
            throw new InternalServerErrorException("Could not load profile picture " + user.getProfilePicture());
        }

        return Files.newInputStream(path);
    }

    @PUT
    @Path("{username}/picture")
    @Consumes({"image/jpeg", "image/png"})
    public void setProfilePicture(@PathParam("username") String username, @HeaderParam("Content-Length") long fileSize, InputStream in) throws IOException
    {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        // Make sure the file is not larger than the maximum allowed size.
        if (fileSize > 1024 * 1024 * MAX_PROFILE_PICTURE_SIZE_IN_MB) {
            throw new BadRequestException("USER_PICTURE");
        }

        BufferedImage image = ImageIO.read(in);

        // Scale the image to 200px x 200px.
        BufferedImage scaledImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, 200, 200, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();

        // Save the image. By default, {username}.png is used as the filename.
        OutputStream out = Files.newOutputStream(IMAGES_BASE_DIR.resolve(username + ".png"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        ImageIO.write(scaledImage, "png", out);

        // Don't forget to set it.
        user.setProfilePicture(username + ".png");
    }

    @DELETE
    @Path("{username}/picture")
    public void removeProfilePicture(@PathParam("username") String username) throws IOException
    {
        User user = em.find(User.class, username);

        if (user == null) {
            throw new NotFoundException();
        }

        if (!context.getUserPrincipal().getName().equals(username) && !context.isUserInRole(Role.ADMINISTRATOR.name())) {
            throw new ForbiddenException();
        }

        Files.deleteIfExists(IMAGES_BASE_DIR.resolve(username + ".png"));

        // Clearing the profile picture will reset it to the default profile picture.
        user.setProfilePicture(null);
    }
}
