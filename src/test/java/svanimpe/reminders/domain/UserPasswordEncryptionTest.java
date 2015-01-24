package svanimpe.reminders.domain;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class UserPasswordEncryptionTest
{
    @Parameterized.Parameters
    public static List<Object[]> getParameters()
    {
        return Arrays.asList(new Object[][] {
            {null, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
            {"", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"},
            {"5μP3R §±!@#$%^&*()-_+=`~,<.>/?;:'\"\\|[{]} 53cR3t", "28ad3023251337bc43e2a24a85bfdb56e04e67164c7035cf9dfa139c70913973"}
        });
    }
    
    private final String password;
    private final String expectedResult;

    public UserPasswordEncryptionTest(String password, String expectedResult)
    {
        this.password = password;
        this.expectedResult = expectedResult;
    }
    
    @Test
    public void testPasswordEncryption()
    {
        User user = new User();
        user.setPassword(password);
        assertEquals(expectedResult, user.getPassword());
    }
}
