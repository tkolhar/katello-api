package com.redhat.qe.katello.base;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;

public class KatelloApi{
    private static Executor executor;
    protected static Logger log = Logger.getLogger(KatelloApi.class.getName());
    
	static{new com.redhat.qe.auto.testng.TestScript();}// to make properties be initialized (if they don't still)
	
	static {
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

            Executor.registerScheme(httpsScheme);
        } catch (Exception e) {
            System.err.println("HttpClient: Scheme not initialized properly");
            e.printStackTrace();
        }
        executor = Executor.newInstance()
                .auth(new HttpHost(System.getProperty("katello.server.hostname", "localhost"),443,"https"), System.getProperty("katello.admin.username", "admin"), System.getProperty("katello.admin.password", "admin"))
                .authPreemptive(new HttpHost(System.getProperty("katello.server.hostname", "localhost"),443,"https"));
	}
	
	private static URI buildURI(String apiCall) throws URISyntaxException {
	    URIBuilder builder = new URIBuilder();
	    builder.setScheme("https").setHost(System.getProperty("katello.server.hostname", "localhost"))
        .setPath("/"+System.getProperty("katello.product", "katello")+"/api"+apiCall);
	    return builder.build();
	}

    private static String _doRequest(Request request) {
        String responseString = null;
        try {
            responseString = executor.execute(request.connectTimeout(1000).socketTimeout(1000)).returnContent().asString();
        } catch (Exception ex) {
            ex.printStackTrace();
            if ( ex instanceof HttpResponseException ) {
                responseString = ex.getMessage();
            }
        }
        return responseString;
    }
    
	public static String get(String call){
		try{
		    URI uri = buildURI(call);
		    return _doRequest(Request.Get(uri));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static String _post(HttpEntity postEntity, String call){
		try{
			URI uri = buildURI(call);
			return _doRequest(Request.Post(uri).body(postEntity));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static String _put(HttpEntity putEntity, String call) {
	    try {
	        URI uri = buildURI(call);
	        return _doRequest(Request.Put(uri).body(putEntity));
	    } catch (URISyntaxException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	private static String _delete(String call) {
	    try {
	        URI uri = buildURI(call);
	        return _doRequest(Request.Delete(uri));
	    } catch (URISyntaxException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	public static String postJson(String content, String call) {
	    return post(content, call, ContentType.APPLICATION_JSON);
	}
	
	public static String post(String content, String call, ContentType contentType) {
	    StringEntity postEntity = new StringEntity(content, contentType);
	    return _post(postEntity, call);
	}
	
	public static String post(List<NameValuePair> nvp, String call) {
	    StringEntity postEntity = new StringEntity(URLEncodedUtils.format(nvp, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
        log.info(URLEncodedUtils.format(nvp,"UTF-8"));
	    return _post(postEntity, call);
	}

	public static String post(KatelloPostParam[] params, String call) {
        String content = "";
        for(KatelloPostParam param: params){
            content = String.format("%s%s,", content, param);
        }
        if(content.length()>1) 
            content = content.substring(0,content.length()-1); // cut last symbol - ","
        content = String.format("{%s}", content);
        StringEntity postEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
	    return _post(postEntity, call);
	}
	
	public static String postFile(String manifest, String call) {
	    FileEntity postEntity = new FileEntity(new File(manifest));
	    return _post(postEntity, call);
	}

	public static String putJson(String content, String call) {
	    StringEntity putEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
	    return _put(putEntity,call);
	}
	
	public static String delete(String call) {
	    return _delete(call);
	}
}
