package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class KatelloEntitlement {
    private String id;
    private KatelloPool pool;
    private List<KatelloEntitlementCertificate> certificates = new ArrayList<KatelloEntitlementCertificate>();
//    private Date startDate;
//    private Date endDate;
//    private Integer quantity;
//    private String accountNumber;
//    private String contractNumber;

    public KatelloEntitlement() {}
//    public KatelloEntitlement(JSONObject json) {
//        this.id = (String)json.get("id");
//        this.pool = new KatelloPool((JSONObject)json.get("pool"));
//        JSONArray certs = (JSONArray)json.get("certificates");
//        for (int i = 0; i < certs.size(); ++i) {
//            JSONObject cert = (JSONObject)certs.get(i);
//            certificates.add(new KatelloEntitlementCertificate((String)cert.get("id")));
//        }
//    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public KatelloPool getPool() {
        return pool;
    }
    
    public void setPool(KatelloPool pool) {
        this.pool = pool;
    }
    
    public List<KatelloEntitlementCertificate> getCertificates() {
        return certificates;
    }
    
    public void setCertificates(List<KatelloEntitlementCertificate> certificates) {
        this.certificates = certificates;
    }
}
