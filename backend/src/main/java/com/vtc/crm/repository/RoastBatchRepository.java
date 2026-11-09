package com.vtc.crm.repository;

import com.vtc.crm.entity.RoastBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoastBatchRepository extends JpaRepository<RoastBatch, Long> {
    List<RoastBatch> findAllByOrderByCreatedAtDesc();
    List<RoastBatch> findTop30ByOrderByCreatedAtDesc();
    long countByQcStatus(String qcStatus);
    List<RoastBatch> findByQcStatusOrderByCreatedAtDesc(String qcStatus);

    @Query("SELECT r FROM RoastBatch r WHERE r.createdAt >= :start AND r.createdAt < :end ORDER BY r.createdAt DESC")
    List<RoastBatch> findBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

