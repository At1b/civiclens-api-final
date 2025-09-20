package com.civiclens.api.repository;

import com.civiclens.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA automatically creates the query based on the method name
    Optional<User> findByEmail(String email);
}
