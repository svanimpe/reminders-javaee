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

import static svanimpe.reminders.util.Utilities.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
@RunAsClient
public class UsersPictureTest
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
    public void testGetPicture() throws Exception
    {
        Response response = target.path("someuser/picture").request("image/png").get();
        assertEquals(200, response.getStatus());
        
        byte[] expectedOutput = getResourceAsBytes("/pictures/default.png");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
    }
    
    @Test @InSequence(2)
    public void testGetPictureUnknownUser()
    {
        Response response = target.path("unknownuser/picture").request("image/png").get();
        assertEquals(404, response.getStatus());
    }
    
    @Test @InSequence(3)
    public void testUpdatePicturePng() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        byte[] input = getResourceAsBytes("/pictures/landscape.png");
        Response response = target.path("someotheruser/picture").request().put(Entity.entity(input, "image/png"));
        assertEquals(204, response.getStatus());
        
        response = target.path("someotheruser/picture").request("image/png").get();
        assertEquals(200, response.getStatus());
        byte[] expectedOutput = getResourceAsBytes("/pictures/landscape_png_converted.png");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
    }
    
    @Test @InSequence(4)
    public void testUpdatePictureJpeg() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        byte[] input = getResourceAsBytes("/pictures/landscape.jpg");
        Response response = target.path("someotheruser/picture").request().put(Entity.entity(input, "image/jpeg"));
        assertEquals(204, response.getStatus());
        
        response = target.path("someotheruser/picture").request("image/png").get();
        assertEquals(200, response.getStatus());
        byte[] expectedOutput = getResourceAsBytes("/pictures/landscape_jpg_converted.png");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
    }
    
    @Test @InSequence(5)
    public void testUpdatePictureTooLarge() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        byte[] input = getResourceAsBytes("/pictures/landscape_large.jpg");
        Response response = target.path("someotheruser/picture").request().put(Entity.entity(input, "image/jpeg"));
        assertEquals(400, response.getStatus());
    }
    
    @Test @InSequence(6)
    public void testUpdatePictureUnknownUser() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        byte[] input = getResourceAsBytes("/pictures/landscape.jpg");
        Response response = target.path("unknownuser/picture").request().put(Entity.entity(input, "image/jpeg"));
        assertEquals(404, response.getStatus());
    }
    
    @Test @InSequence(7)
    public void testUpdatePictureUnauthenticated() throws Exception
    {
        byte[] input = getResourceAsBytes("/pictures/landscape.jpg");
        Response response = target.path("someotheruser/picture").request().put(Entity.entity(input, "image/jpeg"));
        assertEquals(401, response.getStatus());
    }
    
    @Test @InSequence(8)
    public void testUpdatePictureDifferentUserForbidden() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        byte[] input = getResourceAsBytes("/pictures/landscape.jpg");
        Response response = target.path("someuser/picture").request().put(Entity.entity(input, "image/jpeg"));
        assertEquals(403, response.getStatus());
    }
    
    @Test @InSequence(9)
    public void testUpdatePictureDifferentUserAsAdmin() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someuser", "supersecret"));
        byte[] input = getResourceAsBytes("/pictures/landscape.jpg");
        Response response = target.path("someotheruser/picture").request().put(Entity.entity(input, "image/jpeg"));
        assertEquals(204, response.getStatus());
    }
    
    @Test @InSequence(10)
    public void testRemovePicture() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        Response response = target.path("someotheruser/picture").request().delete();
        assertEquals(204, response.getStatus());
        
        response = target.path("someotheruser/picture").request("image/png").get();
        assertEquals(200, response.getStatus());
        byte[] expectedOutput = getResourceAsBytes("/pictures/default.png");
        assertArrayEquals(expectedOutput, response.readEntity(byte[].class));
    }
    
    @Test @InSequence(11)
    public void testRemovePictureUnknownUser() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        Response response = target.path("unknownuser/picture").request().delete();
        assertEquals(404, response.getStatus());
    }
    
    @Test @InSequence(12)
    public void testRemovePictureUnauthenticated() throws Exception
    {
        Response response = target.path("someotheruser/picture").request().delete();
        assertEquals(401, response.getStatus());
    }
    
    @Test @InSequence(13)
    public void testRemovePictureDifferentUserForbidden() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someotheruser", "supersecret"));
        Response response = target.path("someuser/picture").request().delete();
        assertEquals(403, response.getStatus());
    }
    
    @Test @InSequence(14)
    public void testRemovePictureDifferentUserAsAdmin() throws Exception
    {
        target.register(new HttpBasicAuthFilter("someuser", "supersecret"));
        Response response = target.path("someotheruser/picture").request().delete();
        assertEquals(204, response.getStatus());
    }
}
