package com.vtc.crm.repository;

import com.vtc.crm.entity.MasterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterRecordRepository extends JpaRepository<MasterRecord, Long> {
    List<MasterRecord> findByKind(String kind);
    List<MasterRecord> findByKindAndState(String kind, String state);
}

