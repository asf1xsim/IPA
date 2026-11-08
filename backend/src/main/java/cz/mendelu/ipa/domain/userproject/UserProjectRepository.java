package cz.mendelu.ipa.domain.userproject;

import org.springframework.data.repository.CrudRepository;

public interface UserProjectRepository extends CrudRepository<UserProject, Long> {

    Iterable<UserProject> findByUsername(String username);
}