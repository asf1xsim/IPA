package cz.mendelu.ipa.domain.analytics;

import cz.mendelu.ipa.domain.game.Game;
import cz.mendelu.ipa.domain.game.GameRepository;
import cz.mendelu.ipa.domain.game.GameResponse;
import cz.mendelu.ipa.domain.review.ReviewRepository;
import cz.mendelu.ipa.domain.salesdata.SalesData;
import cz.mendelu.ipa.domain.salesdata.SalesDataRepository;
import cz.mendelu.ipa.domain.userproject.UserProject;
import cz.mendelu.ipa.domain.userproject.UserProjectRepository;
import cz.mendelu.ipa.utils.exceptions.NoDataFoundException;
import cz.mendelu.ipa.utils.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AnalyticsService {

    private final ReviewRepository reviewRepository;
    private final SalesDataRepository salesDataRepository;
    private final GameRepository gameRepository;
    private final UserProjectRepository userProjectRepository;

    public AnalyticsService(
            ReviewRepository reviewRepository,
            SalesDataRepository salesDataRepository,
            GameRepository gameRepository,
            UserProjectRepository userProjectRepository) {
        this.reviewRepository = reviewRepository;
        this.salesDataRepository = salesDataRepository;
        this.gameRepository = gameRepository;
        this.userProjectRepository = userProjectRepository;
    }

    private List<SalesData> getSalesData(String genre, String platform) {
        if (platform != null && !platform.isBlank()) {
            return salesDataRepository.findByGame_GenreAndGame_Platform_Name(genre, platform);
        }
        return salesDataRepository.findByGame_Genre(genre);
    }

    public double weightedSuccessScore(long gameId) {
        var review = reviewRepository.findByGame_Id(gameId)
                .orElseThrow(NotFoundException::new);

        if (review.getCriticScore() == null || review.getUserScore() == null) {
            throw new NotFoundException();
        }
        double critic = review.getCriticScore().doubleValue();
        double user   = review.getUserScore().doubleValue() * 10;
        return critic * 0.7 + user * 0.3;
    }

    public List<GameResponse> topCompetitors(String genre, String platform) {
        List<Game> games = gameRepository.findTop3Competitors(genre, platform);
        if (games.isEmpty()) throw new NoDataFoundException(genre);
        return games.stream().map(GameResponse::new).toList();
    }

    private record RegionalSales(double na, double eu, double jp) {}

    private RegionalSales getRegionalSales(Iterable<SalesData> sales) {
        double na = 0, eu = 0, jp = 0;
        for (var s : sales) {
            if (s.getNaSales() != null) na += s.getNaSales().doubleValue();
            if (s.getEuSales() != null) eu += s.getEuSales().doubleValue();
            if (s.getJpSales() != null) jp += s.getJpSales().doubleValue();
        }
        return new RegionalSales(na, eu, jp);
    }

    private double getTotalSales(SalesData s) {
        double total = 0;
        if (s.getNaSales() != null) total += s.getNaSales().doubleValue();
        if (s.getEuSales() != null) total += s.getEuSales().doubleValue();
        if (s.getJpSales() != null) total += s.getJpSales().doubleValue();
        return total;
    }

    public double revenuePredictor(String genre) {
        return revenuePredictor(genre, null);
    }

    public double revenuePredictor(String genre, String platform) {
        var sales = getSalesData(genre, platform);
        double sum = 0;
        int count  = 0;

        for (var s : sales) {
            if (s.getNaSales() != null && s.getNaSales().doubleValue() != 0
                    && s.getEuSales() != null) {
                double na = s.getNaSales().doubleValue();
                double eu = s.getEuSales().doubleValue();
                sum  += eu / na;
                count++;
            }
        }

        if (count == 0) throw new NoDataFoundException(genre);
        return sum / count;
    }

    public BigDecimal revenuePredictor(String genre, String baseRegion, BigDecimal baseAmount) {
        return revenuePredictor(genre, null, baseRegion, baseAmount);
    }

    public BigDecimal revenuePredictor(String genre, String platform, String baseRegion, BigDecimal baseAmount) {
        var sales = getSalesData(genre, platform);

        if (sales.isEmpty()) throw new NoDataFoundException(genre);

        double na = 0, eu = 0, jp = 0, other = 0;
        for (var s : sales) {
            if (s.getNaSales() != null) na += s.getNaSales().doubleValue();
            if (s.getEuSales() != null) eu += s.getEuSales().doubleValue();
            if (s.getJpSales() != null) jp += s.getJpSales().doubleValue();
        }

        double total = na + eu + jp + other;
        if (total == 0) throw new NoDataFoundException(genre);

        double baseShare = switch (baseRegion.toUpperCase()) {
            case "NA"    -> na / total;
            case "EU"    -> eu / total;
            case "JP"    -> jp / total;
            case "OTHER" -> other / total;
            default      -> throw new NoDataFoundException(baseRegion);
        };

        if (baseShare == 0) throw new NoDataFoundException(genre);

        double predicted = baseAmount.doubleValue() / baseShare;
        return BigDecimal.valueOf(Math.round(predicted * 100.0) / 100.0);
    }

    public double globalAdaptabilityScore(String genre) {
        return globalAdaptabilityScore(genre, null);
    }

    public double globalAdaptabilityScore(String genre, String platform) {
        var sales = getSalesData(genre, platform);
        var r = getRegionalSales(sales);

        double total = r.na() + r.eu() + r.jp();
        if (total == 0) throw new NoDataFoundException(genre);

        double naP = r.na() / total;
        double euP = r.eu() / total;
        double jpP = r.jp() / total;

        double max = Math.max(naP, Math.max(euP, jpP));
        double min = Math.min(naP, Math.min(euP, jpP));
        return 100 - (max - min) * 100;
    }

    public String regionPriority(String genre) {
        return regionPriority(genre, null);
    }

    public String regionPriority(String genre, String platform) {
        List<SalesData> sales = getSalesData(genre, platform);

        if (sales.isEmpty()) throw new NoDataFoundException(genre);

        int count = sales.size();
        var r     = getRegionalSales(sales);

        double naAvg = r.na() / count;
        double euAvg = r.eu() / count;
        double jpAvg = r.jp() / count;

        if (naAvg >= euAvg && naAvg >= jpAvg) return "NA";
        if (euAvg >= naAvg && euAvg >= jpAvg) return "EU";
        return "JP";
    }

    public double investmentRisk(String genre) {
        return investmentRisk(genre, null);
    }

    public double investmentRisk(String genre, String platform) {
        List<SalesData> sales = getSalesData(genre, platform);

        record Row(double totalSales, double criticScore) {}

        List<Row> rows = sales.stream()
                .filter(s -> {
                    if (s.getGame() == null || s.getGame().getReview() == null) return false;
                    if (s.getGame().getReview().getCriticScore() == null)        return false;
                    return s.getNaSales() != null
                            || s.getEuSales() != null
                            || s.getJpSales() != null;
                })
                .map(s -> new Row(
                        getTotalSales(s),
                        s.getGame().getReview().getCriticScore().doubleValue()))
                .toList();

        if (rows.isEmpty()) throw new NoDataFoundException(genre);

        double avgSales = rows.stream()
                .mapToDouble(Row::totalSales)
                .average()
                .orElseThrow();

        long riskyCount = rows.stream()
                .filter(r -> r.criticScore() > 70 && r.totalSales() < avgSales)
                .count();

        return (double) riskyCount / rows.size() * 100;
    }

    public record RevenuePredictionBreakdown(BigDecimal na, BigDecimal eu, BigDecimal jp) {}

    public RevenuePredictionBreakdown revenuePredictionBreakdown(String genre, String platform, String baseRegion, BigDecimal baseAmount) {
        var sales = getSalesData(genre, platform);
        if (sales.isEmpty()) throw new NoDataFoundException(genre);

        double na = 0, eu = 0, jp = 0;
        for (var s : sales) {
            if (s.getNaSales() != null) na += s.getNaSales().doubleValue();
            if (s.getEuSales() != null) eu += s.getEuSales().doubleValue();
            if (s.getJpSales() != null) jp += s.getJpSales().doubleValue();
        }

        double total = na + eu + jp;
        if (total == 0) throw new NoDataFoundException(genre);

        double baseShare = switch (baseRegion.toUpperCase()) {
            case "NA" -> na / total;
            case "EU" -> eu / total;
            case "JP" -> jp / total;
            default   -> throw new NoDataFoundException(baseRegion);
        };

        if (baseShare == 0) throw new NoDataFoundException(genre);

        double predicted = baseAmount.doubleValue() / baseShare;

        return new RevenuePredictionBreakdown(
                BigDecimal.valueOf(Math.round(predicted * (na / total) * 100.0) / 100.0),
                BigDecimal.valueOf(Math.round(predicted * (eu / total) * 100.0) / 100.0),
                BigDecimal.valueOf(Math.round(predicted * (jp / total) * 100.0) / 100.0)
        );
    }

    public String platformRecommender(String genre) {
        var sales = salesDataRepository.findByGame_Genre(genre);
        if (sales.isEmpty()) throw new NoDataFoundException(genre);

        record PlatformStats(double totalSales, double totalCritic, int count) {}

        Map<String, PlatformStats> map = new HashMap<>();
        for (var s : sales) {
            if (s.getGame() == null || s.getGame().getPlatform() == null) continue;
            if (s.getGame().getYearOfRelease() == null || s.getGame().getYearOfRelease() < 2000) continue;
            String pName = s.getGame().getPlatform().getName();
            double total = getTotalSales(s);
            double critic = 0;
            if (s.getGame().getReview() != null && s.getGame().getReview().getCriticScore() != null) {
                critic = s.getGame().getReview().getCriticScore().doubleValue();
            }
            var existing = map.getOrDefault(pName, new PlatformStats(0, 0, 0));
            map.put(pName, new PlatformStats(
                    existing.totalSales() + total,
                    existing.totalCritic() + critic,
                    existing.count() + 1));
        }

        if (map.isEmpty()) throw new NoDataFoundException(genre);

        double maxSales = map.values().stream()
                .filter(p -> p.count() >= 5)
                .mapToDouble(p -> p.totalSales() / p.count())
                .max()
                .orElse(1);

        return map.entrySet().stream()
                .filter(e -> e.getValue().count() >= 5)
                .max(Comparator.comparingDouble(e -> {
                    double avgSales  = e.getValue().totalSales() / e.getValue().count();
                    double avgCritic = e.getValue().totalCritic() / e.getValue().count();
                    return (avgSales / maxSales) * 0.6 + (avgCritic / 100.0) * 0.4;
                }))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new NoDataFoundException(genre));
    }

    public ProjectAnalyticsResponse projectAnalytics(Long projectId, String requestingUsername) {
        UserProject project = userProjectRepository.findById(projectId)
                .orElseThrow(NotFoundException::new);

        if (!project.getUsername().equals(requestingUsername)) {
            throw new NotFoundException();
        }

        String genre    = project.getTargetGenre();
        String platform = project.getTargetPlatform();

        ProjectAnalyticsResponse response = new ProjectAnalyticsResponse();

        try {
            response.setTopCompetitors(topCompetitors(genre, platform));
        } catch (NoDataFoundException e) {
            response.setTopCompetitors(List.of());
        }

        try {
            response.setGlobalAdaptability(globalAdaptabilityScore(genre, platform));
        } catch (NoDataFoundException e) {
            response.setGlobalAdaptability(null);
        }

        try {
            response.setInvestmentRisk(investmentRisk(genre, platform));
        } catch (NoDataFoundException e) {
            response.setInvestmentRisk(null);
        }

        try {
            response.setRegionPriority(regionPriority(genre, platform));
        } catch (NoDataFoundException e) {
            response.setRegionPriority(null);
        }

        if (project.getExpectedSalesRegion() != null && project.getExpectedSalesAmount() != null) {
            try {
                var breakdown = revenuePredictionBreakdown(
                        genre, platform,
                        project.getExpectedSalesRegion(),
                        project.getExpectedSalesAmount()
                );
                response.setPredictedNa(breakdown.na());
                response.setPredictedEu(breakdown.eu());
                response.setPredictedJp(breakdown.jp());
            } catch (NoDataFoundException e) {
                response.setPredictedNa(null);
                response.setPredictedEu(null);
                response.setPredictedJp(null);
            }
        }

        if (platform == null || platform.isBlank()) {
            try {
                response.setPlatformRecommendation(platformRecommender(genre));
            } catch (NoDataFoundException e) {
                response.setPlatformRecommendation(null);
            }
        }

        return response;
    }
}