package com.redhat.qe.katello.resource;

import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;

//{"href":"/api/providers/","rel":"providers"},
@Path("/providers")
public interface ProviderResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloProvider>> list();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloProvider> create(Map<String,Object> provider);
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/product_create")
    ClientResponse<KatelloProduct> create(@PathParam("id") Long id, Map<String,Object> product);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/import_products")
    ClientResponse<List<KatelloProduct>> import_products(@PathParam("id") Long id, Map<String, Object> products);

    @DELETE
    @Path("/{id}")
    ClientResponse<String> delete(@PathParam("id") Long provider_id);

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloProvider> update(@PathParam("id") Long providerId, Map<String, Object> updProv);
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{id}/import_manifest")
    ClientResponse<String> import_manifest(@PathParam("id") Long id, @PartType(MediaType.APPLICATION_OCTET_STREAM) Map<String, DataSource>  parts);
}
