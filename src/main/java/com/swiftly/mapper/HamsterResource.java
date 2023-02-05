package com.swiftly.mapper;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/hamster")
public class HamsterResource {

    @Inject
    HamsterService hamsterService;

    @POST
    @Path("/spawn")
    @Produces(MediaType.TEXT_PLAIN)
    public void spawn(@QueryParam("count") int count, @QueryParam("interval") int interval) {
        hamsterService.spawnHamsters(count, interval);
    }

    @POST
    @Path("/kill")
    @Produces(MediaType.TEXT_PLAIN)
    public void kill(@QueryParam("count") int count) {
        hamsterService.killHamsters(count);
    }

}
