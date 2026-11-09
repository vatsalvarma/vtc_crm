package com.vtc.crm.repository;

import com.vtc.crm.entity.PackingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingTicketRepository extends JpaRepository<PackingTicket, Long> {
    List<PackingTicket> findAllByOrderByCreatedAtDesc();
    long countByStateNot(String state);
    List<PackingTicket> findByStateNotOrderByCreatedAtDesc(String state);
}

