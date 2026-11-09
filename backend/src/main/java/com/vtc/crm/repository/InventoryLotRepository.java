package com.vtc.crm.repository;

import com.vtc.crm.entity.InventoryLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryLotRepository extends JpaRepository<InventoryLot, Long> {
    List<InventoryLot> findAllByOrderByCreatedAtDesc();
    List<InventoryLot> findByReceiptId(Long receiptId);
    List<InventoryLot> findByStatusNot(String status);
}

