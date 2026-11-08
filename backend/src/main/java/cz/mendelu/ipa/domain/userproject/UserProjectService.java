package cz.mendelu.ipa.domain.userproject;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserProjectService {

    private final UserProjectRepository repository;

    public UserProjectService(UserProjectRepository repository) {
        this.repository = repository;
    }

    public List<UserProject> getProjectsByUsername(String username) {
        List<UserProject> projects = new ArrayList<>();
        repository.findByUsername(username).forEach(projects::add);
        return projects;
    }

    public Optional<UserProject> getProjectById(Long id) {
        return repository.findById(id);
    }

    public UserProject createProject(UserProject project) {
        return repository.save(project);
    }

    public UserProject updateProject(Long id, UserProject updated) {
        updated.setId(id);
        return repository.save(updated);
    }

    public void deleteProject(Long id) {
        repository.deleteById(id);
    }
}