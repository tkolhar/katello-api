package com.redhat.qe.katello.base;

import org.jboss.resteasy.client.ClientResponse;

public class KatelloApiException extends Throwable {
    private static final long serialVersionUID = -4484368782245454247L;
    private int returnCode;
    private String message;
    
    public KatelloApiException(KatelloApiResponse response) {
        this.returnCode = response.getReturnCode();
        this.message = response.getReturnMessage();
    }

    public KatelloApiException(ClientResponse<?> response) {
        this.returnCode = response.getResponseStatus().getStatusCode();
        this.message = response.getResponseStatus().getReasonPhrase();
    }

    public int getReturnCode() {
        return returnCode;
    }
    
    public String getMessage() {
        return message;
    }
}
