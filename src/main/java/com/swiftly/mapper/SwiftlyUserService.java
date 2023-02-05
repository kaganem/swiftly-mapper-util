package com.swiftly.mapper;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

@RegisterRestClient(baseUri = "https://dev-api.swiftly.bz/api/user")
public interface SwiftlyUserService {

    @GET
    @Path("/get-self")
    Profile getSelf(@HeaderParam("x-swiftly-session-id") String session);

    class Profile {
        public Long id;
        public String name;
        public String email;
        public String phone;
        public String role;
        public Long profileId;
        public String personName;

        @Override
        public String toString() {
            return "Profile [id=" + id + ", name=" + name + ", email=" + email + ", phone=" + phone + ", role=" + role
                    + ", profileId=" + profileId + ", personName=" + personName + "]";
        }

    }
}
