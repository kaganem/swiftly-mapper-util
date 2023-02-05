package com.swiftly.mapper;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@RegisterRestClient(baseUri = "https://dev-api.swiftly.bz/api/session")
public interface SwiftlySessionService {

    @GET
    @Path("/login")
    LoginResult login(
            @QueryParam("user") String user,
            @QueryParam("hash") String passwordHash);

    class LoginResult {
        public Boolean successful;
        public String session;
        public String role;
        public String user;
        public String suggestion;
        public Boolean justCreated;

        @Override
        public String toString() {
            return "LoginResult [successful=" + successful + ", session=" + session + ", role=" + role + ", user="
                    + user + ", suggestion=" + suggestion + ", justCreated=" + justCreated + "]";
        }

    }

}
