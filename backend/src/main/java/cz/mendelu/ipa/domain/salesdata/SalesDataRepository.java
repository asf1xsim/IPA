package cz.mendelu.ipa.domain.salesdata;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SalesDataRepository extends CrudRepository<SalesData, Long> {

    @Override
    @EntityGraph(attributePaths = {"game"})
    @NotNull
    Iterable<SalesData> findAll();

    @EntityGraph(attributePaths = {"game", "game.review", "game.salesData", "game.platform"})
    @NotNull
    List<SalesData> findByGame_Genre(String genre);

    @EntityGraph(attributePaths = {"game", "game.review", "game.salesData", "game.platform"})
    @NotNull
    List<SalesData> findByGame_GenreAndGame_Platform_Name(String genre, String platformName);}
