package com.vtc.crm.repository;

import com.vtc.crm.entity.InwardReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InwardReceiptRepository extends JpaRepository<InwardReceipt, Long> {
    List<InwardReceipt> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(r.bags), 0) FROM InwardReceipt r WHERE r.reversedAt IS NULL")
    Long sumTotalActiveBags();
}

