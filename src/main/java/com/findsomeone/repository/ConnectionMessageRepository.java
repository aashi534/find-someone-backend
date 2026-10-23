package com.findsomeone.repository;

import com.findsomeone.entity.ConnectionMessage;
import com.findsomeone.entity.ConnectionPurpose;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConnectionMessageRepository extends JpaRepository<ConnectionMessage, Long> {
    List<ConnectionMessage> findByConnectionIdOrderByCreatedAtDesc(
            Long connectionId, Pageable pageable);

    List<ConnectionMessage> findByConnectionIdAndPurposeOrderByCreatedAtDesc(
            Long connectionId, ConnectionPurpose purpose, Pageable pageable);
}
