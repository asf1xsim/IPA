package cz.mendelu.ipa.domain.userproject;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_projects")
@Data
@NoArgsConstructor
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(nullable = false, length = 150)
    private String username;

    @NotEmpty
    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Column(name = "target_genre", length = 100)
    private String targetGenre;

    @Column(name = "target_platform", length = 50)
    private String targetPlatform;

    @Column(name = "calculated_risk")
    private BigDecimal calculatedRisk;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expected_sales_region", length = 10)
    private String expectedSalesRegion;

    @Column(name = "expected_sales_amount", precision = 15, scale = 2)
    private BigDecimal expectedSalesAmount;

    @Column(name = "predicted_revenue", precision = 15, scale = 2)
    private BigDecimal predictedRevenue;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}