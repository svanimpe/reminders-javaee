package svanimpe.reminders.resources;

import java.net.URL;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import svanimpe.reminders.ArchiveFactory;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
@RunAsClient
public class CredentialsTest
{
    @Deployment
    public static WebArchive createArchive()
    {
        return ArchiveFactory.createArchive();
    }
    
    @ArquillianResource
    private URL base;
    
    private WebTarget target;
    
    @Before
    public void setUp()
    {
        target = ClientBuilder.newClient().target(base.toExternalForm() + "/api/credentials");
    }
    
    @Test
    public void testEmptyUsername()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"password\":\"supersecret\"}"));
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void testEmptyPassword()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":\"someuser\"}"));
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void testUsernameNotAString()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":123,\"password\":\"supersecret\"}"));
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void testPasswordNotAString()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":\"unknownuser\",\"password\":123}"));
        assertEquals(400, response.getStatus());
    }
    
    @Test
    public void testUnknownUser()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":\"unknownuser\",\"password\":\"supersecret\"}"));
        assertEquals(200, response.getStatus());
        assertEquals("[]", response.readEntity(String.class));
    }
    
    @Test
    public void testInvalidPassword()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":\"someuser\",\"password\":\"invalidpassword\"}"));
        assertEquals(200, response.getStatus());
        assertEquals("[]", response.readEntity(String.class));
    }
    
    @Test
    public void testValidCredentialsAdministrator()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":\"someuser\",\"password\":\"supersecret\"}"));
        assertEquals(200, response.getStatus());
        assertEquals("[\"ADMINISTRATOR\"]", response.readEntity(String.class));
    }
    
    @Test
    public void testValidCredentialsUser()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("{\"username\":\"someotheruser\",\"password\":\"supersecret\"}"));
        assertEquals(200, response.getStatus());
        assertEquals("[\"USER\"]", response.readEntity(String.class));
    }
    
    @Test
    public void testInvalidJson()
    {
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json("\"username\":\"someuser\",\"password\":\"supersecret\""));
        assertEquals(400, response.getStatus());
    }
}
