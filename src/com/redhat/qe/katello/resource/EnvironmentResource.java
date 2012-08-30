package com.redhat.qe.katello.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloEnvironment;

//{"href":"/api/environments/","rel":"environments"},
@Path("/organization/{id}/environments")
public interface EnvironmentResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloEnvironment> get(String id, String envId);
}
