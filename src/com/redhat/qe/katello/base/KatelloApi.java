package com.redhat.qe.katello.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
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
	
	private static URI buildURI(String apiCall, String apiQuery) throws URISyntaxException {
	    URIBuilder builder = new URIBuilder();
	    builder.setScheme("https").setHost(System.getProperty("katello.server.hostname", "localhost"))
        .setPath("/"+System.getProperty("katello.product", "katello")+"/api"+apiCall).setQuery(apiQuery);
	    return builder.build();
	}

    private static KatelloApiResponse _doRequest(Request request) {
        KatelloApiResponse response = null;
        try {
            HttpResponse returnResponse = executor.execute(request.connectTimeout(20000).socketTimeout(20000)).returnResponse();
            HttpEntity entity = returnResponse.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
            StringBuffer buffer = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null ) {
                buffer.append(line);
            }
            reader.close();
            response = new KatelloApiResponse(buffer.toString(), returnResponse.getStatusLine().getStatusCode(), returnResponse.getStatusLine().getReasonPhrase());
        } catch (IOException ex) {
            response = new KatelloApiResponse(ex.getLocalizedMessage());
            ex.printStackTrace();
        } 
        return response;
    }
    
//    public static KatelloApiResponse get(String call) {
//        return get(call, "");
//    }
    
    public static KatelloApiResponse get(String call) {
        return get(call, null);
    }
    
	public static KatelloApiResponse get(String call, String query){
		try{
		    URI uri = buildURI(call, query);
		    return _doRequest(Request.Get(uri));
		} catch(URISyntaxException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static KatelloApiResponse _post(HttpEntity postEntity, String call, String query){
		try{
			URI uri = buildURI(call, query);
            return _doRequest(Request.Post(uri).body(postEntity));
		} catch(URISyntaxException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private static KatelloApiResponse _put(HttpEntity putEntity, String call, String query) {
	    try {
	        URI uri = buildURI(call, query);
	        return _doRequest(Request.Put(uri).body(putEntity));
	    } catch (URISyntaxException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	private static KatelloApiResponse _delete(String call) {
	    try {
	        URI uri = buildURI(call, "");
	        return _doRequest(Request.Delete(uri));
	    } catch (URISyntaxException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	public static KatelloApiResponse postJson(String content, String call, String query) {
	    return post(content, call, query, ContentType.APPLICATION_JSON);
	}
	
	public static KatelloApiResponse post(String content, String call, String query, ContentType contentType) {
	    StringEntity postEntity = new StringEntity(content, contentType);
	    return _post(postEntity, call, query);
	}
	
	public static KatelloApiResponse post(List<NameValuePair> nvp, String call) {
	    return post(nvp, call, null);
	}
	
	public static KatelloApiResponse post(List<NameValuePair> nvp, String call, String query) {
	    StringEntity postEntity = new StringEntity(URLEncodedUtils.format(nvp, "UTF-8"), ContentType.APPLICATION_FORM_URLENCODED);
        log.info(URLEncodedUtils.format(nvp,"UTF-8"));
	    return _post(postEntity, call, query);
	}

	public static KatelloApiResponse post(KatelloPostParam[] params, String call) {
	    return post(params, call, null);
	}
	
	public static KatelloApiResponse post(KatelloPostParam[] params, String call, String query) {
        String content = "";
        for(KatelloPostParam param: params){
            content = String.format("%s%s,", content, param);
        }
        if(content.length()>1) 
            content = content.substring(0,content.length()-1); // cut last symbol - ","
        content = String.format("{%s}", content);
        StringEntity postEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
	    return _post(postEntity, call, query);
	}
	
	public static KatelloApiResponse postFile(String manifest, String call) {
	    FileEntity postEntity = new FileEntity(new File(manifest));
	    return _post(postEntity, call, null);
	}

	public static KatelloApiResponse putJson(String content, String call) {
	    return putJson(content, call, null);
	}
	
	public static KatelloApiResponse putJson(String content, String call, String query) {
	    StringEntity putEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
	    return _put(putEntity,call, query);
	}
	
	public static KatelloApiResponse delete(String call) {
	    return _delete(call);
	}
}
