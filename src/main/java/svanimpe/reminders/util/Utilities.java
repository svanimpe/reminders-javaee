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
    public static final java.nio.file.Path IMAGES_BASE_DIR = Paths.get(System.getProperty("user.home"), "Workspace", "GlassFish Files", "Reminders");

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
