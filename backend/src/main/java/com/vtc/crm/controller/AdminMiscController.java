package com.vtc.crm.controller;

import com.vtc.crm.entity.AuditLog;
import com.vtc.crm.entity.Task;
import com.vtc.crm.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class AdminMiscController {

    private final AuditLogRepository auditRepository;
    private final RoastBatchRepository roastRepository;
    private final PackingTicketRepository packingRepository;
    private final TaskRepository taskRepository;
    private final MaintenanceTicketRepository maintenanceRepository;
    private final OutgoingOrderRepository orderRepository;
    private final OrderDispatchRepository dispatchRepository;
    private final OutsideJobRepository outsideRepository;

    public AdminMiscController(
            AuditLogRepository auditRepository,
            RoastBatchRepository roastRepository,
            PackingTicketRepository packingRepository,
            TaskRepository taskRepository,
            MaintenanceTicketRepository maintenanceRepository,
            OutgoingOrderRepository orderRepository,
            OrderDispatchRepository dispatchRepository,
            OutsideJobRepository outsideRepository) {
        this.auditRepository = auditRepository;
        this.roastRepository = roastRepository;
        this.packingRepository = packingRepository;
        this.taskRepository = taskRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.orderRepository = orderRepository;
        this.dispatchRepository = dispatchRepository;
        this.outsideRepository = outsideRepository;
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
        Map<String, Object> res = new HashMap<>();

        // 1. My Work / Tasks (Open, In Progress, Submitted, Blocked)
        try {
            List<Task> openTasks = taskRepository.findByStatusNot("DONE");
            if (openTasks != null && !openTasks.isEmpty()) {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("count", (long) openTasks.size());
                List<Map<String, Object>> taskList = new ArrayList<>();
                for (Task t : openTasks) {
                    Map<String, Object> tm = new HashMap<>();
                    tm.put("id", t.getId());
                    tm.put("title", t.getTitle());
                    tm.put("kind", t.getKind());
                    tm.put("status", t.getStatus());
                    tm.put("progress", t.getProgress() != null ? t.getProgress() : 0.0);
                    tm.put("target", t.getTarget());
                    tm.put("unit", t.getUnit());
                    tm.put("due", t.getDue());
                    tm.put("assignee", t.getAssignee());
                    tm.put("assigned_to", t.getAssignedTo());
                    taskList.add(tm);
                }
                taskMap.put("tasks", taskList);
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", (long) openTasks.size());
                w.put("label", "tasks in progress");
                waiting.add(w);
                taskMap.put("waiting", waiting);
                res.put("tasks", taskMap);
            }
        } catch (Exception ignored) {}

        // 2. Quality Control (Batches waiting for QC Decision)
        try {
            long holdCount = roastRepository.countByQcStatus("QC_HOLD");
            if (holdCount > 0) {
                Map<String, Object> qc = new HashMap<>();
                qc.put("count", holdCount);
                qc.put("tasks", Collections.emptyList());
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", holdCount);
                w.put("label", "roast batches waiting for a quality decision");
                waiting.add(w);
                qc.put("waiting", waiting);
                res.put("qc", qc);
            }
        } catch (Exception ignored) {}

        // 3. Packaging Queue
        try {
            long unfinishedPacking = packingRepository.countByStateNot("COMPLETE");
            if (unfinishedPacking > 0) {
                Map<String, Object> packMap = new HashMap<>();
                packMap.put("count", unfinishedPacking);
                packMap.put("tasks", Collections.emptyList());
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", unfinishedPacking);
                w.put("label", "packing tickets not finished");
                waiting.add(w);
                packMap.put("waiting", waiting);
                res.put("packing", packMap);
            }
        } catch (Exception ignored) {}

        // 4. Maintenance (Open tickets needing repair)
        try {
            long openMaint = maintenanceRepository.findByStatusIn(Arrays.asList("OPEN", "IN_PROGRESS", "PARTS_WAIT")).size();
            if (openMaint > 0) {
                Map<String, Object> maintMap = new HashMap<>();
                maintMap.put("count", openMaint);
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", openMaint);
                w.put("label", "maintenance tickets needing attention");
                waiting.add(w);
                maintMap.put("waiting", waiting);
                res.put("maintenance", maintMap);
            }
        } catch (Exception ignored) {}

        // 5. Outgoing Orders (Waiting for dispatch)
        try {
            long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> "RESERVED".equalsIgnoreCase(o.getState()) || "CONFIRMED".equalsIgnoreCase(o.getState()))
                .count();
            if (pendingOrders > 0) {
                Map<String, Object> ordMap = new HashMap<>();
                ordMap.put("count", pendingOrders);
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", pendingOrders);
                w.put("label", "orders ready to dispatch");
                waiting.add(w);
                ordMap.put("waiting", waiting);
                res.put("orders", ordMap);
            }
        } catch (Exception ignored) {}

        // 6. Dispatch (In-transit / on the way)
        try {
            long inTransit = dispatchRepository.findAll().stream()
                .filter(d -> d.getDeliveredAt() == null)
                .count();
            if (inTransit > 0) {
                Map<String, Object> dispMap = new HashMap<>();
                dispMap.put("count", inTransit);
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", inTransit);
                w.put("label", "deliveries on the way");
                waiting.add(w);
                dispMap.put("waiting", waiting);
                res.put("dispatch", dispMap);
            }
        } catch (Exception ignored) {}

        // 7. Outside Roasting (Jobs in queue / not delivered)
        try {
            long outsideJobs = outsideRepository.findAll().stream()
                .filter(j -> !"DELIVERED".equalsIgnoreCase(j.getState()))
                .count();
            if (outsideJobs > 0) {
                Map<String, Object> outMap = new HashMap<>();
                outMap.put("count", outsideJobs);
                List<Map<String, Object>> waiting = new ArrayList<>();
                Map<String, Object> w = new HashMap<>();
                w.put("count", outsideJobs);
                w.put("label", "outside roasting jobs in queue");
                waiting.add(w);
                outMap.put("waiting", waiting);
                res.put("outside", outMap);
            }
        } catch (Exception ignored) {}

        return res;
    }
}

