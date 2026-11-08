package cz.mendelu.ipa.domain.game;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends CrudRepository<Game, Long> {

    @Override
    @EntityGraph(attributePaths = {"platform", "salesData", "review"})
    @NotNull
    Iterable<Game> findAll();

    @Override
    @EntityGraph(attributePaths = {"platform", "salesData", "review"})
    @NotNull
    Optional<Game> findById(@NotNull Long id);

    @EntityGraph(attributePaths = {"platform", "salesData", "review"})
    List<Game> findByGenre(String genre);

    @Query("SELECT DISTINCT g.genre FROM Game g ORDER BY g.genre")
    List<String> findAllGenres();

    @Query("SELECT COUNT(g) FROM Game g")
    long countAll();

    @Query("SELECT COUNT(DISTINCT g.genre) FROM Game g")
    long countDistinctGenres();

    @Query("SELECT g FROM Game g " +
            "JOIN FETCH g.platform " +
            "LEFT JOIN FETCH g.salesData " +
            "JOIN FETCH g.review r " +
            "WHERE r.criticScore IS NOT NULL AND r.userScore IS NOT NULL " +
            "ORDER BY (r.criticScore * 0.7 + r.userScore * 10 * 0.3) DESC " +
            "LIMIT 6")
    List<Game> findTop6ByScore();

    @Query("SELECT g FROM Game g " +
            "JOIN FETCH g.platform p " +
            "LEFT JOIN FETCH g.salesData " +
            "JOIN FETCH g.review r " +
            "WHERE g.genre = :genre AND (:platform IS NULL OR p.name = :platform) " +
            "AND r.criticScore IS NOT NULL AND r.userScore IS NOT NULL " +
            "ORDER BY (r.criticScore * 0.7 + r.userScore * 10 * 0.3) DESC " +
            "LIMIT 3")
    List<Game> findTop3Competitors(@Param("genre") String genre, @Param("platform") String platform);
}