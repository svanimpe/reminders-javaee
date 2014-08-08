/*
 * Copyright (c) 2014, Steven Van Impe
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *     following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package svanimpe.reminders.resources;

import java.net.URL;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import svanimpe.reminders.ArchiveFactory;

import static org.junit.Assert.*;
import static svanimpe.reminders.util.Utilities.*;

@RunWith(Arquillian.class)
@RunAsClient
public class UsersTest
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
        target = ClientBuilder.newClient().target(base.toExternalForm() + "/api/users");
    }
    
    @Test @InSequence(1)
    public void testGetAllUsers() throws Exception
    {
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        
        byte[] expectedOutput = getResourceAsBytes("/users/get-all.json");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
    }
    
    @Test @InSequence(2)
    public void testGetAllUsersWithParameters() throws Exception
    {
        Response response = target.queryParam("from", 1).queryParam("results", 1).request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        
        byte[] expectedOutput = getResourceAsBytes("/users/get-all-params.json");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
    }
    
    @Test @InSequence(3)
    public void testAddUser() throws Exception
    {
        byte[] input = getResourceAsBytes("/users/add.json");
        Response response = target.request().post(Entity.json(input));
        
        assertEquals(201, response.getStatus());
        assertEquals("/users/somenewuser", response.getHeaderString("Location"));
        // This does not test whether the roles were reset to USER.
        // This is tested as part of testGetUser.
    }
    
    @Test @InSequence(4)
    public void testAddInvalidUser() throws Exception
    {
        byte[] input = getResourceAsBytes("/users/add-invalid.json");
        Response response = target.request().post(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(5)
    public void testAddDuplicateUser() throws Exception
    {
        byte[] input = getResourceAsBytes("/users/add-duplicate.json");
        Response response = target.request().post(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(6)
    public void testAddUserInvalidJson() throws Exception
    {
        byte[] input = getResourceAsBytes("/users/add-invalid-json.json");
        Response response = target.request().post(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(7)
    public void testGetUser() throws Exception
    {
        Response response = target.path("/somenewuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        
        byte[] expectedOutput = getResourceAsBytes("/users/get.json");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
        // This doesn't test whether the password was saved correctly.
        // Since we use that password to authenticate in later tests, this is not an issue.
    }
    
    @Test @InSequence(8)
    public void testGetUnknownUser() throws Exception
    {
        Response response = target.path("/unknownuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(404, response.getStatus());
    }
    
    @Test @InSequence(9)
    public void testUpdateUser() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "supersecret"));
        byte[] input = getResourceAsBytes("/users/update.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(204, response.getStatus());
        
        response = target.path("/somenewuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("My New Name"));
        // No need to check whether the new password was saved correctly.
        // That password is used to authenticate in later tests.
    }
    
    @Test @InSequence(10)
    public void testUpdateUserClearFullName() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-clear-name.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(204, response.getStatus());
        
        response = target.path("/somenewuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        assertFalse(response.readEntity(String.class).contains("fullName"));
    }

    @Test @InSequence(11)
    public void testUpdateUserFullNameNotAString() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-invalid-name.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(12)
    public void testUpdateUserRolesForbidden() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-roles.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(403, response.getStatus());
    }
    
    @Test @InSequence(13)
    public void testUpdateUserRolesAsAdmin() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someuser", "supersecret"));
        byte[] input = getResourceAsBytes("/users/update-roles.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(204, response.getStatus());
        
        response = target.path("/somenewuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("ADMINISTRATOR"));
    }

    @Test @InSequence(14)
    public void testUpdateUserInvalidRole() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-invalid-role.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(15)
    public void testUpdateUserRolesNotAnArray() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-invalid-roles.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(16)
    public void testUpdateUserInvalidPassword() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-invalid-password-1.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(17)
    public void testUpdateUserPasswordNotAString() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update-invalid-password-2.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(18)
    public void testUpdateUserUnauthenticated() throws Exception
    {
        byte[] input = getResourceAsBytes("/users/update.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(401, response.getStatus());
    }
    
    @Test @InSequence(19)
    public void testUpdateUnknownUser() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        byte[] input = getResourceAsBytes("/users/update.json");
        Response response = target.path("/unknownuser").request().put(Entity.json(input));
        assertEquals(404, response.getStatus());
    }

    @Test @InSequence(20)
    public void testUpdateDifferentUserForbidden() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        byte[] input = getResourceAsBytes("/users/update.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(403, response.getStatus());
    }
    
    @Test @InSequence(21)
    public void testUpdateDifferentUserAsAdmin() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someuser", "supersecret"));
        byte[] input = getResourceAsBytes("/users/update.json");
        Response response = target.path("/somenewuser").request().put(Entity.json(input));
        assertEquals(204, response.getStatus());
        
        response = target.path("/somenewuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(200, response.getStatus());
        assertTrue(response.readEntity(String.class).contains("My New Name"));
    }

    @Test @InSequence(22)
    public void testRemoveUser() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "mynewpassword"));
        Response response = target.path("/somenewuser").request().delete();
        assertEquals(204, response.getStatus());
        
        response = target.path("/somenewuser").request(MediaType.APPLICATION_JSON).get();
        assertEquals(404, response.getStatus());
    }
    
    @Test @InSequence(23)
    public void testReaddUser() throws Exception
    {
        byte[] input = getResourceAsBytes("/users/add.json");
        Response response = target.request().post(Entity.json(input));
        
        assertEquals(201, response.getStatus());
        assertEquals("/users/somenewuser", response.getHeaderString("Location"));
    }
    
    @Test @InSequence(24)
    public void testRemoveUserUnauthenticated() throws Exception
    {
        Response response = target.path("/somenewuser").request().delete();
        assertEquals(401, response.getStatus());
    }
    
    @Test @InSequence(25)
    public void testRemoveUnkownUser() throws Exception
    {
        target.register(new HttpBasicAuthFilter("somenewuser", "supersecret"));
        Response response = target.path("/unknownuser").request().delete();
        assertEquals(404, response.getStatus());
    }
    
    @Test @InSequence(26)
    public void testRemoveDifferentUserForbidden() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        Response response = target.path("/somenewuser").request().delete();
        assertEquals(403, response.getStatus());
    }
    
    @Test @InSequence(27)
    public void testRemoveDifferentUserAsAdmin() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someuser", "supersecret"));
        Response response = target.path("/somenewuser").request().delete();
        assertEquals(204, response.getStatus());
    }
}
