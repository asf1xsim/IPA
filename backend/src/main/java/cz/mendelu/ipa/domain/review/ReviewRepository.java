package cz.mendelu.ipa.domain.review;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ReviewRepository extends CrudRepository<Review, Long> {

    @Override
    @EntityGraph(attributePaths = {"game"})
    @NotNull
    Iterable<Review> findAll();


    @EntityGraph(attributePaths = {"game"})
    @NotNull
    Optional<Review> findByGame_Id(Long gameId);

}
