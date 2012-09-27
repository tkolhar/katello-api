package com.redhat.qe.katello.guice;

import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class KatelloApiModule extends AbstractModule {
    @Inject Logger log;
    
    @Override
    protected void configure() {
        install(new PlainModule(PlainSSLContext.class));
        install(new CertModule(CertSSLContext.class));
    }

}
