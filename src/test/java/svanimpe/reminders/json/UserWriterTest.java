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
