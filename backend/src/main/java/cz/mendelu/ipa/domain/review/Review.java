package cz.mendelu.ipa.domain.review;

import cz.mendelu.ipa.domain.game.Game;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Game game;

    @Min(0)
    @Max(100)
    @Column(name = "critic_score")
    private BigDecimal criticScore;

    @Min(0)
    @Max(10)
    @Column(name = "user_score")
    private BigDecimal userScore;
}