package com.redhat.qe.katello.base;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

@Provider
@ClientInterceptor
public class KatelloClientInterceptor implements ClientExecutionInterceptor {

@Override
  public ClientResponse execute(ClientExecutionContext ctx) throws Exception {
      final ClientResponse response = ctx.proceed();
//      response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      return response;
  }
}

