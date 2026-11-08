package cz.mendelu.ipa.domain.platform;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PlatformRequest {

    @NotEmpty
    private String name;

    private String manufacturer;

    public void toPlatform(Platform platform) {
        platform.setName(name);
        platform.setManufacturer(manufacturer);
    }
}