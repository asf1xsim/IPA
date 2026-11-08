package cz.mendelu.ipa.domain.game;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/test-data/cleanup.sql")
@Sql("/test-data/base-data.sql")
public class GameControllerIntegrationTest {

    private static final String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    // ── GET /games

    @Test
    public void testGetAllGames_returnsOk() {
        when()
                .get("/games")
                .then()
                .statusCode(200)
                .body("data", notNullValue())
                .body("data.size()", is(4))
                .body("count", is(4));
    }

    @Test
    public void testGetAllGames_hasExpectedFields() {
        when()
                .get("/games")
                .then()
                .statusCode(200)
                .body("data[0].id", notNullValue())
                .body("data[0].title", notNullValue())
                .body("data[0].genre", notNullValue())
                .body("data[0].platformName", notNullValue());
    }

    // ── GET /games/{id}

    @Test
    public void testGetGameById_returnsOk() {
        when()
                .get("/games/1")
                .then()
                .statusCode(200)
                .body("data.id", is(1))
                .body("data.title", is("The Witcher 3"))
                .body("data.genre", is("RPG"))
                .body("data.platformName", is("PS4"));
    }

    @Test
    public void testGetGameById_notFound() {
        when()
                .get("/games/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testGetGameById_invalidId_negative() {
        when()
                .get("/games/-1")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    public void testGetGameById_invalidId_zero() {
        when()
                .get("/games/0")
                .then()
                .statusCode(anyOf(is(400), is(500)));
    }

    // ── GET /games/genres

    @Test
    public void testGetAllGenres_returnsOk() {
        when()
                .get("/games/genres")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    public void testGetAllGenres_containsKnownGenres() {
        when()
                .get("/games/genres")
                .then()
                .statusCode(200)
                .body("$", containsInAnyOrder("RPG", "Sports"));
    }

    @Test
    public void testGetAllGenres_returnsStrings() {
        when()
                .get("/games/genres")
                .then()
                .statusCode(200)
                .body("$", everyItem(isA(String.class)));
    }

    // ── GET /games/search?genre=

    @Test
    public void testGetGamesByGenre_RPG_returnsOnlyRPG() {
        when()
                .get("/games/search?genre=RPG")
                .then()
                .statusCode(200)
                .body("data.size()", is(3))
                .body("data.genre", everyItem(is("RPG")));
    }

    @Test
    public void testGetGamesByGenre_Sports_returnsOnlySports() {
        when()
                .get("/games/search?genre=Sports")
                .then()
                .statusCode(200)
                .body("data.size()", is(1))
                .body("data.genre", everyItem(is("Sports")));
    }

    @Test
    public void testGetGamesByGenre_unknown_returnsEmptyList() {
        when()
                .get("/games/search?genre=NonExistentGenre99999")
                .then()
                .statusCode(200)
                .body("data.size()", is(0));
    }

    @Test
    public void testGetGamesByGenre_hasExpectedFields() {
        when()
                .get("/games/search?genre=Sports")
                .then()
                .statusCode(200)
                .body("data[0].id", notNullValue())
                .body("data[0].title", is("FIFA 23"))
                .body("data[0].genre", is("Sports"))
                .body("data[0].platformName", notNullValue());
    }
}