package cz.mendelu.ipa.domain.platform;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformRepository extends CrudRepository<Platform, Long> {

    @Override
    @EntityGraph(attributePaths = {"games"})
    @NotNull
    Iterable<Platform> findAll();

    @Query("SELECT p FROM Platform p ORDER BY p.name")
    List<Platform> findAllLight();

    @Override
    @EntityGraph(attributePaths = {"games"})
    @NotNull
    Optional<Platform> findById(@NotNull Long id);

    Optional<Platform> findByName(String name);

    @Query("SELECT COUNT(p) FROM Platform p")
    long countAll();
}