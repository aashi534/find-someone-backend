package com.findsomeone.repository;

import com.findsomeone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLinkedinSubject(String linkedinSubject);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByDiscoverableTrueOrderByNameAsc();
}
