package com.redhat.qe.katello.resource;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;

import com.redhat.qe.katello.base.obj.KatelloRepo;

//{"href":"/api/repositories/","rel":"repositories"},
@Path("/repositories")
public interface RepositoryResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ClientResponse<KatelloRepo> create(Map<String, String> repository);
}
