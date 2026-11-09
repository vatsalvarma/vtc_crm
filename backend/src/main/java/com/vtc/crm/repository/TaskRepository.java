package com.vtc.crm.repository;

import com.vtc.crm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByCreatedAtDesc();
    List<Task> findByStatus(String status);
    List<Task> findByStatusNot(String status);
    List<Task> findByAssignedTo(Long assignedTo);
    List<Task> findByAssignedToAndKindAndStatusIn(Long assignedTo, String kind, List<String> statuses);
    long countByStatus(String status);
    long countByStatusNot(String status);
}

