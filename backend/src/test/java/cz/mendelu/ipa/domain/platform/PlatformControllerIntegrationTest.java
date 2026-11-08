package cz.mendelu.ipa.domain.platform;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class PlatformControllerIntegrationTest {

    private static final String BASE_URI = "http://localhost";
    private static final String TOKEN_ADMIN = "fake-admin-token";
    private static final String TOKEN_USER = "fake-user-token";

    @LocalServerPort
    private int port;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUp() {
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

    // ── GET /platforms

    @Test
    public void testGetAllPlatforms_returnsOk() {
        when()
                .get("/platforms")
                .then()
                .statusCode(200)
                .body("data", notNullValue())
                .body("data.size()", is(2));
    }

    @Test
    public void testGetAllPlatforms_hasExpectedFields() {
        when()
                .get("/platforms")
                .then()
                .statusCode(200)
                .body("data.name", containsInAnyOrder("PS4", "PC"));
    }

    // ── GET /platforms/{id}

    @Test
    public void testGetPlatformById_returnsOk() {
        when()
                .get("/platforms/1")
                .then()
                .statusCode(200)
                .body("data.id", is(1))
                .body("data.name", is("PS4"))
                .body("data.manufacturer", is("Sony"));
    }

    @Test
    public void testGetPlatformById_notFound() {
        when()
                .get("/platforms/999999")
                .then()
                .statusCode(404);
    }

    // ── POST /platforms

    @Test
    public void testCreatePlatform_asAdmin_returnsCreated() {
        String request = """
                {
                    "name": "Xbox",
                    "manufacturer": "Microsoft"
                }
                """;

        int id = given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/platforms")
                .then()
                .statusCode(201)
                .body("data.name", is("Xbox"))
                .body("data.manufacturer", is("Microsoft"))
                .extract()
                .path("data.id");

        when()
                .get("/platforms/" + id)
                .then()
                .statusCode(200)
                .body("data.name", is("Xbox"));
    }

    @Test
    public void testCreatePlatform_asUser_returns403() {
        String request = """
                {
                    "name": "Xbox",
                    "manufacturer": "Microsoft"
                }
                """;

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/platforms")
                .then()
                .statusCode(403);
    }

    @Test
    public void testCreatePlatform_noToken_returns401() {
        String request = """
                {
                    "name": "Xbox",
                    "manufacturer": "Microsoft"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/platforms")
                .then()
                .statusCode(401);
    }

    @Test
    public void testCreatePlatform_badRequest_returns400() {
        String request = """
                {
                    "name": "",
                    "manufacturer": "Microsoft"
                }
                """;

        given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/platforms")
                .then()
                .statusCode(400);
    }

    // ── PUT /platforms/{id}

    @Test
    public void testUpdatePlatform_asAdmin_returnsOk() {
        String request = """
                {
                    "name": "PS4 Updated",
                    "manufacturer": "Sony Updated"
                }
                """;

        given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/platforms/1")
                .then()
                .statusCode(200)
                .body("data.name", is("PS4 Updated"))
                .body("data.manufacturer", is("Sony Updated"));
    }

    @Test
    public void testUpdatePlatform_notFound_returns404() {
        String request = """
                {
                    "name": "Does not exist",
                    "manufacturer": "Nobody"
                }
                """;

        given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/platforms/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpdatePlatform_asUser_returns403() {
        String request = """
                {
                    "name": "PS4 Updated",
                    "manufacturer": "Sony"
                }
                """;

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/platforms/1")
                .then()
                .statusCode(403);
    }

    // ── DELETE /platforms/{id}

    @Test
    public void testDeletePlatform_asAdmin_returnsNoContent() {
        String request = """
                {
                    "name": "ToDelete",
                    "manufacturer": "Nobody"
                }
                """;

        int id = given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/platforms")
                .then()
                .statusCode(201)
                .extract()
                .path("data.id");

        given()
                .header("Authorization", "Bearer " + TOKEN_ADMIN)
                .when()
                .delete("/platforms/" + id)
                .then()
                .statusCode(204);

        when()
                .get("/platforms/" + id)
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeletePlatform_asUser_returns403() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .delete("/platforms/1")
                .then()
                .statusCode(403);
    }

    @Test
    public void testDeletePlatform_noToken_returns401() {
        given()
                .when()
                .delete("/platforms/1")
                .then()
                .statusCode(401);
    }
}