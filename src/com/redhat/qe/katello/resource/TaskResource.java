package com.redhat.qe.katello.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.client.ClientResponse;
import com.redhat.qe.katello.base.obj.KatelloTask;

//{"href":"/api/tasks/","rel":"tasks"},
@Path("/tasks")
public interface TaskResource {
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<KatelloTask> getTask(@PathParam("id") String id);

}
