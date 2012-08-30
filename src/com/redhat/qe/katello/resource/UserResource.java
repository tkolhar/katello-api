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

import com.redhat.qe.katello.base.obj.KatelloUser;

//{"href":"/api/users/","rel":"users"},
@Path("/users")
public interface UserResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloUser>> list();
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloUser> create(Map<String, Object> user);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    ClientResponse<KatelloUser> get(@PathParam("id") Long userId);
    
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    ClientResponse<String> update(@PathParam("id") Long userId, Map<String,Object> updatedProperties);

    @DELETE
    @Path("/{id}")
    ClientResponse<String> delete(@PathParam("id") Long userId);

}
