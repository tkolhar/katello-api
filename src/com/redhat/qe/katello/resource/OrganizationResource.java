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
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;

//{"href":"/api/organizations/","rel":"organizations"},
@Path("/organizations")
public interface OrganizationResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloOrg>> list();
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloOrg> getOrganization(@PathParam("id") String id);

    @GET
    @Path("/{id}/environments")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloEnvironment>> listEnvironments(@PathParam("id") String orgId);
    
    @DELETE
    @Path("/{id}/environments/{envId}")
    ClientResponse<String> deleteEnvironment(@PathParam("id") String orgId, @PathParam("envId") Long envId);
    
    @PUT
    @Path("/{id}/environments/{envId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloEnvironment> updateEnvironment(@PathParam("id") String id, @PathParam("envId") Long envId, Map<String,Object> updEnv);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloOrg> create(Map<String, Object> orgPost);

    @POST
    @Path("/{id}/environments/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloEnvironment> createEnvironment(@PathParam("id") String cpKey, Map<String, Object> env);

    @GET
    @Path("/{id}/products/")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloProduct>> listProducts(@PathParam("id") String cpKey);

    @GET
    @Path("/{id}/providers/")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloProvider>> listProviders(@PathParam("id") String cpKey);
}
