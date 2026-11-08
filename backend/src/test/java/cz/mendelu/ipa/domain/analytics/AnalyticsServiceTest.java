package cz.mendelu.ipa.domain.analytics;

import cz.mendelu.ipa.domain.game.Game;
import cz.mendelu.ipa.domain.game.GameRepository;
import cz.mendelu.ipa.domain.review.Review;
import cz.mendelu.ipa.domain.review.ReviewRepository;
import cz.mendelu.ipa.domain.salesdata.SalesData;
import cz.mendelu.ipa.domain.salesdata.SalesDataRepository;
import cz.mendelu.ipa.domain.userproject.UserProjectRepository;
import cz.mendelu.ipa.utils.exceptions.NoDataFoundException;
import cz.mendelu.ipa.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnalyticsServiceTest {

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final SalesDataRepository salesDataRepository = mock(SalesDataRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final UserProjectRepository userProjectRepository = mock(UserProjectRepository.class);
    private final AnalyticsService analyticsService = new AnalyticsService(reviewRepository, salesDataRepository, gameRepository, userProjectRepository);

    // weightedSuccessScore
    // criticScore * 0.7 + (userScore * 10) * 0.3

    @Test
    public void testWeightedSuccessScore() {
        // given
        Review review = new Review();
        review.setCriticScore(new BigDecimal("80"));
        review.setUserScore(new BigDecimal("7.0"));

        when(reviewRepository.findByGame_Id(1L)).thenReturn(Optional.of(review));

        // when
        double result = analyticsService.weightedSuccessScore(1L);

        // then — 80 * 0.7 + (7.0 * 10) * 0.3 = 56 + 21 = 77.0
        assertThat(result, is(77.0));
    }

    @Test
    public void testWeightedSuccessScore_ReviewNotFound() {
        // given
        when(reviewRepository.findByGame_Id(999L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(NotFoundException.class,
                () -> analyticsService.weightedSuccessScore(999L));
    }

    @Test
    public void testWeightedSuccessScore_MissingScores() {
        // given — review exists but scores are null
        Review review = new Review();
        review.setCriticScore(null);
        review.setUserScore(null);

        when(reviewRepository.findByGame_Id(1L)).thenReturn(Optional.of(review));

        // when + then
        assertThrows(NotFoundException.class,
                () -> analyticsService.weightedSuccessScore(1L));
    }

    // revenuePredictor
    // average of (euSales / naSales) across all games in genre

    @Test
    public void testRevenuePredictor() {
        // given — two games in RPG genre
        // game1: EU/NA = 8.0/4.0 = 2.0
        // game2: EU/NA = 6.0/2.0 = 3.0
        // average = (2.0 + 3.0) / 2 = 2.5
        SalesData s1 = salesData(4.0, 8.0, 1.0);
        SalesData s2 = salesData(2.0, 6.0, 0.5);

        when(salesDataRepository.findByGame_Genre("RPG")).thenReturn(List.of(s1, s2));

        // when
        double result = analyticsService.revenuePredictor("RPG");

        // then
        assertThat(result, is(2.5));
    }

    @Test
    public void testRevenuePredictor_NoData() {
        // given
        when(salesDataRepository.findByGame_Genre("Unknown")).thenReturn(List.of());

        // when + then
        assertThrows(NoDataFoundException.class,
                () -> analyticsService.revenuePredictor("Unknown"));
    }

    // globalAdaptabilityScore
    // 100 - (max% - min%) * 100 across NA/EU/JP shares

    @Test
    public void testGlobalAdaptabilityScore() {
        // given — equal sales: NA=4, EU=4, JP=4 → each region 33.3%
        // max - min = 0 → score = 100
        SalesData s1 = salesData(4.0, 4.0, 4.0);

        when(salesDataRepository.findByGame_Genre("Sports")).thenReturn(List.of(s1));

        // when
        double result = analyticsService.globalAdaptabilityScore("Sports");

        // then
        assertThat(result, is(100.0));
    }

    @Test
    public void testGlobalAdaptabilityScore_NoData() {
        // given
        when(salesDataRepository.findByGame_Genre("Unknown")).thenReturn(List.of());

        // when + then
        assertThrows(NoDataFoundException.class,
                () -> analyticsService.globalAdaptabilityScore("Unknown"));
    }

    // regionPriority
    // Returns the region with the highest average sales

    @Test
    public void testRegionPriority_EU() {
        // given — EU sales are the highest
        SalesData s1 = salesData(2.0, 8.0, 1.0);
        SalesData s2 = salesData(3.0, 7.0, 0.5);

        when(salesDataRepository.findByGame_Genre("RPG")).thenReturn(List.of(s1, s2));

        // when
        String result = analyticsService.regionPriority("RPG");

        // then
        assertThat(result, is("EU"));
    }

    @Test
    public void testRegionPriority_NA() {
        // given — NA sales are the highest
        SalesData s1 = salesData(10.0, 2.0, 1.0);

        when(salesDataRepository.findByGame_Genre("Action")).thenReturn(List.of(s1));

        // when
        String result = analyticsService.regionPriority("Action");

        // then
        assertThat(result, is("NA"));
    }

    @Test
    public void testRegionPriority_NoData() {
        // given
        when(salesDataRepository.findByGame_Genre("Unknown")).thenReturn(List.of());

        // when + then
        assertThrows(NoDataFoundException.class,
                () -> analyticsService.regionPriority("Unknown"));
    }

    // investmentRisk
    // % of games with criticScore > 70 but below-average sales

    @Test
    public void testInvestmentRisk() {
        // given — 2 games:
        // game1: criticScore=90, totalSales=2.0 (below average) → risky
        // game2: criticScore=60, totalSales=8.0 (above average) → not risky
        // average sales = (2.0 + 8.0) / 2 = 5.0
        // riskyCount = 1 → risk = 1/2 * 100 = 50.0
        SalesData s1 = salesDataWithReview(1.0, 1.0, 0.0, 90.0);
        SalesData s2 = salesDataWithReview(4.0, 4.0, 0.0, 60.0);

        when(salesDataRepository.findByGame_Genre("RPG")).thenReturn(List.of(s1, s2));

        // when
        double result = analyticsService.investmentRisk("RPG");

        // then
        assertThat(result, is(50.0));
    }

    @Test
    public void testInvestmentRisk_NoData() {
        // given
        when(salesDataRepository.findByGame_Genre("Unknown")).thenReturn(List.of());

        // when + then
        assertThrows(NoDataFoundException.class,
                () -> analyticsService.investmentRisk("Unknown"));
    }

    // Helper methods for creating test data

    private SalesData salesData(double na, double eu, double jp) {
        SalesData s = new SalesData();
        s.setNaSales(BigDecimal.valueOf(na));
        s.setEuSales(BigDecimal.valueOf(eu));
        s.setJpSales(BigDecimal.valueOf(jp));
        return s;
    }

    private SalesData salesDataWithReview(double na, double eu, double jp, double criticScore) {
        SalesData s = salesData(na, eu, jp);
        Review review = new Review();
        review.setCriticScore(BigDecimal.valueOf(criticScore));
        Game game = new Game();
        game.setReview(review);
        s.setGame(game);
        return s;
    }
}