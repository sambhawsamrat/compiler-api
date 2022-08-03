package com.compiler.javacompiler.entity;

public class Output {

    private int statusCode;
    private String statusMessage;
    private String outputText;

    public Output() {
    }

    public Output(int statusCode, String statusMessage, String outputText) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.outputText = outputText;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    @Override
    public String toString() {
        return "[statusCode=" + statusCode + ", statusMessage=`" + statusMessage + "`, outputText=`" + outputText + "`]";
    }
}
