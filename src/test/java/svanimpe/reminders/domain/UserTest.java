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
