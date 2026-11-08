package cz.mendelu.ipa.domain.analytics;

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
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class AnalyticsControllerIntegrationTest {

    private static final String BASE_URI = "http://localhost";
    private static final String TOKEN_USER = "fake-user-token";

    @LocalServerPort
    private int port;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;

        org.mockito.Mockito.when(jwtDecoder.decode(TOKEN_USER))
                .thenReturn(buildJwt("test_user", List.of("USER")));
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

    // ── GET /analytics/success-score

    @Test
    public void testWeightedSuccessScore_returnsOk() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/success-score?gameId=1")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", greaterThan(0.0F));
    }

    @Test
    public void testWeightedSuccessScore_notFound() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/success-score?gameId=999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testWeightedSuccessScore_unauthorized() {
        given()
                .when()
                .get("/analytics/success-score?gameId=1")
                .then()
                .statusCode(401);
    }

    // ── GET /analytics/revenue-predictor

    @Test
    public void testRevenuePredictor_returnsOk() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/revenue-predictor?genre=Sports")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", greaterThan(0.0F));
    }

    @Test
    public void testRevenuePredictor_unknownGenre_returns404() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/revenue-predictor?genre=NonExistentGenre99999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testRevenuePredictor_unauthorized() {
        given()
                .when()
                .get("/analytics/revenue-predictor?genre=Sports")
                .then()
                .statusCode(401);
    }

    // ── GET /analytics/global-adaptability

    @Test
    public void testGlobalAdaptabilityScore_returnsOk() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/global-adaptability?genre=Sports")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", allOf(greaterThanOrEqualTo(0.0F), lessThanOrEqualTo(100.0F)));
    }

    @Test
    public void testGlobalAdaptabilityScore_unknownGenre_returns404() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/global-adaptability?genre=NonExistentGenre99999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGlobalAdaptabilityScore_unauthorized() {
        given()
                .when()
                .get("/analytics/global-adaptability?genre=Sports")
                .then()
                .statusCode(401);
    }

    // ── GET /analytics/region-priority

    @Test
    public void testRegionPriority_returnsOk() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/region-priority?genre=Sports")
                .then()
                .statusCode(200)
                .body("label", anyOf(is("NA"), is("EU"), is("JP")));
    }

    @Test
    public void testRegionPriority_unknownGenre_returns404() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/region-priority?genre=NonExistentGenre99999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testRegionPriority_unauthorized() {
        given()
                .when()
                .get("/analytics/region-priority?genre=Sports")
                .then()
                .statusCode(401);
    }

    // ── GET /analytics/investment-risk

    @Test
    public void testInvestmentRisk_returnsOk() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/investment-risk?genre=Sports")
                .then()
                .statusCode(200)
                .body("value", notNullValue())
                .body("value", allOf(greaterThanOrEqualTo(0.0F), lessThanOrEqualTo(100.0F)));
    }

    @Test
    public void testInvestmentRisk_unknownGenre_returns404() {
        given()
                .header("Authorization", "Bearer " + TOKEN_USER)
                .when()
                .get("/analytics/investment-risk?genre=NonExistentGenre99999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testInvestmentRisk_unauthorized() {
        given()
                .when()
                .get("/analytics/investment-risk?genre=Sports")
                .then()
                .statusCode(401);
    }
}