package svanimpe.reminders.util;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UtilitiesMergeTest
{    
    @Mock
    private ConstraintViolation<Object> violation1;
    
    @Mock
    private ConstraintViolation<Object> violation2;
    
    @Mock
    private ConstraintViolation<Object> violation3;
    
    @Mock
    private ConstraintViolation<Object> violation4;
    
    private final Set<ConstraintViolation<Object>> violations = new HashSet<>();
    
    @Before
    public void setUp()
    {
        when(violation1.getMessage()).thenReturn("DR_1");
        when(violation2.getMessage()).thenReturn("DR_2");
        when(violation3.getMessage()).thenReturn("DR_3");
        when(violation4.getMessage()).thenReturn("DR_1");
        
        violations.add(violation1);
        violations.add(violation2);
        violations.add(violation3);
    }
    
    @Test
    public void testMergeMessages()
    {
        assertEquals("DR_1 DR_2 DR_3", Utilities.mergeMessages(violations));
    }
    
    @Test
    public void testMergeMessagesRemovesDuplicates()
    {
        violations.add(violation4);
        
        assertEquals("DR_1 DR_2 DR_3", Utilities.mergeMessages(violations));
    }
}
