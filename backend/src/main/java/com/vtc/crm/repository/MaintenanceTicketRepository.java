package com.vtc.crm.repository;

import com.vtc.crm.entity.MaintenanceTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {
    List<MaintenanceTicket> findAllByOrderByCreatedAtDesc();
    List<MaintenanceTicket> findByStatusIn(List<String> statuses);
    List<MaintenanceTicket> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
}

