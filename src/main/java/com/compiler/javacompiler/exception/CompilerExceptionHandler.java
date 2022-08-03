package com.compiler.javacompiler.exception;

import com.compiler.javacompiler.entity.Error;
import com.compiler.javacompiler.logger.CompilerLogger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CompilerExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable ex) {
        CompilerLogger.log(ex.getMessage());
        if(ex instanceof BadInputException) {
            Error errorMessage = new Error(
                Status.BAD_REQUEST.getStatusCode(),
                "Bad Request.",
                System.currentTimeMillis()
            );
            return Response.status(Status.BAD_REQUEST).entity(errorMessage).build();
        } else {
            Error errorMessage = new Error(
                Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Server Error.",
                System.currentTimeMillis()
            );
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }
}