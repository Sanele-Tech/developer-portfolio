package com.sanele.portfolio.users.repository;

import com.sanele.portfolio.users.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findWithPermissionsByName(String name);
}