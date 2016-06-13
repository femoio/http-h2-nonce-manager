package io.femo.http.auth;

import io.femo.http.Http;
import io.femo.http.handlers.Authentication;
import io.femo.http.handlers.auth.CredentialProvider;

import java.sql.SQLException;

/**
 * Created by felix on 6/13/16.
 */
public class TestApp {

    public static void main(String[] args) {
        Http.server(8080)
                .use(Authentication.digest("test",
                        (uname) -> uname.equals("felix") ? new CredentialProvider.Credentials("felix", "test") : null,
                        H2NonceManager.h2OrDefault("test-nonce")))
                .get("/", (request, response) -> {
                    response.entity("Hello World");
                    return true;
                }).start();
    }
}
