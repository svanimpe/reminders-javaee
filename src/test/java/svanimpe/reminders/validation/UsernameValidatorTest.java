package svanimpe.reminders.validation;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class UsernameValidatorTest
{
    @Parameterized.Parameters
    public static List<Object[]> getParameters()
    {
        return Arrays.asList(new Object[][] {
            {null, false},
            {"", false},
            {" \t\n ", false},
            {" username", false},
            {"username ", false},
            {"usernam", false},
            {"us3rname", false},
            {"user@domain.com", false},
            {"username", true},
            {"myawesomeusername", true},
            {"someusernamethatiswaytoolongbutIdontreallyknowanynamelikethatso" + 
             "thatswhyImjusttypingwhatevercomestomindhereletshopeitsovertwo" +
             "hundredandfiftysixcharacterslongsothetestwillfailbutitsprobably" +
             "notsoIbettercopypasteitsomeusernamethatiswaytoolongbutIdont" +
             "reallyknowanynamelikethatsothatswhyImjusttypingwhatevercomesto" +
             "mindhereletshopeitsovertwohundredandfiftysixcharacterslongsothe" +
             "testwillfailbutitsprobablynotsoIbettercopypasteit", false}
        });
    }
    
    private final String username;
    private final boolean expectedResult;

    public UsernameValidatorTest(String username, boolean expectedResult)
    {
        this.username = username;
        this.expectedResult = expectedResult;
    }
    
    @Test
    public void testUsernameValidation()
    {
        UsernameValidator validator = new UsernameValidator();
        assertEquals(expectedResult, validator.isValid(username, null));
    }
}
