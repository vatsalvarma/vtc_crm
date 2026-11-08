package com.vtc.crm.repository;

import com.vtc.crm.entity.ClientCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientCollectionRepository extends JpaRepository<ClientCollection, Long> {
    List<ClientCollection> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<ClientCollection> findByCancelledAtIsNull();
}

