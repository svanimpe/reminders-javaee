package svanimpe.reminders.validation;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class PasswordValidatorTest
{
    @Parameterized.Parameters
    public static List<Object[]> getParameters()
    {
        return Arrays.asList(new Object[][] {
            {null, false},
            {"", false},
            {" \t\n ", false},
            {" password", false},
            {"password ", false},
            {"passwor", false},
            {"password", true},
            {"P4$$vv0rD", true},
            {"super - secret - password", true},
            {"somepasswordthatiswaytoolongbutIdontreallyknowanywordlikethatso" + 
             "thatswhyImjusttypingwhatevercomestomindhereletshopeitsovertwo" +
             "hundredandfiftysixcharacterslongsothetestwillfailbutitsprobably" +
             "notsoIbettercopypasteitsomepasswordthatiswaytoolongbutIdont" +
             "reallyknowanywordlikethatsothatswhyImjusttypingwhatevercomesto" +
             "mindhereletshopeitsovertwohundredandfiftysixcharacterslongsothe" +
             "testwillfailbutitsprobablynotsoIbettercopypasteit", false}
        });
    }
    
    private final String password;
    private final boolean expectedResult;

    public PasswordValidatorTest(String password, boolean expectedResult)
    {
        this.password = password;
        this.expectedResult = expectedResult;
    }
    
    @Test
    public void testPasswordValidation()
    {
        PasswordValidator validator = new PasswordValidator();
        assertEquals(expectedResult, validator.isValid(password, null));
    }
}
