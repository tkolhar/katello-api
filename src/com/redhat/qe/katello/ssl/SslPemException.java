package com.redhat.qe.katello.ssl;

public class SslPemException extends Exception {
    private static final long serialVersionUID = 6929796710030763263L;

    public SslPemException(String message, Exception exception) {
        super(message, exception);
    }

}
