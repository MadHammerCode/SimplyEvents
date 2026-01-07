package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserRoleJpaRepository extends JpaRepository<UserRoleJpaEntity, Long> {
    Set<UserRoleJpaEntity> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}

