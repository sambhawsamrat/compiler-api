package com.compiler.javacompiler.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Input {

    private String code;
    private String input;

    public Input() {
    }

    public Input(String code, String input) {
        this.code = code;
        this.input = input;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    @Override
    public String toString() {
        return "\n\n[\ncode=\n`" + code + "`" + ",\ninput=\n`" + input + "`" + "\n]\n";
    }
}
