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

package svanimpe.reminders.domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class UserTest
{
    @Test
    public void testSetUsernameTrimsWhitespace()
    {
        User user = new User();
        user.setUsername(" me ");
        assertEquals("me", user.getUsername());
    }
    
    @Test
    public void testSetUsernameUsesNullAsEmpty()
    {
        User user = new User();
        user.setUsername(" ");
        assertNull(user.getUsername());
    }
    
    @Test
    public void testSetFullNameTrimsWhitespace()
    {
        User user = new User();
        user.setFullName(" me ");
        assertEquals("me", user.getFullName());
    }
    
    @Test
    public void testSetFullNameUsesNullAsEmpty()
    {
        User user = new User();
        user.setFullName(" ");
        assertNull(user.getFullName());
    }
    
    @Test
    public void testSetProfilePictureTrimsWhitespace()
    {
        User user = new User();
        user.setProfilePicture(" mypic.png ");
        assertEquals("mypic.png", user.getProfilePicture());
    }
    
    @Test
    public void testSetProfilePictureUsesDefaultAsEmpty()
    {
        User user = new User();
        
        user.setProfilePicture(" ");
        assertEquals(User.DEFAULT_PROFILE_PICTURE, user.getProfilePicture());
        
        user.setProfilePicture(null);
        assertEquals(User.DEFAULT_PROFILE_PICTURE, user.getProfilePicture());
    }
    
    @Test
    public void testSetPasswordTrimsWhitespace()
    {
        User user = new User();
        user.setPassword(" supersecret ");
        // Should have encrypted "supersecret".
        assertEquals("f75778f7425be4db0369d09af37a6c2b9a83dea0e53e7bd57412e4b060e607f7", user.getPassword());
    }
    
    @Test
    public void testSetPasswordAvoidsNull()
    {
        User user = new User();
        user.setPassword(null);
        // Should have encrypted "".
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", user.getPassword());
    }
    
    @Test
    public void testEqualByUsername()
    {
        User first = new User();
        first.setUsername("me");
        
        User second = new User();
        second.setUsername("me");
        
        assertTrue(first.equals(second));
        
        second.setUsername("you");
        
        assertFalse(first.equals(second));
    }
    
    @Test
    public void testHashOnUsername()
    {
        User first = new User();
        first.setUsername("me");
        
        User second = new User();
        second.setUsername("me");
        
        assertEquals(first.hashCode(), second.hashCode());
        
        second.setUsername("you");
        
        assertNotEquals(first.hashCode(), second.hashCode());
    }
}
