package com.findsomeone.repository;

import com.findsomeone.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    List<UserBlock> findByBlockerIdOrderByCreatedAtDesc(Long blockerId);

    @Query("select case when b.blocker.id = :userId then b.blocked.id "
            + "else b.blocker.id end from UserBlock b "
            + "where b.blocker.id = :userId or b.blocked.id = :userId")
    List<Long> findAllBlockedUserIds(@Param("userId") Long userId);

    @Query("select count(b) > 0 from UserBlock b where "
            + "(b.blocker.id = :userOneId and b.blocked.id = :userTwoId) or "
            + "(b.blocker.id = :userTwoId and b.blocked.id = :userOneId)")
    boolean existsBetweenUsers(@Param("userOneId") Long userOneId,
                               @Param("userTwoId") Long userTwoId);
}
