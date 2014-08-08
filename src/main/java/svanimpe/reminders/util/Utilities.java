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

package svanimpe.reminders.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

public class Utilities
{
    private Utilities()
    {
        throw new UnsupportedOperationException();
    }

    /*
     * The directory where the images will be stored. Make sure this directory exists before you run
     * the application.
     */
    public static final java.nio.file.Path IMAGES_BASE_DIR = Paths.get(System.getProperty("user.home"), "app-root", "runtime", "repo", "diy", "files", "reminders");

    /*
     * The maximum allowed file size in megabytes.
     */
    public static final int MAX_IMAGE_SIZE_IN_MB = 10;
    public static final int MAX_PROFILE_PICTURE_SIZE_IN_MB = 2;
    
    /*
     * Performs string cleanup. Whitespace is trimmed from the front and back. If the resulting
     * string is empty (or was null to start with), this method returns null.
     */
    public static String cleanUp(String input)
    {
        return cleanUp(input, null);
    }

    /*
     * Performs string cleanup. Whitespace is trimmed from the front and back. If the resulting
     * string is empty (or was null to start with), this method returns the given default value.
     */
    public static String cleanUp(String input, String defaultValue)
    {
        return input != null && input.trim().length() > 0 ? input.trim() : defaultValue;
    }

    /*
     * Merges all constraint violation messages into one space-separated message. The messages are
     * sorted in alphabetical order. Duplicates are removed.
     */
    public static <T> String mergeMessages(Set<ConstraintViolation<T>> violations)
    {
        List<String> messages = new ArrayList<>();
        for (ConstraintViolation<T> violation : violations) {
            if (!messages.contains(violation.getMessage())) {
                messages.add(violation.getMessage());
            }
        }
        
        Collections.sort(messages);
        
        StringBuilder result = new StringBuilder();
        for (String message : messages) {
            if (result.length() != 0) {
                result.append(" ");
            }
            result.append(message);
        }
        return result.toString();
    }
    
    /*
     * The following methods make loading resource files less painful.
     */
    
    public static String getResourceAsString(String resourcePath)
    {
        return Utilities.class.getResource(resourcePath).toExternalForm();
    }
    
    public static Path getResourceAsPath(String resourcePath)
    {
        try {
            URI uri = Utilities.class.getResource(resourcePath).toURI();
            return Paths.get(uri);
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    public static InputStream getResourceAsStream(String resourcePath)
    {
        return Utilities.class.getResourceAsStream(resourcePath);
    }
    
    public static byte[] getResourceAsBytes(String resourcePath) throws IOException
    {
        Path path = getResourceAsPath(resourcePath);
        if (path != null) {
            return Files.readAllBytes(getResourceAsPath(resourcePath));
        } else {
            throw new IOException("Resource not found");
        }
    }
}
