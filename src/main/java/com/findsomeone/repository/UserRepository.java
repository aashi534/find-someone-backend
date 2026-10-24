package com.findsomeone.repository;

import com.findsomeone.entity.User;
import com.findsomeone.entity.ConnectionPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLinkedinSubject(String linkedinSubject);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByDiscoverableTrueOrderByNameAsc();

    @Query("select distinct u from User u left join u.connectionPurposes purpose "
            + "where u.discoverable = true "
            + "and (:purpose is null or purpose = :purpose) "
            + "and (:query is null or "
            + "lower(u.name) like lower(concat('%', :query, '%')) or "
            + "lower(coalesce(u.bio, '')) like lower(concat('%', :query, '%'))) "
            + "and (:city is null or lower(coalesce(u.city, '')) = lower(:city)) "
            + "order by u.name asc")
    List<User> searchDiscoverableUsers(@Param("purpose") ConnectionPurpose purpose,
                                       @Param("query") String query,
                                       @Param("city") String city);
}
