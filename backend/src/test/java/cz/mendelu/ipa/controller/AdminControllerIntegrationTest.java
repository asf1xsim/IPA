package cz.mendelu.ipa.controller;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class AdminControllerIntegrationTest {

    private static final String BASE_URI = "http://localhost";
    private static final String TOKEN_ADMIN = "fake-admin-token";
    private static final String TOKEN_USER = "fake-user-token";

    @LocalServerPort
    private int port;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;

        org.mockito.Mockito.when(jwtDecoder.decode(TOKEN_ADMIN))
                .thenReturn(buildJwt("adminuser", List.of("ADMIN")));
        org.mockito.Mockito.when(jwtDecoder.decode(TOKEN_USER))
                .thenReturn(buildJwt("regularuser", List.of("USER")));
    }

    private Jwt buildJwt(String username, List<String> roles) {
        LinkedTreeMap<String, List<String>> clientRoles = new LinkedTreeMap<>();
        clientRoles.put("roles", roles);
        LinkedTreeMap<String, Object> resourceAccess = new LinkedTreeMap<>();
        resourceAccess.put("backend-client", clientRoles);

        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("preferred_username", username)
                .claim("name", username)
                .claim("email", username + "@test.com")
                .claim("resource_access", resourceAccess)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void importData_asAdmin_returns200() {
        given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .when()
                .post("/api/admin/import")
                .then()
                .statusCode(200)
                .body(containsString("games data"));
    }

    @Test
    void importData_asUser_returns403() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .post("/api/admin/import")
                .then()
                .statusCode(403);
    }

    @Test
    void importData_noToken_returns401() {
        given()
                .when()
                .post("/api/admin/import")
                .then()
                .statusCode(401);
    }
}