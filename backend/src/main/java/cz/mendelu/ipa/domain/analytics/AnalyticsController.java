package cz.mendelu.ipa.domain.analytics;

import cz.mendelu.ipa.config.Principal;
import cz.mendelu.ipa.config.PrincipalFactory;
import cz.mendelu.ipa.config.RateLimitingService;
import cz.mendelu.ipa.domain.game.GameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cz.mendelu.ipa.utils.exceptions.RateLimitException;
import io.github.bucket4j.Bucket;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("analytics")
@Validated
@Tag(
        name = "Analytics",
        description = "Analytics for user project based on historical game data"
)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    private final RateLimitingService rateLimitingService;

    private final PrincipalFactory principalFactory;

    public AnalyticsController(AnalyticsService analyticsService,
                               RateLimitingService rateLimitingService,
                               PrincipalFactory principalFactory) {
        this.analyticsService = analyticsService;
        this.rateLimitingService = rateLimitingService;
        this.principalFactory = principalFactory;
    }

    private void consumeRateLimit(Jwt jwt) {
        Bucket bucket = rateLimitingService.resolveBucket(jwt);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException();
        }
    }

    // http://localhost:8080/api/analytics/success-score?gameId=1
    @GetMapping(value = "/success-score", produces = "application/json")
    @Operation(
            summary = "Get weighted success score",
            description = "Get weighted success score of the game based on critic and user reviews."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score calculated"),
            @ApiResponse(responseCode = "404", description = "Game or review not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public AnalyticsResponse getWeightedSuccessScore(@RequestParam long gameId, @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return new AnalyticsResponse(analyticsService.weightedSuccessScore(gameId));
    }

    @GetMapping(value = "/top-competitors", produces = "application/json")
    @Operation(
            summary = "Get top 3 competitors",
            description = "Returns top 3 games by weighted success score for a genre, optionally filtered by platform."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Top competitors found"),
            @ApiResponse(responseCode = "404", description = "No data for genre", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public List<GameResponse> getTopCompetitors(
            @RequestParam String genre,
            @RequestParam(required = false) String platform,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return analyticsService.topCompetitors(genre, platform);
    }

    @GetMapping(value = "/revenue-predictor", produces = "application/json")
    @Operation(
            summary = "Get revenue predictor",
            description = "Predicts EU revenue based on historical NA/EU sales ratio for a genre. Optionally filtered by platform."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prediction calculated"),
            @ApiResponse(responseCode = "404", description = "No data for genre", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public AnalyticsResponse getRevenuePredictor(
            @RequestParam String genre,
            @RequestParam(required = false) String platform,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return new AnalyticsResponse(analyticsService.revenuePredictor(genre, platform));
    }

    @GetMapping(value = "/global-adaptability", produces = "application/json")
    @Operation(
            summary = "Get global adaptability score",
            description = "How evenly a genre sells across NA, EU and JP markets. Optionally filtered by platform."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Score calculated"),
            @ApiResponse(responseCode = "404", description = "No data for genre", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public AnalyticsResponse getGlobalAdaptabilityScore(
            @RequestParam String genre,
            @RequestParam(required = false) String platform,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return new AnalyticsResponse(analyticsService.globalAdaptabilityScore(genre, platform));
    }

    @GetMapping(value = "/region-priority", produces = "application/json")
    @Operation(
            summary = "Get region priority",
            description = "Returns the region (NA, EU, JP) with the highest average sales per game for a genre. Optionally filtered by platform."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Region calculated"),
            @ApiResponse(responseCode = "404", description = "No data for genre", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public AnalyticsResponse getRegionPriority(
            @RequestParam String genre,
            @RequestParam(required = false) String platform,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return new AnalyticsResponse(analyticsService.regionPriority(genre, platform));
    }

    @GetMapping(value = "/investment-risk", produces = "application/json")
    @Operation(
            summary = "Get investment risk",
            description = "Percentage (0–100) of games in the genre that had a good critic score (> 70) but below-average sales. Higher = riskier investment. Optionally filtered by platform."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Risk calculated"),
            @ApiResponse(responseCode = "404", description = "No data for genre", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public AnalyticsResponse getInvestmentRisk(
            @RequestParam String genre,
            @RequestParam(required = false) String platform,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return new AnalyticsResponse(analyticsService.investmentRisk(genre, platform));
    }

    @GetMapping(value = "/project/{projectId}", produces = "application/json")
    @Operation(
            summary = "Get all analytics for a project",
            description = "Returns all analytics results for a user project in one call."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics calculated"),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public ProjectAnalyticsResponse getProjectAnalytics(
            @PathVariable Long projectId,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        Principal principal = principalFactory.of(jwt);
        return analyticsService.projectAnalytics(projectId, principal.getUsername());
    }

    @GetMapping(value = "/platform-recommender", produces = "application/json")
    @Operation(
            summary = "Get platform recommendation",
            description = "Returns the best platform for a given genre based on average sales and critic score."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Platform calculated"),
            @ApiResponse(responseCode = "404", description = "No data for genre", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public AnalyticsResponse getPlatformRecommender(
            @RequestParam String genre,
            @AuthenticationPrincipal Jwt jwt) {
        consumeRateLimit(jwt);
        return new AnalyticsResponse(analyticsService.platformRecommender(genre));
    }
}