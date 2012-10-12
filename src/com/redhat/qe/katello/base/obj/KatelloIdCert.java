package com.redhat.qe.katello.base.obj;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloIdCert {
    public KatelloIdCert() {}
    
    private String cert;
    private String id;
    private String key;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) { 
        this.id = id;
    }
    
    public String getCert() {
        return cert;
    }
    
    public void setCert(String cert) {
        this.cert = cert;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }

}
