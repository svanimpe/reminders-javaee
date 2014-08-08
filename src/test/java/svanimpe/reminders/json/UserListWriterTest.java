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

package svanimpe.reminders.json;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.GenericEntity;
import org.junit.Test;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

import static org.junit.Assert.*;
import static svanimpe.reminders.util.Utilities.*;

public class UserListWriterTest
{
    private final UserListWriter writer = new UserListWriter();
    private final List<User> users = new ArrayList<>();
    
    @Test
    public void testIsUserListWritable()
    {
        GenericEntity<List<User>> usersEntity = new GenericEntity<List<User>>(users){};   
        assertTrue(writer.isWriteable(usersEntity.getRawType(), usersEntity.getType(), null, null));
    }
    
    @Test
    public void testWriteUserList() throws Exception
    {
        User first = new User();
        first.setUsername("someuser");
        first.setFullName("Some User");
        first.setPassword("supersecret");
        first.getRoles().add(Role.ADMINISTRATOR);

        User second = new User();
        second.setUsername("someotheruser");
        second.setFullName("Some Other User");
        second.setPassword("supersecret");
        second.getRoles().add(Role.USER);
        
        users.add(first);
        users.add(second);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.writeTo(users, null, null, null, null, null, output);

        byte[] expectedOutput = getResourceAsBytes("/json/user/write-list.json");
        assertArrayEquals(expectedOutput, output.toByteArray());
    }
}
