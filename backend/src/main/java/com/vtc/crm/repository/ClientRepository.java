package com.vtc.crm.repository;

import com.vtc.crm.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByClientType(String clientType);
    List<Client> findByActiveTrue();
    List<Client> findByClientTypeAndActiveTrue(String clientType);
}

