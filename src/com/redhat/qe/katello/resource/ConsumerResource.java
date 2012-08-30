package com.redhat.qe.katello.resource;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloEntitlement;
import com.redhat.qe.katello.base.obj.KatelloEntitlementCertificate;
import com.redhat.qe.katello.base.obj.KatelloSerial;
import com.redhat.qe.katello.base.obj.KatelloSystem;

//{"href":"/api/consumers/","rel":"consumers"},
@Path("/consumers")
public interface ConsumerResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ClientResponse<KatelloSystem> create(@QueryParam("owner") String orgName, Map<String, Object> sFacts);
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public ClientResponse<KatelloSystem> get(@PathParam("id") String id);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public ClientResponse<KatelloSystem> update(@PathParam("id") String consumerId, Map<String, Object> updFacts);
    
    @DELETE
    @Path("/{id}")
    public ClientResponse<String> delete(@PathParam("id") String consumerId);
    
    @DELETE
    @Path("/{id}/certificates/{serial}")
    public ClientResponse<KatelloSystem> unsubscribe(@PathParam("id") String consumerId, @PathParam("serial") String serial);

    @DELETE
    @Path("/{id}/entitlements")
    public ClientResponse<KatelloSystem> unsubscribe(@PathParam("id") String consumerId);

    @GET
    @Path("/{id}/certificates/")
    @Produces(MediaType.APPLICATION_JSON)
    public ClientResponse<List<KatelloEntitlementCertificate>> listCertificates(@PathParam("id") String consumerId);
    
    @GET
    @Path("/{id}/certificates/serials/")
    @Produces(MediaType.APPLICATION_JSON)
    public ClientResponse<List<KatelloSerial>> listSerials(@PathParam("id") String consumerId);

    @POST
    @Path("/{id}/entitlements")
    @Produces(MediaType.APPLICATION_JSON)
    public ClientResponse<List<KatelloEntitlement>> subscribe(@PathParam("id") String consumerId, @QueryParam("pool") String poolId);
}
