package cz.mendelu.ipa.domain.game;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GameResponse {

    @NotNull
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String genre;

    private Integer yearOfRelease;

    private String publisher;

    @NotNull
    private Long platformId;

    private String platformName;

    private BigDecimal naSales;
    private BigDecimal euSales;
    private BigDecimal jpSales;

    private BigDecimal criticScore;
    private BigDecimal userScore;

    public GameResponse(Game game) {
        this.id = game.getId();
        this.title = game.getTitle();
        this.genre = game.getGenre();
        this.yearOfRelease = game.getYearOfRelease();
        this.publisher = game.getPublisher();
        this.platformId = game.getPlatform().getId();
        this.platformName = game.getPlatform().getName();
        if (game.getSalesData() != null) {
            this.naSales = game.getSalesData().getNaSales();
            this.euSales = game.getSalesData().getEuSales();
            this.jpSales = game.getSalesData().getJpSales();
        }

        if (game.getReview() != null) {
            this.criticScore = game.getReview().getCriticScore();
            this.userScore = game.getReview().getUserScore();
        }
    }
}
