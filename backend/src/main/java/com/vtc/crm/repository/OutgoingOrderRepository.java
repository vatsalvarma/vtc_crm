package com.vtc.crm.repository;

import com.vtc.crm.entity.OutgoingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutgoingOrderRepository extends JpaRepository<OutgoingOrder, Long> {
    List<OutgoingOrder> findAllByOrderByCreatedAtDesc();
}

