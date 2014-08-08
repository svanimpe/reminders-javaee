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
