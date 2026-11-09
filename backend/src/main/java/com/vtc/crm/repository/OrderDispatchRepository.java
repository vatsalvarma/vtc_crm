package com.vtc.crm.repository;

import com.vtc.crm.entity.OrderDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDispatchRepository extends JpaRepository<OrderDispatch, Long> {
    List<OrderDispatch> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    List<OrderDispatch> findAllByOrderByCreatedAtDesc();
}

