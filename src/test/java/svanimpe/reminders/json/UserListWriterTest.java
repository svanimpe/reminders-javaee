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
