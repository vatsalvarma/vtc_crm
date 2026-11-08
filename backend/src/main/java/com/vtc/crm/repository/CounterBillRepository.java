package com.vtc.crm.repository;

import com.vtc.crm.entity.CounterBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounterBillRepository extends JpaRepository<CounterBill, Long> {
    List<CounterBill> findTop50ByOrderByCreatedAtDesc();
}

