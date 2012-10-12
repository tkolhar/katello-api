package com.redhat.qe.katello.ssl;

public class KatelloPemThreadLocal {
    public static final ThreadLocal<String> userThreadLocal = new ThreadLocal<String>();

    public static void set(String pem) {
        userThreadLocal.set(pem);
    }

    public static void unset() {
        userThreadLocal.remove();
    }

    public static String get() {
        return userThreadLocal.get();
    }

}
