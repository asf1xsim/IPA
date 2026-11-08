package cz.mendelu.ipa.domain.userproject;

import cz.mendelu.ipa.config.Principal;
import cz.mendelu.ipa.config.PrincipalFactory;
import cz.mendelu.ipa.config.RateLimitingService;
import cz.mendelu.ipa.domain.analytics.AnalyticsService;
import cz.mendelu.ipa.utils.exceptions.NoDataFoundException;
import cz.mendelu.ipa.utils.exceptions.NotFoundException;
import cz.mendelu.ipa.utils.exceptions.RateLimitException;
import cz.mendelu.ipa.utils.response.ArrayResponse;
import cz.mendelu.ipa.utils.response.ObjectResponse;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("projects")
@Validated
@Tag(
        name = "Projects",
        description = "User projects management"
)
public class UserProjectController {

    private final UserProjectService userProjectService;
    private final PrincipalFactory principalFactory;
    private final RateLimitingService rateLimitingService;
    private final AnalyticsService analyticsService;

    public UserProjectController(UserProjectService userProjectService,
                                 PrincipalFactory principalFactory,
                                 RateLimitingService rateLimitingService,
                                 AnalyticsService analyticsService) {
        this.userProjectService  = userProjectService;
        this.principalFactory    = principalFactory;
        this.rateLimitingService = rateLimitingService;
        this.analyticsService    = analyticsService;
    }

    @GetMapping(value = "", produces = "application/json")
    @Operation(summary = "Get all projects", description = "Get projects created by this user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of projects")
    })
    @PreAuthorize("hasRole('USER')")
    public ArrayResponse<UserProjectResponse> getMyProjects(@AuthenticationPrincipal Jwt jwt) {
        Principal principal = principalFactory.of(jwt);
        return ArrayResponse.of(
                userProjectService.getProjectsByUsername(principal.getUsername()),
                UserProjectResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Get project by id", description = "Get this user's project by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public ObjectResponse<UserProjectResponse> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserProject project = userProjectService
                .getProjectById(id)
                .orElseThrow(NotFoundException::new);
        Principal principal = principalFactory.of(jwt);
        if (!project.getUsername().equals(principal.getUsername())) {
            throw new NotFoundException();
        }
        return ObjectResponse.of(project, UserProjectResponse::new);
    }

    @PostMapping(value = "", produces = "application/json")
    @Operation(summary = "Create project", description = "Create new project by this user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public ObjectResponse<UserProjectResponse> createProject(
            @RequestBody @Valid UserProjectRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Bucket bucket = rateLimitingService.resolveBucket(jwt);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException();
        }
        Principal principal = principalFactory.of(jwt);
        UserProject project = new UserProject();
        request.toUserProject(project, principal.getUsername());

        try {
            double risk = analyticsService.investmentRisk(
                    request.getTargetGenre(),
                    request.getTargetPlatform()
            );
            project.setCalculatedRisk(BigDecimal.valueOf(risk));
        } catch (NoDataFoundException e) {
            project.setCalculatedRisk(null);
        }

        if (request.getExpectedSalesRegion() != null && request.getExpectedSalesAmount() != null) {
            try {
                BigDecimal predicted = analyticsService.revenuePredictor(
                        request.getTargetGenre(),
                        request.getTargetPlatform(),
                        request.getExpectedSalesRegion(),
                        request.getExpectedSalesAmount()
                );
                project.setPredictedRevenue(predicted);
            } catch (NoDataFoundException e) {
                project.setPredictedRevenue(null);
            }
        }

        return ObjectResponse.of(userProjectService.createProject(project), UserProjectResponse::new);
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Update project", description = "Update this user's project by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated"),
            @ApiResponse(responseCode = "404", description = "Project not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @PreAuthorize("hasRole('USER')")
    public ObjectResponse<UserProjectResponse> updateProject(
            @PathVariable Long id,
            @RequestBody @Valid UserProjectRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserProject project = userProjectService
                .getProjectById(id)
                .orElseThrow(NotFoundException::new);
        Principal principal = principalFactory.of(jwt);
        if (!project.getUsername().equals(principal.getUsername())) {
            throw new NotFoundException();
        }
        request.toUserProject(project, principal.getUsername());

        try {
            double risk = analyticsService.investmentRisk(
                    request.getTargetGenre(),
                    request.getTargetPlatform()
            );
            project.setCalculatedRisk(BigDecimal.valueOf(risk));
        } catch (NoDataFoundException e) {
            project.setCalculatedRisk(null);
        }

        if (request.getExpectedSalesRegion() != null && request.getExpectedSalesAmount() != null) {
            try {
                BigDecimal predicted = analyticsService.revenuePredictor(
                        request.getTargetGenre(),
                        request.getTargetPlatform(),
                        request.getExpectedSalesRegion(),
                        request.getExpectedSalesAmount()
                );
                project.setPredictedRevenue(predicted);
            } catch (NoDataFoundException e) {
                project.setPredictedRevenue(null);
            }
        } else {
            project.setPredictedRevenue(null);
        }

        return ObjectResponse.of(userProjectService.updateProject(id, project), UserProjectResponse::new);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete project by ID", description = "Delete this user's project by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserProject project = userProjectService
                .getProjectById(id)
                .orElseThrow(NotFoundException::new);
        Principal principal = principalFactory.of(jwt);
        if (!project.getUsername().equals(principal.getUsername())) {
            throw new NotFoundException();
        }
        userProjectService.deleteProject(id);
    }
}