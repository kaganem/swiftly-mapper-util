package com.swiftly.mapper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonProperty;

@Path("/")
public class LocationResource {
    public static class CoordinatesSnapshot {

        @JsonProperty("LON")
        public double longitude;

        @JsonProperty("LAT")
        public double latitude;

        @JsonProperty("T")
        public long timestamp;
    }

    @POST
    @Path("/coordinates")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(CoordinatesSnapshot coordinatesSnapshot) {
        System.out.println(coordinatesSnapshot.longitude
                + " x " +
                coordinatesSnapshot.latitude
                + " : " +
                coordinatesSnapshot.timestamp);
        return "";
    }

    // @GET
    // @Path("/login")
    // @Produces(MediaType.TEXT_PLAIN)
    // public String login() {
    // try {
    // LoginResult result = sessionService.login("ekagan@gehtsoft.com",
    // "LpZ3IjJIf7OgWNWPLDEAI+B+QBfJTVbMX65LVLRGBfQqdbCx81iZH4xsvptotk5bKgnQrSP8rAfumpGYp0Xh1Q==");
    // if (result.successful) {
    // return result.session;
    // } else {
    // return "failed";
    // }
    // } catch (ResteasyWebApplicationException exception) {
    // if (exception.getResponse().getStatus() == 429) {
    // return "too fast";
    // } else {
    // return "error";
    // }
    // }
    // }

    // @GET
    // @Path("/profile")
    // @Produces(MediaType.TEXT_PLAIN)
    // public String whoAmI(@QueryParam("session") String session) {
    // try {
    // return this.getsSelfTimer.record(() -> {
    // Profile profile = userService.getSelf(session);
    // return profile.toString();
    // });
    // } catch (Exception e) {
    // return "failed";
    // }
    // }
}