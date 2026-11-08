package cz.mendelu.ipa.domain.userproject;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProjectRequest {

    @NotEmpty
    private String projectName;

    @NotEmpty
    private String targetGenre;

    private String targetPlatform;

    private String expectedSalesRegion;

    private BigDecimal expectedSalesAmount;

    public void toUserProject(UserProject project, String username) {
        project.setUsername(username);
        project.setProjectName(projectName);
        project.setTargetGenre(targetGenre);
        project.setTargetPlatform(targetPlatform);
        project.setExpectedSalesRegion(expectedSalesRegion);
        project.setExpectedSalesAmount(expectedSalesAmount);
    }
}