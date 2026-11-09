package com.vtc.crm.repository;

import com.vtc.crm.entity.OutsideJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutsideJobRepository extends JpaRepository<OutsideJob, Long> {
    List<OutsideJob> findAllByOrderByCreatedAtDesc();
    long countByState(String state);
}

