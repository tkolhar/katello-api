package com.redhat.qe.katello.resource;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloSystem;

//[{"href":"/api/systems/","rel":"systems"},
@Path("/systems")
public interface SystemResource {
    @POST
    @Path("/{id}/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloSystem> subscribe(@PathParam("id") String id, Map<String,Object> pool);
}
