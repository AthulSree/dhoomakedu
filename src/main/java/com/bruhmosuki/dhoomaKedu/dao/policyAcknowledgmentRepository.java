package com.bruhmosuki.dhoomaKedu.dao;

import com.bruhmosuki.dhoomaKedu.entity.policyAcknowledgment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface policyAcknowledgmentRepository extends JpaRepository<policyAcknowledgment, Long> {

    boolean existsByIpAddressAndAcknowledgedDate(String ipAddress, LocalDate acknowledgedDate);

    List<policyAcknowledgment> findByAcknowledgedDate(LocalDate acknowledgedDate);

    long countByAcknowledgedDate(LocalDate acknowledgedDate);

    @Transactional
    @Modifying
    @Query("DELETE FROM policyAcknowledgment p WHERE p.acknowledgedDate = :date")
    void deleteByAcknowledgedDate(@Param("date") LocalDate date);
}
