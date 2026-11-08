package cz.mendelu.ipa.domain.game;

import cz.mendelu.ipa.domain.platform.Platform;
import cz.mendelu.ipa.domain.review.Review;
import cz.mendelu.ipa.domain.salesdata.SalesData;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(nullable = false, length = 255)
    private String title;

    @NotEmpty
    @Column(nullable = false, length = 100)
    private String genre;

    @Column(name = "year_of_release")
    private Integer yearOfRelease;

    @Column(length = 255)
    private String publisher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    @EqualsAndHashCode.Exclude
    private Platform platform;

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private SalesData salesData;

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Review review;
}