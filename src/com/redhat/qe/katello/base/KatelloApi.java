package com.redhat.qe.katello.base;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

public class KatelloApi{
    protected static Logger log = Logger.getLogger(KatelloApi.class.getName());
    
	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
    public static ClientExecutor createExecutor() {
        String userId = System.getProperty("katello.admin.user", "admin");
        String password = System.getProperty("katello.admin.password", "admin");
        HttpParams params = new BasicHttpParams();

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            // set up a TrustManager that trusts everything
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }
            } }, new SecureRandom());
            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            Scheme httpsScheme = new Scheme("https", 443, sf);

            schemeRegistry.register(httpsScheme);            
        } catch (Exception e) {
            System.err.println("HttpClient: Scheme not initialized properly");
            e.printStackTrace();
        }

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setDefaultMaxPerRoute(10);
        String hostname = System.getProperty("katello.server.hostname", "localhost");
        int port = Integer.valueOf(System.getProperty("katello.server.port", "443"));
        HttpHost targetHost = new HttpHost(hostname, port, "https");
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
                new UsernamePasswordCredentials(userId, password));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache); 
            
        return new ApacheHttpClient4Executor(httpClient, localcontext);     
    }
	
}
