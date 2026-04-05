package com.finance.role.repository;

import com.finance.core.entity.RoleEntity;
import com.finance.core.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {


    Optional<RoleEntity> findByNameAndIsActiveTrue(RoleName name);

    boolean existsByName(RoleName name);
}