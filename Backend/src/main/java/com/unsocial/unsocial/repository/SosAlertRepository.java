package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.SosAlert;
import com.unsocial.unsocial.entity.SosStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SosAlertRepository extends JpaRepository<SosAlert, Long> {

    // Get most recent alerts for a user
    List<SosAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get alerts filtered by status
    List<SosAlert> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, SosStatus status);

    // Find a specific alert owned by a user
    Optional<SosAlert> findByIdAndUserId(Long id, Long userId);

    // Find the currently active SOS (only one allowed at a time)
    Optional<SosAlert> findByUserIdAndStatus(Long userId, SosStatus status);

    // Count total SOS alerts sent by a user
    long countByUserId(Long userId);
}
