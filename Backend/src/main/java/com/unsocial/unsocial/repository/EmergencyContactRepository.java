package com.unsocial.unsocial.repository;

import com.unsocial.unsocial.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    // All contacts for a user
    List<EmergencyContact> findByUserIdOrderByIsPrimaryDescCreatedAtAsc(Long userId);

    // Specific contact belonging to a user (prevents accessing other users' contacts)
    Optional<EmergencyContact> findByIdAndUserId(Long id, Long userId);

    // Count contacts per user (to enforce max limit)
    int countByUserId(Long userId);

    // Check if user already has a primary contact
   // boolean existsByUserIdAndIsPhone(Long userId,String Phone);
    boolean existsByUserIdAndPhone(
            Long userId,
            String phone

    );

    // Get primary contact for SOS
    Optional<EmergencyContact> findByUserIdAndIsPrimaryTrue(Long userId);

    // All contacts that want SOS notifications
    List<EmergencyContact> findByUserIdAndNotifyOnSosTrue(Long userId);

    //boolean existsByUserIdAndPhone(Long userId, String phone);


}
