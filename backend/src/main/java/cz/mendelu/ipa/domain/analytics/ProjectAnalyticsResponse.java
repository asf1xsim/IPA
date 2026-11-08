package cz.mendelu.ipa.domain.analytics;

import cz.mendelu.ipa.domain.game.GameResponse;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProjectAnalyticsResponse {
    private List<GameResponse> topCompetitors;
    private Double globalAdaptability;
    private Double investmentRisk;
    private String regionPriority;
    private BigDecimal predictedNa;
    private BigDecimal predictedEu;
    private BigDecimal predictedJp;
    private String platformRecommendation;
}