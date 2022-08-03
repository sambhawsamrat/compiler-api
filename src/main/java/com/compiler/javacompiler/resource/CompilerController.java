package com.compiler.javacompiler.resource;

import com.compiler.javacompiler.entity.Input;
import com.compiler.javacompiler.entity.Output;
import com.compiler.javacompiler.exception.BadInputException;
import com.compiler.javacompiler.logger.CompilerLogger;
import com.compiler.javacompiler.service.CompilerService;
import com.compiler.javacompiler.service.CompilerServiceImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/compiler")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CompilerController {

    private static final CompilerService compilerService = new CompilerServiceImpl();

    @POST
    @Path("/run")
    public Output compileAndRun(Input input) {
        CompilerLogger.log(input.toString());

        if(input.getCode().length() < 10 || input.getCode().length() > 51200)
            throw new BadInputException("Invalid code.");

        if(input.getInput().length() > 51200)
            throw new BadInputException("Invalid input.");

        return compilerService.execute(input);
    }
}
