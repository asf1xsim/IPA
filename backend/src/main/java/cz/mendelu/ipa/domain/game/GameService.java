package cz.mendelu.ipa.domain.game;

import cz.mendelu.ipa.domain.platform.PlatformRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository repository;
    private final PlatformRepository platformRepository;

    public GameService(GameRepository repository, PlatformRepository platformRepository) {
        this.repository = repository;
        this.platformRepository = platformRepository;
    }

    @Cacheable("games")
    public List<Game> getAllGames() {
        List<Game> games = new ArrayList<>();
        repository.findAll().forEach(games::add);
        return games;
    }

    public Optional<Game> getGameById(Long id) {
        return repository.findById(id);
    }

    public List<Game> getGamesByGenre(String genre) {
        return repository.findByGenre(genre);
    }

    @Cacheable("genres")
    public List<String> getAllGenres() {
        return repository.findAllGenres();
    }

    @Cacheable("gameStats")
    public long[] getStats() {
        return new long[]{ repository.countAll(), repository.countDistinctGenres(), platformRepository.countAll() };
    }

    @Cacheable("topGames")
    public List<Game> getTopGames() {
        return repository.findTop6ByScore();
    }
}