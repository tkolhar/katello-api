package com.redhat.qe.katello.base.obj;

public class KatelloOwner {
    private String id;
    private String key;
    private String displayName;
    private String href;
    
    public KatelloOwner() {} // For resteasy
    
    public KatelloOwner(String id, String key, String displayName, String href) {
        this.id = id;
        this.key = key;
        this.displayName = displayName;
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHref() {
        return href;
    }
    
    public void setHref(String href) {
        this.href = href;
    }
}
