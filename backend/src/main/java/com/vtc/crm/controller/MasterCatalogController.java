package com.vtc.crm.controller;

import com.vtc.crm.entity.MasterRecord;
import com.vtc.crm.repository.MasterRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class MasterCatalogController {

    private final MasterRecordRepository masterRepository;

    public MasterCatalogController(MasterRecordRepository masterRepository) {
        this.masterRepository = masterRepository;
    }

    @GetMapping("/masters")
    public List<MasterRecord> listMasters(@RequestParam(required = false) String kind) {
        if (kind != null && !kind.isEmpty()) {
            return masterRepository.findByKind(kind.toUpperCase());
        }
        return masterRepository.findAll();
    }

    @PostMapping("/masters")
    public MasterRecord createMaster(@RequestBody MasterRecord record) {
        if (record.getState() == null) record.setState("DRAFT");
        return masterRepository.save(record);
    }

    @PostMapping("/masters/{id}/approve")
    public ResponseEntity<?> approveMaster(@PathVariable Long id) {
        Optional<MasterRecord> opt = masterRepository.findById(id);
        if (opt.isPresent()) {
            MasterRecord m = opt.get();
            m.setState("ACTIVE");
            masterRepository.save(m);
            return ResponseEntity.ok(m);
        }
        return ResponseEntity.notFound().build();
    }
}

