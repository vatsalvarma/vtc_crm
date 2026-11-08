package com.vtc.crm.repository;

import com.vtc.crm.entity.RoastBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoastBatchRepository extends JpaRepository<RoastBatch, Long> {
    List<RoastBatch> findTop30ByOrderByCreatedAtDesc();
}

