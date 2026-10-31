package com.koperek.bin.mastercard;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "bin-lookup")
@RegisterProvider(RequestIdClientFilter.class)
@RegisterProvider(MastercardOAuthFilter.class)
@Path("/bin-ranges")
public interface MastercardBinLookupClient {

    @POST
    @Path("/account-searches")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    String search(String body);
}
