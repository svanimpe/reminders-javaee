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
import org.junit.Test;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

import static org.junit.Assert.*;
import static svanimpe.reminders.util.Utilities.*;

public class UserWriterTest
{
    private final UserWriter writer = new UserWriter();
    
    @Test
    public void testIsUserWritable()
    {
        assertTrue(writer.isWriteable(User.class, null, null, null));
    }
    
    @Test
    public void testWriteUser() throws Exception
    {
        User user = new User();
        user.setUsername("someuser");
        user.setFullName("Some User");
        user.setPassword("supersecret");
        user.getRoles().add(Role.ADMINISTRATOR);
        user.getRoles().add(Role.USER);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.writeTo(user, null, null, null, null, null, output);
        
        byte[] expectedOutput = getResourceAsBytes("/json/user/write.json");
        assertArrayEquals(expectedOutput, output.toByteArray());
    }
}
