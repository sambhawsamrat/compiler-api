package com.compiler.javacompiler.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CompilerLogger {

    private static final Logger logger = Logger.getLogger(CompilerLogger.class.getName());

    public static void log(String msg) {
        logger.log(Level.INFO, msg);
    }
}
