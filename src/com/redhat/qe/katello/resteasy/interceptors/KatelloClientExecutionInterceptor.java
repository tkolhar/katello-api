package com.redhat.qe.katello.resteasy.interceptors;

import java.util.logging.Logger;

import javax.ws.rs.ext.Provider;

import net.oauth.signature.pem.PEMReader;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.redhat.qe.katello.ssl.KatelloPemThreadLocal;
import com.redhat.qe.katello.ssl.PEMx509KeyManager;

@Provider
@ClientInterceptor
public class KatelloClientExecutionInterceptor implements ClientExecutionInterceptor {
    private final PEMx509KeyManager keyManager;
    private final AuthCache authCache;
    private final String username;
    private final String password;
    private final String hostname;
    private final int port;
    @Inject protected Logger log;
    private String pem = null;
    
    @Inject 
    KatelloClientExecutionInterceptor(PEMx509KeyManager[] keyManagers, AuthCache authCache, @Named("katello.api.user") String username, @Named("katello.api.password") String password,
            @Named("katello.server.hostname") String hostname, @Named("katello.server.port") int port) {
        this.keyManager = keyManagers[0];
        this.authCache = authCache;
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
    }
    
    @Override
    public ClientResponse<?> execute(ClientExecutionContext ctx) throws Exception {        
        ApacheHttpClient4Executor executor = (ApacheHttpClient4Executor)ctx.getRequest().getExecutor();
        DefaultHttpClient client = (DefaultHttpClient)executor.getHttpClient();
        
        if ( pem == null ) {
            pem = KatelloPemThreadLocal.get();
            if ( pem != null ) {
                log.fine("Cert is: \n" + pem);
                int endOfFirstPart = pem.indexOf("\n", pem.indexOf("END"));
                if (endOfFirstPart == -1) {
                    throw new IllegalArgumentException("unable to parse PEM data");
                }
                String certificate = pem.substring(0, endOfFirstPart);
                String privateKey = pem.substring(endOfFirstPart);
                if (!certificate.startsWith(PEMReader.CERTIFICATE_X509_MARKER)) {
                    String tmp = privateKey;
                    privateKey = certificate;
                    certificate = tmp;
                }

                keyManager.addPEM(certificate, privateKey);
            }
        }
        
        if ( pem != null ) {           
            executor.getHttpContext().removeAttribute(ClientContext.AUTH_CACHE);
            client.getCredentialsProvider().clear();
        } else {
            log.fine("Adding basic auth info");
            HttpHost targetHost = new HttpHost(hostname, port, "https");
            client.getCredentialsProvider().setCredentials(
                    new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
                    new UsernamePasswordCredentials(username, password));

            // Generate BASIC scheme object and add it to the local auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            executor.getHttpContext().setAttribute(ClientContext.AUTH_CACHE, authCache); 
        }
        
        return ctx.proceed();
    }

}
