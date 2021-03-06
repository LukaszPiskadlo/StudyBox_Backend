package com.bls.patronage.auth;

import com.bls.patronage.resources.ResetPasswordResource;
import com.bls.patronage.resources.UsersResource;
import org.glassfish.jersey.internal.util.Base64;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION - 100)
public class PreAuthenticationFilter implements ContainerRequestFilter {

    public static String defultUserEmail = "anon";
    public static String defultUserPassword = "password";

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        boolean hasValidHeader = false;
        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            final String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (header.toLowerCase().startsWith("basic")) {
                hasValidHeader = true;
            }
        }
        if (!hasValidHeader) {
            final boolean isPostingUser =
                    request.getMethod() == "POST" &&
                            request.getUriInfo().getMatchedResources().size() > 0 &&
                            (request.getUriInfo().getMatchedResources().get(0) instanceof UsersResource ||
                                    request.getUriInfo().getMatchedResources().get(0) instanceof ResetPasswordResource);

            if (request.getMethod() != "GET" && !isPostingUser) {
                request.abortWith(Response.noContent().status(Response.Status.FORBIDDEN).build());
            }

            final String base64 = Base64.encodeAsString(defultUserEmail + ":" + defultUserPassword);
            request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + base64);
        }
    }
}