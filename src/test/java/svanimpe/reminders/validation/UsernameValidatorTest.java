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
