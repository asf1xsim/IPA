package cz.mendelu.ipa.domain.userproject;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
class UserProjectControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private UserProjectRepository userProjectRepository;

    private static final String TOKEN_USER = "fake-user-token";
    private static final String TOKEN_OTHER = "fake-other-token";
    private static final String USERNAME = "testuser";
    private static final String OTHER_USERNAME = "otheruser";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        when(jwtDecoder.decode(TOKEN_USER)).thenReturn(buildJwt(USERNAME, List.of("USER")));
        when(jwtDecoder.decode(TOKEN_OTHER)).thenReturn(buildJwt(OTHER_USERNAME, List.of("USER")));
    }

    @AfterEach
    void tearDown() {
        userProjectRepository.deleteAll();
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

    private UserProject createProjectInDb(String username, String name, String genre, String platform) {
        UserProject p = new UserProject();
        p.setUsername(username);
        p.setProjectName(name);
        p.setTargetGenre(genre);
        p.setTargetPlatform(platform);
        return userProjectRepository.save(p);
    }

    // ── GET /projects

    @Test
    void getMyProjects_returnsEmptyList() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/projects")
                .then()
                .statusCode(200)
                .body("data", hasSize(0));
    }

    @Test
    void getMyProjects_returnsOnlyOwnProjects() {
        createProjectInDb(USERNAME, "My Game", "Action", "PC");
        createProjectInDb(OTHER_USERNAME, "Other Game", "RPG", "PS5");

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/projects")
                .then()
                .statusCode(200)
                .body("data", hasSize(1))
                .body("data[0].projectName", equalTo("My Game"));
    }

    @Test
    void getMyProjects_unauthorized_returns401() {
        given()
                .when()
                .get("/projects")
                .then()
                .statusCode(401);
    }

    // ── GET /projects/{id}

    @Test
    void getProjectById_returnsProject() {
        UserProject saved = createProjectInDb(USERNAME, "My Game", "Action", "PC");

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/projects/" + saved.getId())
                .then()
                .statusCode(200)
                .body("data.id", equalTo(saved.getId().intValue()))
                .body("data.projectName", equalTo("My Game"))
                .body("data.targetGenre", equalTo("Action"))
                .body("data.targetPlatform", equalTo("PC"));
    }

    @Test
    void getProjectById_notFound_returns404() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/projects/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void getProjectById_belongsToOtherUser_returns404() {
        UserProject saved = createProjectInDb(OTHER_USERNAME, "Other Game", "RPG", "PS5");

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/projects/" + saved.getId())
                .then()
                .statusCode(404);
    }

    // ── POST /projects

    @Test
    void createProject_success() {
        String bodyJson = """
        {
            "projectName": "New Game",
            "targetGenre": "Sports",
            "targetPlatform": "Xbox"
        }
        """;

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(bodyJson)
                .when()
                .post("/projects")
                .then()
                .statusCode(201)
                .body("data.projectName", equalTo("New Game"))
                .body("data.targetGenre", equalTo("Sports"))
                .body("data.targetPlatform", equalTo("Xbox"))
                .body("data.username", equalTo(USERNAME));
    }

    @Test
    void createProject_missingFields_returns400() {
        Map<String, String> body = Map.of("projectName", "");

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/projects")
                .then()
                .statusCode(400);
    }

    @Test
    void createProject_unauthorized_returns401() {
        Map<String, String> body = Map.of(
                "projectName", "New Game",
                "targetGenre", "Sports",
                "targetPlatform", "Xbox"
        );

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/projects")
                .then()
                .statusCode(401);
    }

    // ── PUT /projects/{id}

    @Test
    void updateProject_success() {
        UserProject saved = createProjectInDb(USERNAME, "Old Name", "Action", "PC");

        Map<String, String> body = Map.of(
                "projectName", "Updated Name",
                "targetGenre", "RPG",
                "targetPlatform", "PS5"
        );

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/projects/" + saved.getId())
                .then()
                .statusCode(200)
                .body("data.projectName", equalTo("Updated Name"))
                .body("data.targetGenre", equalTo("RPG"))
                .body("data.targetPlatform", equalTo("PS5"));
    }

    @Test
    void updateProject_notFound_returns404() {
        Map<String, String> body = Map.of(
                "projectName", "Updated Name",
                "targetGenre", "RPG",
                "targetPlatform", "PS5"
        );

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/projects/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void updateProject_belongsToOtherUser_returns404() {
        UserProject saved = createProjectInDb(OTHER_USERNAME, "Other Game", "RPG", "PS5");

        Map<String, String> body = Map.of(
                "projectName", "Hacked Name",
                "targetGenre", "Action",
                "targetPlatform", "PC"
        );

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/projects/" + saved.getId())
                .then()
                .statusCode(404);
    }

    // ── DELETE /projects/{id}

    @Test
    void deleteProject_success() {
        UserProject saved = createProjectInDb(USERNAME, "To Delete", "Action", "PC");

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .delete("/projects/" + saved.getId())
                .then()
                .statusCode(204);

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/projects/" + saved.getId())
                .then()
                .statusCode(404);
    }

    @Test
    void deleteProject_notFound_returns404() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .delete("/projects/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteProject_belongsToOtherUser_returns404() {
        UserProject saved = createProjectInDb(OTHER_USERNAME, "Other Game", "RPG", "PS5");

        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .delete("/projects/" + saved.getId())
                .then()
                .statusCode(404);
    }
}