package cz.mendelu.ipa.utils.data;

import com.opencsv.CSVReader;
import cz.mendelu.ipa.domain.game.Game;
import cz.mendelu.ipa.domain.game.GameRepository;
import cz.mendelu.ipa.domain.platform.Platform;
import cz.mendelu.ipa.domain.platform.PlatformRepository;
import cz.mendelu.ipa.domain.review.Review;
import cz.mendelu.ipa.domain.salesdata.SalesData;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataImporter implements ApplicationRunner {

    private final PlatformRepository platformRepository;
    private final GameRepository gameRepository;
    private final ResourceLoader resourceLoader;

    public DataImporter(PlatformRepository platformRepository, GameRepository gameRepository, ResourceLoader resourceLoader) {
        this.platformRepository = platformRepository;
        this.gameRepository = gameRepository;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) {
        importData();
    }

    @Transactional
    public String importData() {
        try {
            if (gameRepository.count() > 0) {
                log.info("Database already contains games data. Skipping import.");
                return "Database already contains games data. Skipping import.";
            }
        } catch (Exception e) {
            log.warn("Tables not ready yet or other error: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }

        Resource resource = resourceLoader.getResource("classpath:data/Video_Games_Sales_as_at_22_Dec_2016.csv");

        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            reader.skip(1);
            String[] nextLine;

            Map<String, Platform> platformCache = new HashMap<>();

            List<Game> batch = new ArrayList<>();
            int importedCount = 0;

            while ((nextLine = reader.readNext()) != null) {

                if (nextLine.length < 13 || nextLine[0] == null || nextLine[0].isBlank()) {
                    continue;
                }

                String title = nextLine[0];
                String platformName = nextLine[1];
                String yearStr = nextLine[2];
                String genre = nextLine[3];
                String publisher = nextLine[4];
                String naSalesStr = nextLine[5];
                String euSalesStr = nextLine[6];
                String jpSalesStr = nextLine[7];
                String criticScoreStr = nextLine[10];
                String userScoreStr = nextLine[12];

                Platform platform = platformCache.get(platformName);
                if (platform == null) {
                    platform = platformRepository.findByName(platformName)
                            .orElseGet(() -> platformRepository.save(new Platform(platformName)));
                    platformCache.put(platformName, platform);
                }

                Game game = new Game();
                game.setTitle(title);
                game.setGenre(genre);
                game.setPublisher(publisher.equalsIgnoreCase("N/A") ? null : publisher);
                game.setPlatform(platform);

                if (yearStr != null && !yearStr.isBlank() && !yearStr.equalsIgnoreCase("N/A")) {
                    try {
                        double yearDouble = Double.parseDouble(yearStr);
                        game.setYearOfRelease((int) yearDouble);
                    } catch (NumberFormatException e) {}
                }

                SalesData salesData = new SalesData();
                salesData.setGame(game);
                salesData.setNaSales(safeParseBigDecimal(naSalesStr));
                salesData.setEuSales(safeParseBigDecimal(euSalesStr));
                salesData.setJpSales(safeParseBigDecimal(jpSalesStr));
                game.setSalesData(salesData);

                Review review = new Review();
                review.setGame(game);
                review.setCriticScore(safeParseBigDecimal(criticScoreStr));

                if (userScoreStr != null && !userScoreStr.isBlank() && !userScoreStr.equalsIgnoreCase("tbd")) {
                    try {
                        review.setUserScore(new BigDecimal(userScoreStr));
                    } catch (NumberFormatException e) {}
                }
                game.setReview(review);

                batch.add(game);
                importedCount++;

                if (batch.size() >= 500) {
                    gameRepository.saveAll(batch);
                    batch.clear();
                    log.info("Imported {} games...", importedCount);
                }
            }
            if (!batch.isEmpty()) {
                gameRepository.saveAll(batch);
            }

            log.info("Import successfully finished. Imported {} games, {} platforms.", importedCount, platformCache.size());
            return "Import finished. Imported " + importedCount + " games, " + platformCache.size() + " platforms.";

        } catch (Exception e) {
            log.error("Critical error while importing data.", e);
            return "Import failed: " + e.getMessage();
        }
    }

    private BigDecimal safeParseBigDecimal(String val) {
        if (val == null || val.isBlank() || val.equalsIgnoreCase("N/A")) {
            return null;
        }
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
