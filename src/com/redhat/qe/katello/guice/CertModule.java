package com.redhat.qe.katello.guice;

import java.lang.annotation.Annotation;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.redhat.qe.katello.ssl.PEMx509KeyManager;

public class CertModule extends ClientFactoryModule {
    @Inject private PEMx509KeyManager[] keyManagers;
    @Inject private TrustManager[] trustManagers;
    @Inject private SecureRandom secureRandom;
    
    public CertModule(Class<? extends Annotation> annotation) {
        super(annotation);
    }

    @Override
    void bindSSLContext() {
//        bind(SSLContext.class);
    }
    
    @Provides
    SSLContext provideCertSSLContext(PEMx509KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom) {
        SSLContext clientContext = null;

        // Initialize the SSLContext to work with our key managers.
        try {
            clientContext = SSLContext.getInstance(SSLSocketFactory.TLS);
            clientContext.init(keyManagers, trustManagers, secureRandom);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clientContext;
    }
}
