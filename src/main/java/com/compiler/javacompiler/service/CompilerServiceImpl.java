package com.compiler.javacompiler.service;

import com.compiler.javacompiler.entity.Input;
import com.compiler.javacompiler.entity.Output;

public class CompilerServiceImpl implements CompilerService {

    @Override
    public Output execute(Input testCaseInput){
        CompilerServiceUtil util = new CompilerServiceUtil(testCaseInput);
        return util.execute();
    }
}
