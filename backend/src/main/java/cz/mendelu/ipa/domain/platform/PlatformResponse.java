package cz.mendelu.ipa.domain.platform;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlatformResponse {

    @NotNull
    private Long id;

    @NotEmpty
    private String name;

    private String manufacturer;

    private int gameCount;

    public PlatformResponse(Platform platform) {
        this.id = platform.getId();
        this.name = platform.getName();
        this.manufacturer = platform.getManufacturer();
        this.gameCount = (platform.getGames() != null && org.hibernate.Hibernate.isInitialized(platform.getGames()))
                ? platform.getGames().size()
                : 0;
    }
}
