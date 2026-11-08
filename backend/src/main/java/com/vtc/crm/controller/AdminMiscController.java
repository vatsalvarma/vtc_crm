package com.vtc.crm.controller;

import com.vtc.crm.entity.AuditLog;
import com.vtc.crm.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class AdminMiscController {

    private final AuditLogRepository auditRepository;

    public AdminMiscController(AuditLogRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @GetMapping("/admin/audit")
    public List<AuditLog> getAuditLogs() {
        return auditRepository.findTop50ByOrderByTimestampDesc();
    }

    @GetMapping("/admin/settings")
    public Map<String, Object> getSettings() {
        Map<String, Object> s = new HashMap<>();
        s.put("company_name", "Vasavi Trading Company");
        s.put("phone", "9849012345");
        s.put("email", "info@vasavitrading.com");
        s.put("quiet_hours", "22:00-06:00");
        s.put("invoice_prefix", "VTC-26");
        s.put("counter_float_rupees", 500);
        return s;
    }

    @PostMapping("/admin/settings")
    public Map<String, Object> saveSettings(@RequestBody Map<String, Object> settings) {
        return settings;
    }

    @GetMapping("/admin/dashboard-defaults")
    public Map<String, Object> getDashboardDefaults() {
        Map<String, Object> map = new HashMap<>();
        map.put("cards", Arrays.asList("inward", "roasting", "inventory", "sales", "counter"));
        return map;
    }

    @GetMapping("/golive")
    public Map<String, Object> getGoLive() {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> state = new HashMap<>();
        state.put("state", "SETUP");
        state.put("cash_paise", null);
        map.put("state", state);

        Map<String, Object> counts = new HashMap<>();
        counts.put("clients", 2);
        counts.put("suppliers", 1);
        counts.put("beans", 4);
        counts.put("customers", 1);
        map.put("counts", counts);

        map.put("stock", Collections.emptyList());
        map.put("dues", Collections.emptyList());
        return map;
    }

    @GetMapping("/i18n")
    public Map<String, Object> getI18n() {
        return Collections.emptyMap();
    }

    @GetMapping("/pending")
    public Map<String, Object> getPending() {
        return Collections.emptyMap();
    }

    @GetMapping("/comms/badge")
    public Map<String, Object> getCommsBadge() {
        Map<String, Object> map = new HashMap<>();
        map.put("messages", 0);
        map.put("alerts", 0);
        return map;
    }
}

