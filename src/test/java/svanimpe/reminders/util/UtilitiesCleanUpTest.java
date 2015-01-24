package svanimpe.reminders.util;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class UtilitiesCleanUpTest
{
    private static final String defaultValue = "default";
    
    @Parameterized.Parameters
    public static List<Object[]> getParameters()
    {
        return Arrays.asList(new Object[][] {
            {null, defaultValue},
            {"", defaultValue},
            {" ", defaultValue},
            {"\t", defaultValue},
            {"\n", defaultValue},
            {"x", "x"},
            {"  x", "x"},
            {"x  ", "x"},
            {"  x  ", "x"}
        });
    }
        
    private final String value;
    private final String expectedResult;
    
    public UtilitiesCleanUpTest(String value, String expectedResult)
    {
        this.value = value;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testCleanUpWithDefault()
    {
        assertEquals(expectedResult, Utilities.cleanUp(value, defaultValue));
    }
    
    @Test
    public void testCleanUpWithoutDefault()
    {
        if (expectedResult.equals(defaultValue)) {
            assertEquals(null, Utilities.cleanUp(value));
        } else {
            assertEquals(expectedResult, Utilities.cleanUp(value));
        }
    }
}
