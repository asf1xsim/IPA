package cz.mendelu.ipa.domain.userproject;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProjectResponse {

    @NotNull
    private Long id;

    @NotEmpty
    private String username;

    @NotEmpty
    private String projectName;

    private String targetGenre;

    private String targetPlatform;

    private BigDecimal calculatedRisk;

    private LocalDateTime createdAt;

    private String expectedSalesRegion;

    private BigDecimal expectedSalesAmount;

    private BigDecimal predictedRevenue;

    public UserProjectResponse(UserProject project) {
        this.id = project.getId();
        this.username = project.getUsername();
        this.projectName = project.getProjectName();
        this.targetGenre = project.getTargetGenre();
        this.targetPlatform = project.getTargetPlatform();
        this.calculatedRisk = project.getCalculatedRisk();
        this.createdAt = project.getCreatedAt();
        this.expectedSalesRegion = project.getExpectedSalesRegion();
        this.expectedSalesAmount = project.getExpectedSalesAmount();
        this.predictedRevenue    = project.getPredictedRevenue();
    }
}