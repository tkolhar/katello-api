package com.redhat.qe.katello.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloPool;

@Path("/pools")
public interface PoolResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    ClientResponse<List<KatelloPool>> list();
}
