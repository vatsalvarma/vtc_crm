package com.vtc.crm.controller;

import com.vtc.crm.entity.CounterBill;
import com.vtc.crm.entity.InwardReceipt;
import com.vtc.crm.entity.InventoryLot;
import com.vtc.crm.entity.MaintenanceTicket;
import com.vtc.crm.entity.MasterRecord;
import com.vtc.crm.entity.Task;
import com.vtc.crm.entity.User;
import com.vtc.crm.repository.CounterBillRepository;
import com.vtc.crm.repository.InwardReceiptRepository;
import com.vtc.crm.repository.InventoryLotRepository;
import com.vtc.crm.repository.MaintenanceTicketRepository;
import com.vtc.crm.repository.MasterRecordRepository;
import com.vtc.crm.repository.RoastBatchRepository;
import com.vtc.crm.repository.TaskRepository;
import com.vtc.crm.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1")
public class PortalEndpointsController {

    private final RoastBatchRepository roastRepository;
    private final CounterBillRepository counterRepository;
    private final MasterRecordRepository masterRepository;
    private final UserRepository userRepository;
    private final MaintenanceTicketRepository maintenanceTicketRepository;
    private final TaskRepository taskRepository;
    private final InwardReceiptRepository inwardReceiptRepository;
    private final InventoryLotRepository inventoryLotRepository;

    private final List<Map<String, Object>> transfersList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> ordersList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> outsideJobsList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> packingJobsList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> blendRecipes = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> blendJobs = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> grindJobs = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> roastProfiles = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> counterItems = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> cashCloseList = new CopyOnWriteArrayList<>();

    // Finance data structures
    private final List<Map<String, Object>> receivablesInvoicesList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> receivablesPendingList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> payablesReceiptsList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> payablesPaymentsList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> tallyExportsList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> openingDuesList = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> openingDuesPendingList = new CopyOnWriteArrayList<>();

    public PortalEndpointsController(
            RoastBatchRepository roastRepository,
            CounterBillRepository counterRepository,
            MasterRecordRepository masterRepository,
            UserRepository userRepository,
            MaintenanceTicketRepository maintenanceTicketRepository,
            TaskRepository taskRepository,
            InwardReceiptRepository inwardReceiptRepository,
            InventoryLotRepository inventoryLotRepository) {
        this.roastRepository = roastRepository;
        this.counterRepository = counterRepository;
        this.masterRepository = masterRepository;
        this.userRepository = userRepository;
        this.maintenanceTicketRepository = maintenanceTicketRepository;
        this.taskRepository = taskRepository;
        this.inwardReceiptRepository = inwardReceiptRepository;
        this.inventoryLotRepository = inventoryLotRepository;

        initDefaults();
    }

    private void initDefaults() {
        // Default roast profiles
        Map<String, Object> p1 = new HashMap<>();
        p1.put("id", 1);
        p1.put("name", "Medium Roast (Standard)");
        p1.put("active", true);
        p1.put("notes", "12 min @ 205°C - Balanced acidity & body");
        roastProfiles.add(p1);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("id", 2);
        p2.put("name", "Dark Roast (Espresso / South Indian Filter)");
        p2.put("active", true);
        p2.put("notes", "14 min @ 218°C - Rich chocolate note");
        roastProfiles.add(p2);

        // Counter sales items
        Map<String, Object> ci1 = new HashMap<>();
        ci1.put("id", 1);
        ci1.put("name", "Fresh Filter Coffee (Cup)");
        ci1.put("price_paise", 3500);
        ci1.put("active", true);
        counterItems.add(ci1);

        Map<String, Object> ci2 = new HashMap<>();
        ci2.put("id", 2);
        ci2.put("name", "Classic House Blend 80:20 (250g)");
        ci2.put("price_paise", 18000);
        ci2.put("active", true);
        counterItems.add(ci2);

        Map<String, Object> ci3 = new HashMap<>();
        ci3.put("id", 3);
        ci3.put("name", "Pure Arabica Roast Beans (500g)");
        ci3.put("price_paise", 42000);
        ci3.put("active", true);
        counterItems.add(ci3);

        // Initial receivables invoices
        Map<String, Object> inv1 = new HashMap<>();
        inv1.put("id", 1);
        inv1.put("number", "INV-2610-0001");
        inv1.put("customer", "Sample Café, Banjara Hills");
        inv1.put("customer_phone", "9000000001");
        inv1.put("total_paise", 546000L);
        inv1.put("paid_paise", 200000L);
        inv1.put("due_paise", 346000L);
        inv1.put("due_date", "2026-11-11");
        inv1.put("days_late", 0);
        inv1.put("status", "PART_PAID");
        receivablesInvoicesList.add(inv1);

        // Initial payables receipts
        Map<String, Object> r1 = new HashMap<>();
        r1.put("id", 1);
        r1.put("created_at", "2026-10-27T10:00:00");
        r1.put("source_name", "Sample Coffee Auction House");
        r1.put("product", "Arabica Plantation A");
        r1.put("bags", 40);
        r1.put("invoice_ref", "INV-S-0042");
        r1.put("invoice_status", "MATCHED");
        r1.put("invoice_amount_paise", 24000000L);
        r1.put("paid_paise", 24000000L);
        r1.put("payment_status", "PAID");
        payablesReceiptsList.add(r1);

        // Initial Tally export
        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", 1);
        t1.put("batch_no", "TALLY-20261108-01");
        t1.put("created_at", "2026-11-08T09:00:00");
        t1.put("period_from", "2026-11-01");
        t1.put("period_to", "2026-11-08");
        t1.put("voucher_count", 14);
        t1.put("control_total_paise", 4850000L);
        t1.put("checksum", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        t1.put("state", "ACCEPTED");
        t1.put("result_note", "Imported into TallyPrime without errors");
        t1.put("content", "Date,VoucherType,VoucherNo,Party,Debit,Credit\n2026-11-08,Sales,INV-2610-0001,Sample Café Banjara Hills,5460,0\n");
        tallyExportsList.add(t1);

        // Initial Old Dues
        Map<String, Object> d1 = new HashMap<>();
        d1.put("id", 1);
        d1.put("side", "CLIENT");
        d1.put("party", "Kumar Traders");
        d1.put("party_phone", "9000000002");
        d1.put("amount_paise", 1880000L);
        d1.put("paid_paise", 0L);
        d1.put("due_paise", 1880000L);
        d1.put("due_date", "2026-11-01");
        d1.put("note", "Opening balance from before portal go-live");
        openingDuesList.add(d1);
    }

    private Map<String, Object> createStockItem(String bean, String stage, String location, double kg) {
        Map<String, Object> m = new HashMap<>();
        m.put("bean", bean);
        m.put("stage", stage);
        m.put("location", location);
        m.put("kg", kg);
        return m;
    }

    // ---------------------------------------------------- /summary
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> s = new HashMap<>();
        long activeTasks = 0;
        long toReviewTasks = 0;
        try {
            activeTasks = taskRepository.countByStatusNot("DONE");
            toReviewTasks = taskRepository.countByStatus("SUBMITTED");
        } catch (Exception ignored) {}
        s.put("tasks", activeTasks);
        long receivedBags = 0;
        try { receivedBags = inwardReceiptRepository.sumTotalActiveBags(); } catch (Exception ignored) {}
        s.put("received_bags", receivedBags);

        long beansInStock = 0;
        try { beansInStock = inventoryLotRepository.findByStatusNot("REJECTED").size(); } catch (Exception ignored) {}
        s.put("beans_in_stock", beansInStock);
        s.put("beans_low", 0);
        long roasts = 0;
        try { roasts = roastRepository.count(); } catch (Exception ignored) {}
        s.put("roasts", roasts);
        s.put("packing", packingJobsList.size());
        s.put("team_tasks", activeTasks);
        s.put("to_review", toReviewTasks);
        s.put("held", 0);
        s.put("orders", ordersList.size());
        long bills = 0;
        try { bills = counterRepository.count(); } catch (Exception ignored) {}
        s.put("pending_invoices", bills);
        return s;
    }

    // ---------------------------------------------------- /tasks & /tasks/options
    @GetMapping("/tasks")
    public List<Map<String, Object>> getTasks(@AuthenticationPrincipal User currentUser) {
        List<Task> tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> res = new ArrayList<>();
        for (Task t : tasks) {
            res.add(formatTask(t, currentUser));
        }
        return res;
    }

    @GetMapping("/tasks/options")
    public Map<String, Object> getTaskOptions() {
        Map<String, Object> options = new HashMap<>();

        List<Map<String, Object>> kinds = new ArrayList<>();
        kinds.add(createKind("ROAST", "Roasting", "batches"));
        kinds.add(createKind("PACK", "Packing", "packs"));
        kinds.add(createKind("QC", "Quality Control", "lots"));
        kinds.add(createKind("OUTSIDE", "Outside Roasting", "jobs"));
        kinds.add(createKind("COUNTER", "Counter Sales", "bills"));
        kinds.add(createKind("DISPATCH", "Dispatch", "orders"));
        kinds.add(createKind("INWARD", "Stock Inward", "bags"));
        kinds.add(createKind("BLEND", "Blending", "kg"));
        kinds.add(createKind("COUNT", "Cycle Count", "lots"));
        kinds.add(createKind("GENERAL", "General Work", ""));
        options.put("kinds", kinds);

        List<Map<String, Object>> people = new ArrayList<>();
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if ("ACTIVE".equalsIgnoreCase(u.getStatus())) {
                Map<String, Object> p = new HashMap<>();
                p.put("id", u.getId());
                p.put("full_name", u.getFullName());
                p.put("role_name", u.getRole());
                p.put("can", Arrays.asList("ROAST", "PACK", "QC", "OUTSIDE", "COUNTER", "DISPATCH", "INWARD", "BLEND", "COUNT", "GENERAL"));
                people.add(p);
            }
        }
        options.put("people", people);
        options.put("pack_jobs", packingJobsList);
        options.put("orders", ordersList);

        return options;
    }

    private Map<String, Object> createKind(String key, String label, String unit) {
        Map<String, Object> k = new HashMap<>();
        k.put("key", key);
        k.put("label", label);
        k.put("unit", unit);
        return k;
    }

    private String getKindLabel(String kind) {
        if (kind == null) return "General";
        switch (kind.toUpperCase()) {
            case "ROAST": return "Roasting";
            case "PACK": return "Packing";
            case "QC": return "Quality Control";
            case "OUTSIDE": return "Outside Roasting";
            case "COUNTER": return "Counter Sales";
            case "DISPATCH": return "Dispatch";
            case "INWARD": return "Stock Inward";
            case "BLEND": return "Blending";
            case "COUNT": return "Cycle Count";
            default: return "General";
        }
    }

    private String getKindUnit(String kind) {
        if (kind == null) return "";
        switch (kind.toUpperCase()) {
            case "ROAST": return "batches";
            case "PACK": return "packs";
            case "QC": return "lots";
            case "OUTSIDE": return "jobs";
            case "COUNTER": return "bills";
            case "DISPATCH": return "orders";
            case "INWARD": return "bags";
            case "BLEND": return "kg";
            case "COUNT": return "lots";
            default: return "";
        }
    }

    private Map<String, Object> formatTask(Task t, User currentUser) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("title", t.getTitle());
        map.put("kind", t.getKind());
        map.put("kind_label", t.getKindLabel() != null ? t.getKindLabel() : getKindLabel(t.getKind()));
        map.put("unit", t.getUnit() != null ? t.getUnit() : getKindUnit(t.getKind()));
        map.put("status", t.getStatus());
        map.put("progress", t.getProgress() != null ? t.getProgress() : 0.0);
        map.put("target", t.getTarget());
        map.put("due", t.getDue());
        map.put("assigned_to", t.getAssignedTo());
        map.put("assignee", t.getAssignee());
        map.put("assigned_by", t.getAssignedBy());
        map.put("assigned_by_id", t.getAssignedById());
        map.put("submit_note", t.getSubmitNote());
        map.put("review_note", t.getReviewNote());
        map.put("reviewed_by_id", t.getReviewedById());
        map.put("reviewed_by_name", t.getReviewedByName());
        map.put("ref_id", t.getRefId());
        map.put("created_at", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);

        boolean isAssigner = currentUser != null && currentUser.getId() != null && Objects.equals(currentUser.getId(), t.getAssignedById());
        boolean isBoss = currentUser != null && ("Admin".equalsIgnoreCase(currentUser.getRole()) || "Manager".equalsIgnoreCase(currentUser.getRole()) || "owner".equalsIgnoreCase(currentUser.getUsername()));
        boolean mayReview = "SUBMITTED".equalsIgnoreCase(t.getStatus()) && (isAssigner || isBoss || currentUser == null);
        map.put("may_review", mayReview);

        boolean isMine = false;
        if (currentUser != null) {
            if (currentUser.getId() != null && Objects.equals(currentUser.getId(), t.getAssignedTo())) {
                isMine = true;
            } else if (currentUser.getFullName() != null && t.getAssignee() != null &&
                    currentUser.getFullName().trim().equalsIgnoreCase(t.getAssignee().trim())) {
                isMine = true;
            }
        }
        map.put("mine", isMine);

        return map;
    }

    @PostMapping("/tasks")
    public Map<String, Object> createTask(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Task task = new Task();
        String kind = req.get("kind") != null ? req.get("kind").toString() : "GENERAL";
        task.setKind(kind);
        task.setKindLabel(getKindLabel(kind));
        task.setUnit(getKindUnit(kind));

        if (req.get("target") != null && !req.get("target").toString().isEmpty()) {
            try {
                task.setTarget(Double.parseDouble(req.get("target").toString()));
            } catch (Exception ignored) {}
        }
        task.setProgress(0.0);
        task.setStatus("OPEN");

        if (req.get("due") != null) {
            task.setDue(req.get("due").toString());
        }
        if (req.get("ref_id") != null && !req.get("ref_id").toString().isEmpty()) {
            try {
                task.setRefId(Long.parseLong(req.get("ref_id").toString()));
            } catch (Exception ignored) {}
        }

        if (req.get("assigned_to") != null && !req.get("assigned_to").toString().isEmpty()) {
            try {
                Long uid = Long.parseLong(req.get("assigned_to").toString());
                task.setAssignedTo(uid);
                userRepository.findById(uid).ifPresent(u -> task.setAssignee(u.getFullName()));
            } catch (Exception ignored) {}
        }
        if (task.getAssignee() == null && req.get("assignee") != null) {
            task.setAssignee(req.get("assignee").toString());
        }

        if (currentUser != null) {
            task.setAssignedById(currentUser.getId());
            task.setAssignedBy(currentUser.getFullName());
        } else {
            task.setAssignedBy("Owner");
        }

        String title = req.get("title") != null ? req.get("title").toString().trim() : "";
        if (title.isEmpty()) {
            if ("ROAST".equalsIgnoreCase(kind)) {
                title = "Roast " + (task.getTarget() != null ? (task.getTarget().intValue() + " batches") : "batches");
            } else if ("PACK".equalsIgnoreCase(kind)) {
                title = "Pack " + (task.getTarget() != null ? (task.getTarget().intValue() + " packs") : "packs");
            } else if ("COUNTER".equalsIgnoreCase(kind)) {
                title = "Handle " + (task.getTarget() != null ? (task.getTarget().intValue() + " counter bills") : "counter bills");
            } else if ("DISPATCH".equalsIgnoreCase(kind)) {
                title = "Dispatch " + (task.getTarget() != null ? (task.getTarget().intValue() + " orders") : "orders");
            } else if ("INWARD".equalsIgnoreCase(kind)) {
                title = "Inward " + (task.getTarget() != null ? (task.getTarget().intValue() + " bags") : "bags");
            } else if ("BLEND".equalsIgnoreCase(kind)) {
                title = "Blend " + (task.getTarget() != null ? (task.getTarget().intValue() + " kg") : "batches");
            } else if ("QC".equalsIgnoreCase(kind)) {
                title = "QC check " + (task.getTarget() != null ? (task.getTarget().intValue() + " lots") : "lots");
            } else if ("COUNT".equalsIgnoreCase(kind)) {
                title = "Cycle count " + (task.getTarget() != null ? (task.getTarget().intValue() + " lots") : "lots");
            } else {
                title = task.getKindLabel() + " work";
            }
        }
        task.setTitle(title);

        Task saved = taskRepository.save(task);
        return formatTask(saved, currentUser);
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Optional<Task> opt = taskRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Task task = opt.get();
        if (req.containsKey("progress") && req.get("progress") != null) {
            try {
                task.setProgress(Double.parseDouble(req.get("progress").toString()));
            } catch (Exception ignored) {}
        }
        if (req.containsKey("status") && req.get("status") != null) {
            String newStatus = req.get("status").toString();
            task.setStatus(newStatus);
        }
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(formatTask(saved, currentUser));
    }

    @PostMapping("/tasks/{id}/submit")
    public ResponseEntity<?> submitTask(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Optional<Task> opt = taskRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Task task = opt.get();
        task.setStatus("SUBMITTED");
        if (req != null && req.containsKey("note") && req.get("note") != null) {
            task.setSubmitNote(req.get("note").toString());
        }
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(formatTask(saved, currentUser));
    }

    @PostMapping("/tasks/{id}/review")
    public ResponseEntity<?> reviewTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Optional<Task> opt = taskRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Task task = opt.get();
        boolean approve = Boolean.TRUE.equals(req.get("approve")) || "true".equalsIgnoreCase(String.valueOf(req.get("approve")));
        task.setStatus(approve ? "DONE" : "IN_PROGRESS");
        if (req.containsKey("note") && req.get("note") != null) {
            task.setReviewNote(req.get("note").toString());
        }
        if (currentUser != null) {
            task.setReviewedById(currentUser.getId());
            task.setReviewedByName(currentUser.getFullName());
        } else {
            task.setReviewedByName("Owner");
        }
        task.setUpdatedAt(LocalDateTime.now());
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(formatTask(saved, currentUser));
    }

    // ---------------------------------------------------- /maintenance
    @GetMapping("/maintenance")
    public List<Map<String, Object>> getMaintenance() {
        List<MaintenanceTicket> all = maintenanceTicketRepository.findAllByOrderByCreatedAtDesc();
        List<Map<String, Object>> res = new ArrayList<>();
        for (MaintenanceTicket t : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("title", t.getTitle());
            m.put("area", t.getArea() != null ? t.getArea() : "ROASTER");
            m.put("urgency", t.getUrgency() != null ? t.getUrgency() : "NORMAL");
            m.put("details", t.getDetails());
            m.put("status", t.getStatus());
            m.put("raised_by_name", t.getRaisedByName() != null ? t.getRaisedByName() : "Staff");
            m.put("created_at", t.getCreatedAt() != null ? t.getCreatedAt().toString() : LocalDateTime.now().toString());
            m.put("assigned_name", t.getAssignedName());
            m.put("resolution", t.getResolution());
            m.put("resolved_by_name", t.getResolvedByName());
            res.add(m);
        }
        return res;
    }

    @PostMapping("/maintenance")
    public Map<String, Object> createMaintenanceTicket(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        String title = req.getOrDefault("title", "Equipment issue").toString();
        String area = req.getOrDefault("area", "ROASTER").toString();
        String urgency = req.getOrDefault("urgency", "NORMAL").toString();
        String details = req.containsKey("details") && req.get("details") != null ? req.get("details").toString() : null;
        Long uid = currentUser != null ? currentUser.getId() : 1L;
        String uname = currentUser != null ? currentUser.getFullName() : "Admin";

        MaintenanceTicket ticket = new MaintenanceTicket(title, area, urgency, details, uid, uname);
        MaintenanceTicket saved = maintenanceTicketRepository.save(ticket);

        Map<String, Object> m = new HashMap<>();
        m.put("id", saved.getId());
        m.put("title", saved.getTitle());
        m.put("area", saved.getArea());
        m.put("urgency", saved.getUrgency());
        m.put("details", saved.getDetails());
        m.put("status", saved.getStatus());
        m.put("raised_by_name", saved.getRaisedByName());
        m.put("created_at", saved.getCreatedAt().toString());
        m.put("assigned_name", saved.getAssignedName());
        m.put("resolution", saved.getResolution());
        m.put("resolved_by_name", saved.getResolvedByName());
        return m;
    }

    @PutMapping("/maintenance/{id}")
    public Map<String, Object> updateMaintenanceTicket(
            @PathVariable long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Optional<MaintenanceTicket> opt = maintenanceTicketRepository.findById(id);
        if (opt.isPresent()) {
            MaintenanceTicket t = opt.get();
            if (req.containsKey("status") && req.get("status") != null) {
                t.setStatus(req.get("status").toString());
            }
            if (req.containsKey("note") && req.get("note") != null && !req.get("note").toString().isEmpty()) {
                t.setResolution(req.get("note").toString());
                t.setResolvedById(currentUser != null ? currentUser.getId() : 1L);
                t.setResolvedByName(currentUser != null ? currentUser.getFullName() : "Admin");
            } else if (("RESOLVED".equalsIgnoreCase(t.getStatus()) || "CLOSED".equalsIgnoreCase(t.getStatus())) && (t.getResolvedByName() == null || t.getResolvedByName().isEmpty())) {
                t.setResolvedById(currentUser != null ? currentUser.getId() : 1L);
                t.setResolvedByName(currentUser != null ? currentUser.getFullName() : "Admin");
            }
            if (req.containsKey("assigned_to") && req.get("assigned_to") != null) {
                try {
                    Long assigneeId = Long.valueOf(req.get("assigned_to").toString());
                    t.setAssignedToId(assigneeId);
                    userRepository.findById(assigneeId).ifPresent(u -> t.setAssignedName(u.getFullName()));
                } catch (Exception ignored) {}
            }
            MaintenanceTicket saved = maintenanceTicketRepository.save(t);
            Map<String, Object> m = new HashMap<>();
            m.put("id", saved.getId());
            m.put("title", saved.getTitle());
            m.put("area", saved.getArea());
            m.put("urgency", saved.getUrgency());
            m.put("details", saved.getDetails());
            m.put("status", saved.getStatus());
            m.put("raised_by_name", saved.getRaisedByName());
            m.put("created_at", saved.getCreatedAt().toString());
            m.put("assigned_name", saved.getAssignedName());
            m.put("resolution", saved.getResolution());
            m.put("resolved_by_name", saved.getResolvedByName());
            return m;
        }
        return req;
    }

    // ---------------------------------------------------- /inward
    @GetMapping("/inward")
    public List<InwardReceipt> getInward() {
        return inwardReceiptRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/inward")
    public Map<String, Object> recordInward(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        String source = req.getOrDefault("source", "AUCTION").toString();
        String sourceName = req.getOrDefault("source_name", "Supplier").toString();

        Long productId = null;
        if (req.containsKey("product_id") && req.get("product_id") != null) {
            try {
                productId = Long.valueOf(req.get("product_id").toString());
            } catch (Exception ignored) {}
        }

        String productName = "Coffee Beans";
        double perBag = 60.0;
        if (productId != null) {
            Optional<MasterRecord> prodOpt = masterRepository.findById(productId);
            if (prodOpt.isPresent()) {
                MasterRecord p = prodOpt.get();
                productName = p.getName();
                if (p.getNominalKg() != null && p.getNominalKg() > 0) {
                    perBag = p.getNominalKg();
                }
            }
        }

        int bags = req.containsKey("bags") ? Integer.parseInt(req.get("bags").toString()) : 1;
        int damaged = req.containsKey("damaged") ? Integer.parseInt(req.get("damaged").toString()) : 0;
        int partial = req.containsKey("partial") ? Integer.parseInt(req.get("partial").toString()) : 0;
        Double partialKg = req.containsKey("partial_kg") && req.get("partial_kg") != null 
                ? Double.parseDouble(req.get("partial_kg").toString()) : null;

        int soundBags = Math.max(0, bags - damaged - partial);
        double soundKg = soundBags * perBag;
        double damagedKg = damaged * perBag;
        double pKg = (partialKg != null ? partialKg : 0.0);
        double totalNominalKg = (soundBags * perBag) + (damaged * perBag) + pKg;
        double allocatableKg = soundKg + pKg;
        double quarantineKg = damagedKg;

        long count = inwardReceiptRepository.count() + 1;
        String code = "IN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + String.format("%02d", count);

        InwardReceipt receipt = new InwardReceipt();
        receipt.setCode(code);
        receipt.setSource(source);
        receipt.setSourceName(sourceName);
        receipt.setProductId(productId);
        receipt.setProduct(productName);
        receipt.setBags(bags);
        receipt.setDamaged(damaged);
        receipt.setPartial(partial);
        receipt.setPartialKg(partial > 0 ? pKg : null);
        receipt.setNominalKg(totalNominalKg);
        receipt.setAllocatableKg(allocatableKg);
        receipt.setQuarantineKg(quarantineKg);
        receipt.setInvoiceStatus("MATCHED");
        receipt.setPaymentStatus("PAID");
        receipt.setCreatedBy(currentUser != null ? currentUser.getFullName() : "Admin");
        receipt.setCreatedAt(LocalDateTime.now());

        InwardReceipt savedReceipt = inwardReceiptRepository.save(receipt);

        List<InventoryLot> createdLots = new ArrayList<>();
        if (soundBags > 0) {
            InventoryLot lotSound = new InventoryLot();
            lotSound.setCode("LOT-" + savedReceipt.getCode() + "-S");
            lotSound.setReceiptId(savedReceipt.getId());
            lotSound.setReceiptCode(savedReceipt.getCode());
            lotSound.setProductId(productId);
            lotSound.setProduct(productName);
            lotSound.setName(productName);
            lotSound.setStage("RAW");
            lotSound.setLocation("PROD-01");
            lotSound.setOwner("VTC");
            lotSound.setStatus("RELEASED");
            lotSound.setBagKind("SOUND");
            lotSound.setBags(soundBags);
            lotSound.setKg(soundKg);
            lotSound.setAvailableKg(soundKg);
            lotSound.setNominalKg(perBag);
            lotSound.setCreatedAt(LocalDateTime.now());
            createdLots.add(inventoryLotRepository.save(lotSound));
        }

        if (damaged > 0) {
            InventoryLot lotDamaged = new InventoryLot();
            lotDamaged.setCode("LOT-" + savedReceipt.getCode() + "-D");
            lotDamaged.setReceiptId(savedReceipt.getId());
            lotDamaged.setReceiptCode(savedReceipt.getCode());
            lotDamaged.setProductId(productId);
            lotDamaged.setProduct(productName);
            lotDamaged.setName(productName);
            lotDamaged.setStage("RAW");
            lotDamaged.setLocation("PROD-01");
            lotDamaged.setOwner("VTC");
            lotDamaged.setStatus("QUARANTINE");
            lotDamaged.setBagKind("DAMAGED");
            lotDamaged.setBags(damaged);
            lotDamaged.setKg(damagedKg);
            lotDamaged.setAvailableKg(0.0);
            lotDamaged.setNominalKg(perBag);
            lotDamaged.setCreatedAt(LocalDateTime.now());
            createdLots.add(inventoryLotRepository.save(lotDamaged));
        }

        if (partial > 0) {
            InventoryLot lotPartial = new InventoryLot();
            lotPartial.setCode("LOT-" + savedReceipt.getCode() + "-P");
            lotPartial.setReceiptId(savedReceipt.getId());
            lotPartial.setReceiptCode(savedReceipt.getCode());
            lotPartial.setProductId(productId);
            lotPartial.setProduct(productName);
            lotPartial.setName(productName);
            lotPartial.setStage("RAW");
            lotPartial.setLocation("PROD-01");
            lotPartial.setOwner("VTC");
            lotPartial.setStatus("RELEASED");
            lotPartial.setBagKind("PARTIAL");
            lotPartial.setBags(partial);
            lotPartial.setKg(pKg);
            lotPartial.setAvailableKg(pKg);
            lotPartial.setNominalKg(perBag);
            lotPartial.setCreatedAt(LocalDateTime.now());
            createdLots.add(inventoryLotRepository.save(lotPartial));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("id", savedReceipt.getId());
        res.put("code", savedReceipt.getCode());
        res.put("source", savedReceipt.getSource());
        res.put("source_name", savedReceipt.getSourceName());
        res.put("product_id", savedReceipt.getProductId());
        res.put("product", savedReceipt.getProduct());
        res.put("bags", savedReceipt.getBags());
        res.put("damaged", savedReceipt.getDamaged());
        res.put("partial", savedReceipt.getPartial());
        res.put("partial_kg", savedReceipt.getPartialKg());
        res.put("nominal_kg", savedReceipt.getNominalKg());
        res.put("allocatable_kg", savedReceipt.getAllocatableKg());
        res.put("quarantine_kg", savedReceipt.getQuarantineKg());
        res.put("invoice_status", savedReceipt.getInvoiceStatus());
        res.put("payment_status", savedReceipt.getPaymentStatus());
        res.put("created_by", savedReceipt.getCreatedBy());
        res.put("created_at", savedReceipt.getCreatedAt().toString());
        res.put("lots", createdLots);
        return res;
    }

    @PostMapping("/inward/{id}/reverse")
    public ResponseEntity<?> reverseInward(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        Optional<InwardReceipt> opt = inwardReceiptRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        InwardReceipt r = opt.get();
        if (r.getReversedAt() != null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "ALREADY_REVERSED"));
        }
        r.setReversedAt(LocalDateTime.now());
        r.setReverseReason(req.getOrDefault("reason", "Reversed by user").toString());
        inwardReceiptRepository.save(r);

        List<InventoryLot> lots = inventoryLotRepository.findByReceiptId(r.getId());
        for (InventoryLot lot : lots) {
            lot.setStatus("REJECTED");
            lot.setAvailableKg(0.0);
            inventoryLotRepository.save(lot);
        }
        return ResponseEntity.ok(r);
    }

    // ---------------------------------------------------- /inventory
    @GetMapping("/inventory")
    public List<InventoryLot> getInventory() {
        return inventoryLotRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/lots/{id}/verify")
    public ResponseEntity<?> verifyQuarantinedLot(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        Optional<InventoryLot> opt = inventoryLotRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        InventoryLot lot = opt.get();
        double verifiedKg = req.containsKey("verified_kg") ? Double.parseDouble(req.get("verified_kg").toString()) : lot.getKg();
        lot.setStatus("RELEASED");
        lot.setBagKind("SOUND");
        lot.setKg(verifiedKg);
        lot.setAvailableKg(verifiedKg);
        lot.setUpdatedAt(LocalDateTime.now());
        inventoryLotRepository.save(lot);
        return ResponseEntity.ok(lot);
    }

    @GetMapping("/transfers")
    public List<Map<String, Object>> getTransfers() {
        return transfersList;
    }

    @PostMapping("/transfers")
    public Map<String, Object> createTransfer(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> transfer = new HashMap<>(req);
        long id = transfersList.size() + 1;
        transfer.put("id", id);
        transfer.put("transit_code", "TRF-" + String.format("%04d", id));
        transfer.put("state", "IN_TRANSIT");
        transfer.put("sent_by_name", currentUser != null ? currentUser.getFullName() : "Staff");
        transfer.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        transfersList.add(transfer);
        return transfer;
    }

    @GetMapping("/stock-cards")
    public List<Map<String, Object>> getStockCards() {
        List<Map<String, Object>> cards = new ArrayList<>();
        List<MasterRecord> products = masterRepository.findByKind("PRODUCT");
        for (MasterRecord p : products) {
            Map<String, Object> card = new HashMap<>();
            card.put("id", p.getId());
            card.put("product_id", p.getId());
            card.put("name", p.getName());
            card.put("code", p.getCode());
            card.put("min_kg", 50.0);
            card.put("max_kg", 1000.0);
            card.put("raw_kg", 250.0);
            card.put("roasted_kg", 80.0);
            cards.add(card);
        }
        return cards;
    }

    @GetMapping("/branches")
    public List<Map<String, Object>> getBranches() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> b1 = new HashMap<>();
        b1.put("code", "PROD-01");
        b1.put("name", "Roastery & Main Store");
        list.add(b1);
        Map<String, Object> b2 = new HashMap<>();
        b2.put("code", "COUNTER-01");
        b2.put("name", "Retail Tasting Counter");
        list.add(b2);
        return list;
    }

    @GetMapping("/counts")
    public List<Map<String, Object>> getCounts() {
        return Collections.emptyList();
    }

    // ---------------------------------------------------- /qc
    @GetMapping("/qc")
    public List<Map<String, Object>> getQcList() {
        return Collections.emptyList();
    }

    @GetMapping("/qc/config")
    public Map<String, Object> getQcConfig() {
        return Collections.emptyMap();
    }

    // ---------------------------------------------------- /outside/jobs
    @GetMapping("/outside/jobs")
    public Map<String, Object> getOutsideJobs() {
        Map<String, Object> res = new HashMap<>();
        res.put("jobs", outsideJobsList);

        List<Map<String, Object>> customers = new ArrayList<>();
        List<MasterRecord> custMasters = masterRepository.findByKind("CUSTOMER");
        for (MasterRecord m : custMasters) {
            Map<String, Object> c = new HashMap<>();
            c.put("id", m.getId());
            c.put("name", m.getName());
            customers.add(c);
        }
        res.put("customers", customers);
        return res;
    }

    @PostMapping("/outside/jobs")
    public Map<String, Object> createOutsideJob(@RequestBody Map<String, Object> req) {
        Map<String, Object> job = new HashMap<>(req);
        long id = outsideJobsList.size() + 1;
        job.put("id", id);
        job.put("state", "RECEIVED");
        job.put("received_kg", req.getOrDefault("received_kg", 50.0));
        job.put("raw_left_kg", req.getOrDefault("received_kg", 50.0));
        job.put("roasted_left_kg", 0.0);
        job.put("returned_kg", 0.0);
        job.put("payment_status", "PENDING");
        outsideJobsList.add(job);
        return job;
    }

    @PostMapping("/outside/jobs/{id}/roast")
    public Map<String, Object> roastOutsideJob(@PathVariable long id, @RequestBody Map<String, Object> req) {
        for (Map<String, Object> j : outsideJobsList) {
            if (Objects.equals(j.get("id"), id)) {
                j.put("state", "ROASTED");
                j.put("roasted_left_kg", req.getOrDefault("output_kg", 42.0));
                j.put("raw_left_kg", 0.0);
                return j;
            }
        }
        return req;
    }

    @PostMapping("/outside/jobs/{id}/return")
    public Map<String, Object> returnOutsideJob(@PathVariable long id, @RequestBody Map<String, Object> req) {
        for (Map<String, Object> j : outsideJobsList) {
            if (Objects.equals(j.get("id"), id)) {
                j.put("state", "RETURNED");
                return j;
            }
        }
        return req;
    }

    @PostMapping("/outside/jobs/{id}/payment")
    public Map<String, Object> payOutsideJob(@PathVariable long id, @RequestBody Map<String, Object> req) {
        for (Map<String, Object> j : outsideJobsList) {
            if (Objects.equals(j.get("id"), id)) {
                j.put("payment_status", "PAID");
                return j;
            }
        }
        return req;
    }

    // ---------------------------------------------------- /packing
    @GetMapping("/packing")
    public Map<String, Object> getPacking() {
        Map<String, Object> res = new HashMap<>();
        res.put("jobs", packingJobsList);

        res.put("sources", inventoryLotRepository.findAllByOrderByCreatedAtDesc());
        return res;
    }

    @PostMapping("/packing")
    public Map<String, Object> createPackingTicket(@RequestBody Map<String, Object> req) {
        Map<String, Object> job = new HashMap<>(req);
        long id = packingJobsList.size() + 1;
        job.put("id", id);
        job.put("packs_done", 0);
        job.put("packs_target", req.getOrDefault("packs_target", 100));
        job.put("pack_g", req.getOrDefault("pack_g", 250));
        job.put("state", "OPEN");
        job.put("finished_status", "QC_HOLD");
        packingJobsList.add(job);
        return job;
    }

    @PostMapping("/packing/{id}/run")
    public Map<String, Object> runPackingTicket(@PathVariable long id, @RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>(req);
        res.put("packs_done", req.getOrDefault("good_packs", 50));
        res.put("loss_kg", 0.2);
        res.put("code", "PACK-LOT-" + id);
        res.put("lot_id", id);
        res.put("packs", req.getOrDefault("good_packs", 50));

        for (Map<String, Object> j : packingJobsList) {
            if (Objects.equals(j.get("id"), id)) {
                j.put("packs_done", req.getOrDefault("good_packs", 50));
                j.put("state", "COMPLETE");
                break;
            }
        }
        return res;
    }

    @PostMapping("/packing/{id}/release")
    public Map<String, Object> releasePackingTicket(@PathVariable long id, @RequestBody Map<String, Object> req) {
        for (Map<String, Object> j : packingJobsList) {
            if (Objects.equals(j.get("id"), id)) {
                j.put("finished_status", "RELEASED");
                return j;
            }
        }
        return req;
    }

    // ---------------------------------------------------- /orders
    @GetMapping("/orders")
    public List<Map<String, Object>> getOrders() {
        return ordersList;
    }

    @PostMapping("/orders")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> req) {
        Map<String, Object> order = new HashMap<>(req);
        long id = ordersList.size() + 1;
        order.put("id", id);
        order.put("state", "RESERVED");
        order.put("cleared", true);
        order.put("clearance_note", "Credit cleared");
        order.put("dispatched_kg", 0.0);
        order.put("dispatches", Collections.emptyList());
        ordersList.add(order);
        return order;
    }

    // ---------------------------------------------------- /exceptions
    @GetMapping("/exceptions")
    public List<Map<String, Object>> getExceptions() {
        List<Map<String, Object>> res = new ArrayList<>();

        // 1. Open Maintenance tickets appear here!
        List<MaintenanceTicket> openTickets = maintenanceTicketRepository.findByStatusInOrderByCreatedAtDesc(Arrays.asList("OPEN", "IN_PROGRESS"));
        for (MaintenanceTicket t : openTickets) {
            Map<String, Object> exc = new HashMap<>();
            exc.put("id", "maint-" + t.getId());
            exc.put("severity", "URGENT".equalsIgnoreCase(t.getUrgency()) ? "critical" : "high");
            exc.put("view", "maintenance");
            exc.put("title", "Maintenance: " + t.getTitle());
            String detail = (t.getArea() != null ? t.getArea() : "Machine")
                    + (t.getDetails() != null && !t.getDetails().isEmpty() ? " · " + t.getDetails() : "")
                    + " · Raised by " + (t.getRaisedByName() != null ? t.getRaisedByName() : "Staff");
            exc.put("detail", detail);
            exc.put("category", "MAINTENANCE");
            exc.put("opened_at", t.getCreatedAt() != null ? t.getCreatedAt().toString() : LocalDateTime.now().toString());
            res.add(exc);
        }

        // 2. Operational exceptions from ledger
        Map<String, Object> e1 = new HashMap<>();
        e1.put("id", "sys-1");
        e1.put("severity", "high");
        e1.put("view", "inward");
        e1.put("title", "Damaged bags to verify — lot #2");
        e1.put("detail", "Robusta Cherry AB (sample) · IN-261027-01 · Damaged bags");
        e1.put("category", "receipt_variance");
        e1.put("opened_at", "2026-10-27T17:36:00");
        res.add(e1);

        Map<String, Object> e2 = new HashMap<>();
        e2.put("id", "sys-2");
        e2.put("severity", "high");
        e2.put("view", "qc");
        e2.put("title", "Roast #3 waiting for quality decision");
        e2.put("detail", "30 → 24.1 kg");
        e2.put("category", "qc_hold");
        e2.put("opened_at", "2026-10-27T17:36:00");
        res.add(e2);

        Map<String, Object> e3 = new HashMap<>();
        e3.put("id", "sys-3");
        e3.put("severity", "high");
        e3.put("view", "counter");
        e3.put("title", "Transfer #3 arrived short by 0.2 kg");
        e3.put("detail", "transfer shortage");
        e3.put("category", "transfer_shortage");
        e3.put("opened_at", "2026-10-27T17:36:00");
        res.add(e3);

        Map<String, Object> e4 = new HashMap<>();
        e4.put("id", "sys-4");
        e4.put("severity", "high");
        e4.put("view", "orders");
        e4.put("title", "Order #2 (Sample Dealer, Secunderabad) on credit hold");
        e4.put("detail", "No approved credit terms for this customer");
        e4.put("category", "credit_hold");
        e4.put("opened_at", "2026-10-27T17:36:00");
        res.add(e4);

        Map<String, Object> e5 = new HashMap<>();
        e5.put("id", "sys-5");
        e5.put("severity", "normal");
        e5.put("view", "finance");
        e5.put("title", "Invoice not matched within 3 days — receipt #1 (O-34)");
        e5.put("detail", "Sample Coffee Auction House");
        e5.put("category", "invoice_difference");
        e5.put("opened_at", "2026-10-27T17:36:00");
        res.add(e5);

        return res;
    }

    // ---------------------------------------------------- /recipes, /blends, /grinds, /roast-profiles
    @GetMapping("/recipes")
    public List<Map<String, Object>> getRecipes() {
        return blendRecipes;
    }

    @GetMapping("/grinds")
    public List<Map<String, Object>> getGrinds() {
        return grindJobs;
    }

    @GetMapping("/roast-profiles")
    public List<Map<String, Object>> getRoastProfiles() {
        return roastProfiles;
    }

    // ---------------------------------------------------- /counter
    @GetMapping("/counter/cash-close")
    public List<Map<String, Object>> getCounterCashClose() {
        return cashCloseList;
    }

    // ---------------------------------------------------- /reports
    @GetMapping("/reports")
    public Map<String, Object> getReports(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        Map<String, Object> res = new HashMap<>();

        long roasts = roastRepository.count();
        Map<String, Object> ro = new HashMap<>();
        ro.put("batches", roasts);
        ro.put("input_kg", roasts * 30.0);
        ro.put("output_kg", roasts * 25.0);
        ro.put("loss_pct", roasts > 0 ? 16.7 : null);
        ro.put("on_hold", 0);
        res.put("roasting", ro);

        Map<String, Object> rc = new HashMap<>();
        long totalActiveBags = 0;
        try { totalActiveBags = inwardReceiptRepository.sumTotalActiveBags(); } catch (Exception ignored) {}
        rc.put("bags", totalActiveBags);
        rc.put("damaged", 0);
        rc.put("kg", totalActiveBags * 60.0);
        res.put("receipts", rc);

        Map<String, Object> pk = new HashMap<>();
        pk.put("kg", 0.0);
        pk.put("runs", packingJobsList.size());
        res.put("packing", pk);

        List<Map<String, Object>> stock = new ArrayList<>();
        stock.add(createStockItem("Arabica Plantation A (sample)", "RAW", "PROD-01", 240.0));
        stock.add(createStockItem("Arabica Plantation A (sample)", "ROASTED", "PROD-01", 0.0));
        stock.add(createStockItem("Robusta Cherry AB (sample)", "RAW", "PROD-01", 180.0));
        stock.add(createStockItem("Robusta Cherry AB (sample)", "ROASTED", "PROD-01", 0.0));
        stock.add(createStockItem("Chicory Premium", "RAW", "PROD-01", 50.0));
        res.put("stock", stock);

        Map<String, Object> ct = new HashMap<>();
        List<CounterBill> bills = counterRepository.findAll();
        long totalPaise = bills.stream().mapToLong(CounterBill::getTotalPaise).sum();
        long discountPaise = bills.stream().mapToLong(b -> b.getDiscountPaise() != null ? b.getDiscountPaise() : 0L).sum();
        ct.put("total_paise", totalPaise);
        ct.put("bills", bills.size());
        ct.put("discount_paise", discountPaise);
        res.put("counter", ct);

        List<Map<String, Object>> cm = new ArrayList<>();
        Map<String, Object> mCash = new HashMap<>();
        mCash.put("method", "CASH");
        mCash.put("total_paise", bills.stream().filter(b -> "CASH".equalsIgnoreCase(b.getPaymentMode())).mapToLong(CounterBill::getTotalPaise).sum());
        cm.add(mCash);
        Map<String, Object> mUpi = new HashMap<>();
        mUpi.put("method", "UPI");
        mUpi.put("total_paise", bills.stream().filter(b -> "UPI".equalsIgnoreCase(b.getPaymentMode())).mapToLong(CounterBill::getTotalPaise).sum());
        cm.add(mUpi);
        res.put("counter_methods", cm);

        Map<String, Object> invoiced = new HashMap<>();
        invoiced.put("total_paise", 546000L);
        invoiced.put("invoices", 1);
        res.put("invoiced", invoiced);

        Map<String, Object> outside = new HashMap<>();
        outside.put("charge_paise", 0L);
        outside.put("jobs", outsideJobsList.size());
        res.put("outside", outside);

        res.put("margin_note", "Gross margins will calculate once all batch costs are recorded.");
        return res;
    }

    // ---------------------------------------------------- /receivables
    @GetMapping("/receivables")
    public Map<String, Object> getReceivables() {
        Map<String, Object> res = new HashMap<>();
        Map<String, Object> aging = new HashMap<>();
        aging.put("current", 346000L);
        aging.put("1-30", 0L);
        aging.put("31-60", 0L);
        aging.put("60+", 0L);
        res.put("aging", aging);

        res.put("pending", receivablesPendingList);
        res.put("invoices", receivablesInvoicesList);
        return res;
    }

    @GetMapping("/invoices/{id}")
    public Map<String, Object> getInvoice(@PathVariable long id) {
        for (Map<String, Object> inv : receivablesInvoicesList) {
            if (Objects.equals(inv.get("id"), id) || String.valueOf(id).equals(String.valueOf(inv.get("id")))) {
                Map<String, Object> res = new HashMap<>(inv);
                res.put("created_at", "2026-10-27T10:00:00");
                res.put("item", "Special Filter Blend");
                res.put("form", "POWDER");
                res.put("price_per_kg_paise", 52000L);
                res.put("taxable_paise", 520000L);
                res.put("gst_rate_bp", 500);
                res.put("gst_paise", 26000L);
                Map<String, Object> header = new HashMap<>();
                header.put("company_name", "Vasavi Trading Company");
                header.put("company_address", "8-2-120/86, Banjara Hills, Hyderabad - 500034");
                header.put("company_gstin", "36AAACV1234F1Z5");
                header.put("tax_setup_approved", true);
                res.put("header", header);
                return res;
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("id", id);
        res.put("number", "INV-2610-000" + id);
        res.put("customer", "Sample Customer");
        res.put("created_at", "2026-10-27T10:00:00");
        res.put("item", "Special Filter Blend");
        res.put("form", "POWDER");
        res.put("price_per_kg_paise", 52000L);
        res.put("taxable_paise", 520000L);
        res.put("gst_rate_bp", 500);
        res.put("gst_paise", 26000L);
        res.put("total_paise", 546000L);
        res.put("due_date", "2026-11-11");
        res.put("status", "PART_PAID");
        Map<String, Object> header = new HashMap<>();
        header.put("company_name", "Vasavi Trading Company");
        header.put("company_address", "8-2-120/86, Banjara Hills, Hyderabad - 500034");
        header.put("company_gstin", "36AAACV1234F1Z5");
        header.put("tax_setup_approved", true);
        res.put("header", header);
        return res;
    }

    @PostMapping("/invoices/{id}/collections")
    public Map<String, Object> recordInvoiceCollection(
            @PathVariable long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> col = new HashMap<>();
        long colId = receivablesPendingList.size() + 1;
        col.put("id", colId);
        col.put("invoice_id", id);
        col.put("number", "INV-2610-000" + id);
        col.put("amount_paise", req.get("amount_paise"));
        col.put("method", req.getOrDefault("method", "UPI"));
        col.put("reference", req.getOrDefault("reference", ""));
        col.put("recorded_by", currentUser != null ? currentUser.getId() : 1L);
        col.put("recorded_by_name", currentUser != null ? currentUser.getFullName() : "Cashier");
        receivablesPendingList.add(col);
        return col;
    }

    @PostMapping("/collections/{id}/verify")
    public Map<String, Object> verifyCollection(
            @PathVariable long id,
            @RequestBody Map<String, Object> req) {
        boolean approve = Boolean.TRUE.equals(req.get("approve"));
        for (Map<String, Object> c : receivablesPendingList) {
            if (Objects.equals(c.get("id"), id)) {
                receivablesPendingList.remove(c);
                if (approve) {
                    long invId = Long.valueOf(c.get("invoice_id").toString());
                    long amount = Long.valueOf(c.get("amount_paise").toString());
                    for (Map<String, Object> inv : receivablesInvoicesList) {
                        if (Objects.equals(inv.get("id"), invId)) {
                            long paid = Long.valueOf(inv.getOrDefault("paid_paise", 0L).toString()) + amount;
                            long total = Long.valueOf(inv.getOrDefault("total_paise", 0L).toString());
                            long due = Math.max(0, total - paid);
                            inv.put("paid_paise", paid);
                            inv.put("due_paise", due);
                            if (due <= 0) inv.put("status", "PAID");
                            break;
                        }
                    }
                }
                break;
            }
        }
        return Collections.singletonMap("status", approve ? "VERIFIED" : "REJECTED");
    }

    // ---------------------------------------------------- /payables
    @GetMapping("/payables")
    public Map<String, Object> getPayables() {
        Map<String, Object> res = new HashMap<>();
        res.put("receipts", payablesReceiptsList);
        res.put("payments", payablesPaymentsList);
        return res;
    }

    @PutMapping("/inward/{id}/finance")
    public Map<String, Object> updateInwardFinance(
            @PathVariable long id,
            @RequestBody Map<String, Object> req) {
        for (Map<String, Object> r : payablesReceiptsList) {
            if (Objects.equals(r.get("id"), id)) {
                r.putAll(req);
                return r;
            }
        }
        return req;
    }

    @PostMapping("/receipts/{id}/payments")
    public Map<String, Object> recordSupplierPayment(
            @PathVariable long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> p = new HashMap<>(req);
        long pId = payablesPaymentsList.size() + 1;
        p.put("id", pId);
        p.put("receipt_id", id);
        p.put("state", "PENDING_APPROVAL");
        p.put("recorded_by", currentUser != null ? currentUser.getId() : 1L);
        p.put("recorded_by_name", currentUser != null ? currentUser.getFullName() : "Admin");
        payablesPaymentsList.add(p);
        return p;
    }

    @PostMapping("/supplier-payments/{id}/decide")
    public Map<String, Object> decideSupplierPayment(
            @PathVariable long id,
            @RequestBody Map<String, Object> req) {
        boolean approve = Boolean.TRUE.equals(req.get("approve"));
        for (Map<String, Object> p : payablesPaymentsList) {
            if (Objects.equals(p.get("id"), id)) {
                p.put("state", approve ? "APPROVED" : "REJECTED");
                return p;
            }
        }
        return Collections.singletonMap("state", approve ? "APPROVED" : "REJECTED");
    }

    // ---------------------------------------------------- /tally
    @GetMapping("/tally/exports")
    public List<Map<String, Object>> getTallyExports() {
        return tallyExportsList;
    }

    @PostMapping("/tally/exports")
    public Map<String, Object> createTallyExport(@RequestBody Map<String, Object> req) {
        Map<String, Object> e = new HashMap<>();
        long id = tallyExportsList.size() + 1;
        e.put("id", id);
        e.put("batch_no", "TALLY-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-0" + id);
        e.put("created_at", LocalDateTime.now().toString());
        e.put("period_from", req.getOrDefault("from", "2026-11-01"));
        e.put("period_to", req.getOrDefault("to", "2026-11-08"));
        e.put("voucher_count", 5);
        e.put("vouchers", 5);
        e.put("control_total_paise", 2500000L);
        e.put("checksum", UUID.randomUUID().toString().replace("-", ""));
        e.put("state", "EXPORTED");
        e.put("content", "Date,VoucherType,VoucherNo,Party,Debit,Credit\n2026-11-08,Sales,INV-2610-0001,Sample Café Banjara Hills,5460,0\n");
        tallyExportsList.add(e);
        return e;
    }

    @GetMapping("/tally/exports/{id}")
    public Map<String, Object> getTallyExport(@PathVariable long id) {
        for (Map<String, Object> e : tallyExportsList) {
            if (Objects.equals(e.get("id"), id)) return e;
        }
        return tallyExportsList.isEmpty() ? Collections.emptyMap() : tallyExportsList.get(0);
    }

    @PostMapping("/tally/exports/{id}/result")
    public Map<String, Object> recordTallyResult(
            @PathVariable long id,
            @RequestBody Map<String, Object> req) {
        boolean accepted = Boolean.TRUE.equals(req.get("accepted"));
        String note = req.getOrDefault("note", "").toString();
        for (Map<String, Object> e : tallyExportsList) {
            if (Objects.equals(e.get("id"), id)) {
                e.put("state", accepted ? "ACCEPTED" : "REJECTED");
                e.put("result_note", note);
                return e;
            }
        }
        return Collections.singletonMap("state", accepted ? "ACCEPTED" : "REJECTED");
    }

    // ---------------------------------------------------- /opening-dues
    @GetMapping("/opening-dues")
    public Map<String, Object> getOpeningDues() {
        Map<String, Object> res = new HashMap<>();
        res.put("pending", openingDuesPendingList);
        res.put("dues", openingDuesList);
        return res;
    }

    @PostMapping("/opening-dues/{id}/payments")
    public Map<String, Object> recordOpeningDuePayment(
            @PathVariable long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Map<String, Object> p = new HashMap<>();
        long pId = openingDuesPendingList.size() + 1;
        p.put("id", pId);
        p.put("due_id", id);
        p.put("side", "CLIENT");
        for (Map<String, Object> d : openingDuesList) {
            if (Objects.equals(d.get("id"), id)) {
                p.put("side", d.get("side"));
                break;
            }
        }
        p.put("amount_paise", req.get("amount_paise"));
        p.put("method", req.getOrDefault("method", "UPI"));
        p.put("reference", req.getOrDefault("reference", ""));
        p.put("recorded_by", currentUser != null ? currentUser.getId() : 1L);
        p.put("recorded_by_name", currentUser != null ? currentUser.getFullName() : "Admin");
        openingDuesPendingList.add(p);
        return p;
    }

    @PostMapping("/opening-due-payments/{id}/verify")
    public Map<String, Object> verifyOpeningDuePayment(
            @PathVariable long id,
            @RequestBody Map<String, Object> req) {
        boolean approve = Boolean.TRUE.equals(req.get("approve"));
        for (Map<String, Object> p : openingDuesPendingList) {
            if (Objects.equals(p.get("id"), id)) {
                openingDuesPendingList.remove(p);
                if (approve) {
                    long dueId = Long.valueOf(p.get("due_id").toString());
                    long amount = Long.valueOf(p.get("amount_paise").toString());
                    for (Map<String, Object> d : openingDuesList) {
                        if (Objects.equals(d.get("id"), dueId)) {
                            long paid = Long.valueOf(d.getOrDefault("paid_paise", 0L).toString()) + amount;
                            long orig = Long.valueOf(d.getOrDefault("amount_paise", 0L).toString());
                            long due = Math.max(0, orig - paid);
                            d.put("paid_paise", paid);
                            d.put("due_paise", due);
                            break;
                        }
                    }
                }
                break;
            }
        }
        return Collections.singletonMap("status", approve ? "VERIFIED" : "REJECTED");
    }

    // ---------------------------------------------------- /me/profile
    @GetMapping("/me/profile")
    public Map<String, Object> getMyProfile(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> res = new HashMap<>();

        Map<String, Object> contact = new HashMap<>();
        contact.put("phone", currentUser != null && currentUser.getPhone() != null ? currentUser.getPhone() : "");
        contact.put("alt_phone", "");
        contact.put("email", currentUser != null && currentUser.getEmail() != null ? currentUser.getEmail() : "");
        contact.put("emergency_name", "Primary Emergency Contact");
        contact.put("emergency_phone", "9849000000");
        res.put("contact", contact);

        Map<String, Object> sess = new HashMap<>();
        sess.put("id", 1);
        sess.put("device", "Desktop Chrome (Current Session)");
        sess.put("this", true);
        sess.put("device_mode", "personal");
        sess.put("last_seen", System.currentTimeMillis());
        sess.put("created_at", System.currentTimeMillis());
        res.put("sessions", Collections.singletonList(sess));

        res.put("teams", Arrays.asList("Management", "Production"));
        res.put("joined_on", "2026-01-01");
        res.put("designation", currentUser != null ? currentUser.getRole() : "Staff");

        Map<String, Object> boss = new HashMap<>();
        boss.put("full_name", currentUser != null && "owner".equalsIgnoreCase(currentUser.getUsername()) ? "Self (Owner)" : "Owner");
        res.put("boss", boss);

        return res;
    }
}
