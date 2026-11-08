package cz.mendelu.ipa.domain.platform;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlatformService {

    private final PlatformRepository repository;

    public PlatformService(PlatformRepository repository) {
        this.repository = repository;
    }

    @Cacheable("platforms")
    public List<Platform> getAllPlatforms() {
        List<Platform> platforms = new ArrayList<>();
        repository.findAll().forEach(platforms::add);
        return platforms;
    }

    @Transactional
    @Cacheable("platformsLight")
    public List<Platform> getAllPlatformsLight() {
        return repository.findAllLight();
    }

    public Optional<Platform> getPlatformById(Long id) {
        return repository.findById(id);
    }

    public Platform createPlatform(Platform platform) {
        return repository.save(platform);
    }

    public Platform updatePlatform(Long id, Platform updated) {
        updated.setId(id);
        return repository.save(updated);
    }

    public void deletePlatform(Long id) {
        repository.deleteById(id);
    }
}