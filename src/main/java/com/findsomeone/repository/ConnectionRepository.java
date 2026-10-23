package com.findsomeone.repository;

import com.findsomeone.entity.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    @Query("select c from Connection c where c.userOne.id = :userId or c.userTwo.id = :userId order by c.createdAt desc")
    List<Connection> findAllForUser(@Param("userId") Long userId);

    @Query("select c from Connection c where c.userOne.id = :lowId and c.userTwo.id = :highId")
    Optional<Connection> findPair(@Param("lowId") Long lowId,
                                  @Param("highId") Long highId);
}
