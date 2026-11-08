package cz.mendelu.ipa.domain.game;

import cz.mendelu.ipa.utils.exceptions.NotFoundException;
import cz.mendelu.ipa.utils.response.ArrayResponse;
import cz.mendelu.ipa.utils.response.ObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("games")
@Validated
@Tag(
        name = "Games",
        description = "Read only access to game dataset"
)
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping(value = "", produces = "application/json")
    @Operation(summary = "Get all games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of games")
    })
    public ArrayResponse<GameResponse> getAllGames() {
        return ArrayResponse.of(
                gameService.getAllGames(),
                GameResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Get game by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found"),
            @ApiResponse(responseCode = "404", description = "Game not found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ObjectResponse<GameResponse> getGameById(@PathVariable @Positive Long id) {
        Game game = gameService
                .getGameById(id)
                .orElseThrow(NotFoundException::new);
        return ObjectResponse.of(game, GameResponse::new);
    }

    @GetMapping(value = "genres", produces = "application/json")
    @Operation(
            summary = "Get all genres",
            description = "Get list of all unique genres in dataset."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of genres")
    })
    public List<String> getAllGenres() {
        return gameService.getAllGenres();
    }

    @GetMapping(value = "/search", produces = "application/json")
    @Operation(
            summary = "Search games by genre",
            description = "Get all games filtered by genre."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of games")
    })
    public ArrayResponse<GameResponse> getGamesByGenre(@RequestParam String genre) {
        return ArrayResponse.of(
                gameService.getGamesByGenre(genre),
                GameResponse::new
        );
    }

    @GetMapping(value = "/stats", produces = "application/json")
    @Operation(
            summary = "Get database stats",
            description = "Returns total game count, distinct genre count and platform count."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stats returned")
    })
    public Map<String, Long> getStats() {
        long[] s = gameService.getStats();
        return Map.of("games", s[0], "genres", s[1], "platforms", s[2]);
    }

    @GetMapping(value = "/top", produces = "application/json")
    @Operation(
            summary = "Get top 6 rated games",
            description = "Returns top 6 games by weighted success score."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top games returned")
    })
    public ArrayResponse<GameResponse> getTopGames() {
        return ArrayResponse.of(
                gameService.getTopGames(),
                GameResponse::new
        );
    }
}