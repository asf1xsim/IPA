package cz.mendelu.ipa.utils;

import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static io.restassured.RestAssured.given;

@Service
public class AuthHelper {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    public String login(String username, String password) {
        return given()
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("password", password)
                .formParam("grant_type", "password")
                .formParam("client_id", clientId)
                .when()
                .post(issuerUri + "/protocol/openid-connect/token")
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }

    public String loginAsUser() {
        return login("test_user", "123");
    }

    public String loginAsAdmin() {
        return login("test_admin", "123");
    }
}