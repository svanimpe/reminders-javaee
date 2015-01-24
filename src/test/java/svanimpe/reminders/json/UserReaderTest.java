package svanimpe.reminders.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.ws.rs.BadRequestException;
import org.junit.Test;
import svanimpe.reminders.domain.Role;
import svanimpe.reminders.domain.User;

import static org.junit.Assert.*;
import static svanimpe.reminders.util.Utilities.*;

public class UserReaderTest
{
    private final UserReader reader = new UserReader();
    
    @Test
    public void testIsUserReadable()
    {
        assertTrue(reader.isReadable(User.class, null, null, null));
    }
    
    @Test
    public void testReadUser() throws Exception
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read.json"));
        
        assertEquals("someuser", result.getUsername());
        assertEquals("Some User", result.getFullName());
        assertEquals("f75778f7425be4db0369d09af37a6c2b9a83dea0e53e7bd57412e4b060e607f7", result.getPassword());
        assertArrayEquals(new Role[] {Role.ADMINISTRATOR, Role.USER}, result.getRoles().toArray());
    }
    
    @Test(expected = BadRequestException.class)
    public void testUsernameNotAString() throws IOException
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read-invalid-username.json"));
    }
    
    @Test(expected = BadRequestException.class)
    public void testFullNameNotAString() throws IOException
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read-invalid-fullname.json"));
    }
    
    @Test(expected = BadRequestException.class)
    public void testPasswordNotAString() throws IOException
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read-invalid-password.json"));
    }
    
    @Test(expected = BadRequestException.class)
    public void testInvalidRole() throws IOException
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read-invalid-role-1.json"));
    }
    
    @Test(expected = BadRequestException.class)
    public void testRoleNotAString() throws IOException
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read-invalid-role-2.json"));
    }
    
    @Test(expected = BadRequestException.class)
    public void testRolesNotAnArray() throws IOException
    {
        User result = reader.readFrom(User.class, null, null, null, null, getResourceAsStream("/json/user/read-invalid-roles.json"));
    }
    
    @Test(expected = BadRequestException.class)
    public void testInvalidJson() throws IOException
    {
        String invalidJson = "{]";
        User result = reader.readFrom(User.class, null, null, null, null, new ByteArrayInputStream(invalidJson.getBytes()));
    }
}
