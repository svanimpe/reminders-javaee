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

import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

/*
 * See the API docs for more info on how to use this resource.
 */
@Path("credentials")
@Transactional(dontRollbackOn = {BadRequestException.class})
public class Credentials
{
    @PersistenceContext
    private EntityManager em;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String checkCredentials(InputStream in)
    {
        try {
            JsonObject credentials = Json.createReader(in).readObject();

            String username = null;
            String password = null;
            
            if (credentials.containsKey("username")) {
                username = credentials.getString("username");
            }
            
            if (credentials.containsKey("password")) {
                password = credentials.getString("password");
            }

            if (username == null) {
                // Username is required.
                throw new BadRequestException("CREDENTIALS_USERNAME");
            }

            if (password == null) {
                // Password is required.
                throw new BadRequestException("CREDENTIALS_PASSWORD");
            }

            User existingUser = em.find(User.class, username);

            if (existingUser == null) {
                return "[]";
            }

            User tempUser = new User();
            tempUser.setPassword(password);

            if (!existingUser.getPassword().equals(tempUser.getPassword())) {
                return "[]";
            }

            JsonArrayBuilder roles = Json.createArrayBuilder();
            for (Role role : existingUser.getRoles()) {
                roles.add(role.name());
            }
            return roles.build().toString();
            
        } catch (JsonException | ClassCastException ex) {
            // Invalid JSON or type mismatch.
            throw new BadRequestException("JSON");
        }
    }
}
