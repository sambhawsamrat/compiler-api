package com.compiler.javacompiler.service;

import com.compiler.javacompiler.entity.Input;
import com.compiler.javacompiler.entity.Output;

public interface CompilerService {

    Output execute(Input testCaseInput);
}
