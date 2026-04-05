package com.finance.user.repository;

import com.finance.core.entity.UserEntity;
import com.finance.core.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {


    Optional<UserEntity> findByUsernameAndIsActiveTrue(String username);


    Optional<UserEntity> findByEmailAndIsActiveTrue(String email);


    boolean existsByUsernameAndIsActiveTrue(String username);


    boolean existsByEmailAndIsActiveTrue(String email);


    @Query("SELECT u FROM UserEntity u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    Page<UserEntity> findAllActiveUsers(Pageable pageable);


    @Query("""
        SELECT u FROM UserEntity u
        WHERE u.isActive = true
          AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY u.createdAt DESC
    """)
    Page<UserEntity> searchUsers(@Param("query") String query, Pageable pageable);


    Page<UserEntity> findByStatusAndIsActiveTrue(UserStatus status, Pageable pageable);
}