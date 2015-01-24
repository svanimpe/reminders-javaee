package svanimpe.reminders.util;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/*
 * Mapper to extract error names from the exception and return them in the response body.
 */
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException>
{
    @Override
    public Response toResponse(BadRequestException exception)
    {
        return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(exception.getMessage()).build();
    }
}
