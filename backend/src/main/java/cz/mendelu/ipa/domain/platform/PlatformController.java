package cz.mendelu.ipa.domain.platform;

import cz.mendelu.ipa.utils.exceptions.NotFoundException;
import cz.mendelu.ipa.utils.response.ArrayResponse;
import cz.mendelu.ipa.utils.response.ObjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("platforms")
@Validated
@Tag(
        name = "Platforms",
        description = "Read only access to platforms in dataset"
)
public class PlatformController {

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping(value = "", produces = "application/json")
    @Operation(summary = "Get all platforms")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of platforms")
    })
    public ArrayResponse<PlatformResponse> getAllPlatforms() {
        return ArrayResponse.of(
                platformService.getAllPlatforms(),
                PlatformResponse::new
        );
    }

    @GetMapping(value = "/names", produces = "application/json")
    @Operation(summary = "Get all platform names", description = "Lightweight list of platforms without game data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of platforms")
    })
    public ArrayResponse<PlatformResponse> getAllPlatformNames() {
        return ArrayResponse.of(
                platformService.getAllPlatformsLight(),
                PlatformResponse::new
        );
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Get platform by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Platform found"),
            @ApiResponse(responseCode = "404", description = "Platform not found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ObjectResponse<PlatformResponse> getPlatformById(@PathVariable Long id) {
        Platform platform = platformService
                .getPlatformById(id)
                .orElseThrow(NotFoundException::new);
        return ObjectResponse.of(platform, PlatformResponse::new);
    }

    @PostMapping(value = "", produces = "application/json")
    @Operation(summary = "Create platform")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Platform created")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ObjectResponse<PlatformResponse> createPlatform(@RequestBody @Valid PlatformRequest request) {
        Platform platform = new Platform();
        request.toPlatform(platform);
        return ObjectResponse.of(platformService.createPlatform(platform), PlatformResponse::new);
    }

    @PutMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Update platform")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Platform updated"),
            @ApiResponse(responseCode = "404", description = "Platform not found", content = @Content(schema = @Schema(hidden = true)))
    })
    public ObjectResponse<PlatformResponse> updatePlatform(@PathVariable Long id, @RequestBody @Valid PlatformRequest request) {
        Platform platform = platformService
                .getPlatformById(id)
                .orElseThrow(NotFoundException::new);
        request.toPlatform(platform);
        return ObjectResponse.of(platformService.updatePlatform(id, platform), PlatformResponse::new);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete platform")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Platform deleted"),
            @ApiResponse(responseCode = "404", description = "Platform not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlatform(@PathVariable Long id) {
        platformService.getPlatformById(id).orElseThrow(NotFoundException::new);
        platformService.deletePlatform(id);
    }
}