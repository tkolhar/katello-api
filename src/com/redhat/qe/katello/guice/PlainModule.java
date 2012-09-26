package com.redhat.qe.katello.guice;

import java.lang.annotation.Annotation;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.google.inject.Provides;

public class PlainModule extends ClientFactoryModule {
    public PlainModule(Class<? extends Annotation> annotation) {
        super(annotation);
    }
    
    @Override
    void bindSSLContext() {
//        bind(SSLContext.class);
    }
    
    @Provides
    SSLContext provideSSLContext(TrustManager[] trustManagers, SecureRandom secureRandom) {
        SSLContext clientContext = null;

        // Initialize the SSLContext to work with our key managers.
        try {
            clientContext = SSLContext.getInstance(SSLSocketFactory.TLS);
            clientContext.init(null, trustManagers, secureRandom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return clientContext;
    }
}
