package com.swiftly.mapper;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonProperty;

@Path("/coordinates")
public class GreetingResource {
    public static class CoordinatesSnapshot {

        @JsonProperty("LON")
        public double longitude;

        @JsonProperty("LAT")
        public double latitude;

        @JsonProperty("T")
        public long timestamp;
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(CoordinatesSnapshot coordinatesSnapshot) {
        System.out.println(coordinatesSnapshot.longitude
                + " x " +
                coordinatesSnapshot.latitude
                + " : " +
                coordinatesSnapshot.timestamp);
        return "";
    }
}