package com.redhat.qe.katello.base;

public class KatelloApiResponse {
    protected int returnCode;
    protected String returnMessage;
    protected String content;
    
    public KatelloApiResponse(String content) {
        this(content, -1);
    }
    
    public KatelloApiResponse(String content, int returnCode) {
        this(content, returnCode, "");
    }
    
    public KatelloApiResponse(String content, int returnCode, String returnMessage) {
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
        this.content = content;
    }
    
    public int getReturnCode() {
        return returnCode;
    }
    
    public String getReturnMessage() {
        return returnMessage;
    }
    
    public String getContent() {
        return content;
    }
}
