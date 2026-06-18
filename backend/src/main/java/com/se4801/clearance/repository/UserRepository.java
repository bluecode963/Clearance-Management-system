package com.se4801.clearance.repository;

import com.se4801.clearance.model.User;
import com.se4801.clearance.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    @EntityGraph(attributePaths = "office")
    @Query("select user from User user where user.id = :id")
    Optional<User> findWithOfficeById(@Param("id") Long id);
}
