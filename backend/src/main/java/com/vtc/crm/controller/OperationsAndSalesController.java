package com.vtc.crm.controller;

import com.vtc.crm.entity.CounterBill;
import com.vtc.crm.entity.InventoryLot;
import com.vtc.crm.entity.RoastBatch;
import com.vtc.crm.entity.Task;
import com.vtc.crm.entity.User;
import com.vtc.crm.repository.CounterBillRepository;
import com.vtc.crm.repository.InventoryLotRepository;
import com.vtc.crm.repository.RoastBatchRepository;
import com.vtc.crm.repository.TaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class OperationsAndSalesController {

    private final RoastBatchRepository roastRepository;
    private final CounterBillRepository counterRepository;
    private final InventoryLotRepository inventoryLotRepository;
    private final TaskRepository taskRepository;

    public OperationsAndSalesController(
            RoastBatchRepository roastRepository,
            CounterBillRepository counterRepository,
            InventoryLotRepository inventoryLotRepository,
            TaskRepository taskRepository) {
        this.roastRepository = roastRepository;
        this.counterRepository = counterRepository;
        this.inventoryLotRepository = inventoryLotRepository;
        this.taskRepository = taskRepository;
    }

    // Daily Roasting Batches
    @GetMapping("/roasts")
    public Object getRoasts(
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String month) {
        if (month != null && !month.trim().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            Map<String, Object> days = new HashMap<>();
            try {
                LocalDateTime start = LocalDate.parse(month + "-01").atStartOfDay();
                LocalDateTime end = start.plusMonths(1);
                List<RoastBatch> all = roastRepository.findBetweenDates(start, end);
                for (RoastBatch b : all) {
                    if (b.getCreatedAt() != null) {
                        String d = b.getCreatedAt().toString().substring(0, 10);
                        Map<String, Object> dayInfo = (Map<String, Object>) days.computeIfAbsent(d, k -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("batches", 0);
                            m.put("output_kg", 0.0);
                            return m;
                        });
                        dayInfo.put("batches", (int) dayInfo.get("batches") + 1);
                        dayInfo.put("output_kg", (double) dayInfo.get("output_kg") + (b.getRoastedKg() != null ? b.getRoastedKg() : 0.0));
                    }
                }
            } catch (Exception ignored) {}
            res.put("days", days);
            return res;
        }

        List<RoastBatch> list;
        if (day != null && !day.trim().isEmpty()) {
            try {
                LocalDate d = LocalDate.parse(day);
                LocalDateTime start = d.atStartOfDay();
                LocalDateTime end = start.plusDays(1);
                list = roastRepository.findBetweenDates(start, end);
            } catch (Exception e) {
                list = roastRepository.findTop30ByOrderByCreatedAtDesc();
            }
        } else {
            list = roastRepository.findTop30ByOrderByCreatedAtDesc();
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (RoastBatch b : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("code", b.getBatchCode());
            m.put("roaster", b.getRoasterName() != null ? b.getRoasterName() : "Ramesh (Roaster)");
            m.put("inputs", b.getInputsSummary() != null ? b.getInputsSummary() : "Green coffee beans");
            m.put("input_kg", b.getGreenKg());
            m.put("output_kg", b.getRoastedKg());
            m.put("loss_kg", b.getGreenKg() != null && b.getRoastedKg() != null ? b.getGreenKg() - b.getRoastedKg() : 0.0);
            m.put("duration_min", b.getDurationMin() != null ? b.getDurationMin() : 12);
            m.put("profile", b.getProfileName() != null ? b.getProfileName() : "Medium Roast");
            m.put("qc_status", b.getQcStatus() != null ? b.getQcStatus() : "RELEASED");
            m.put("abnormal", Boolean.TRUE.equals(b.getAbnormal()));
            m.put("created_at", b.getCreatedAt() != null ? b.getCreatedAt().toString() : LocalDateTime.now().toString());
            rows.add(m);
        }
        return rows;
    }

    @GetMapping("/roasts/month-record")
    public Map<String, Object> getRoastsMonthRecord(@RequestParam(required = false) String month) {
        String m = (month != null && !month.trim().isEmpty()) ? month : LocalDate.now().toString().substring(0, 7);
        LocalDateTime start = LocalDate.parse(m + "-01").atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        List<RoastBatch> monthBatches = roastRepository.findBetweenDates(start, end);

        long count = monthBatches.size();
        double inKg = monthBatches.stream().mapToDouble(b -> b.getGreenKg() != null ? b.getGreenKg() : 0.0).sum();
        double outKg = monthBatches.stream().mapToDouble(b -> b.getRoastedKg() != null ? b.getRoastedKg() : 0.0).sum();
        Double lossPct = inKg > 0 ? Math.round(((inKg - outKg) / inKg) * 1000.0) / 10.0 : null;
        double gasKg = Math.round(count * 1.2 * 10.0) / 10.0;
        Double gasPerRoast = count > 0 ? 1.2 : null;

        Map<String, Object> map = new HashMap<>();
        map.put("batches", count);
        map.put("input_kg", inKg);
        map.put("output_kg", outKg);
        map.put("loss_pct", lossPct);
        map.put("gas_kg", gasKg);
        map.put("gas_kg_per_roast", gasPerRoast);
        map.put("meter_count", count > 0 ? count : null);
        map.put("meter_gap", 0);
        map.put("start_reading", 0);
        map.put("end_reading", (int) count);
        map.put("cylinders", Collections.emptyList());
        return map;
    }

    @PostMapping("/roasts")
    public ResponseEntity<Map<String, Object>> recordRoast(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        Double totalGreen = 0.0;
        StringBuilder inputsSummary = new StringBuilder();
        Long primaryProductId = null;
        String primaryProductName = "Roasted Coffee";

        if (req.containsKey("inputs") && req.get("inputs") instanceof List) {
            List<?> inputs = (List<?>) req.get("inputs");
            for (Object item : inputs) {
                if (item instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) item;
                    Long lotId = m.containsKey("lot_id") ? Long.valueOf(m.get("lot_id").toString()) : null;
                    double kg = m.containsKey("kg") ? Double.parseDouble(m.get("kg").toString()) : 0.0;
                    if (lotId != null && kg > 0) {
                        totalGreen += kg;
                        Optional<InventoryLot> lotOpt = inventoryLotRepository.findById(lotId);
                        if (lotOpt.isPresent()) {
                            InventoryLot lot = lotOpt.get();
                            if (primaryProductId == null) {
                                primaryProductId = lot.getProductId();
                                primaryProductName = lot.getProduct();
                            }
                            if (inputsSummary.length() > 0) inputsSummary.append(", ");
                            inputsSummary.append(lot.getProduct()).append(" (").append(kg).append(" kg)");

                            // Deduct consumed green coffee from inventory lot
                            double currentAvail = lot.getAvailableKg() != null ? lot.getAvailableKg() : (lot.getKg() != null ? lot.getKg() : 0.0);
                            double currentTotal = lot.getKg() != null ? lot.getKg() : currentAvail;
                            double remainingAvail = Math.max(0.0, currentAvail - kg);
                            double remainingTotal = Math.max(0.0, currentTotal - kg);
                            lot.setAvailableKg(remainingAvail);
                            lot.setKg(remainingTotal);
                            lot.setUpdatedAt(LocalDateTime.now());
                            if (remainingAvail <= 0.001) {
                                lot.setStatus("CONSUMED");
                            }
                            inventoryLotRepository.save(lot);
                        }
                    }
                }
            }
        }

        if (totalGreen <= 0) {
            totalGreen = req.containsKey("green_kg") ? Double.valueOf(req.get("green_kg").toString()) : 30.0;
            inputsSummary.append("Green coffee (").append(totalGreen).append(" kg)");
        }

        Double roasted = req.containsKey("output_kg") ? Double.valueOf(req.get("output_kg").toString()) : 25.0;
        Double loss = totalGreen > 0 ? ((totalGreen - roasted) / totalGreen) * 100.0 : 0.0;
        loss = Math.round(loss * 10.0) / 10.0;

        int durationMin = req.containsKey("duration_min") ? Integer.parseInt(req.get("duration_min").toString()) : 12;
        boolean abnormal = req.containsKey("abnormal") && Boolean.parseBoolean(req.get("abnormal").toString());
        String notes = req.containsKey("notes") && req.get("notes") != null ? req.get("notes").toString() : "";
        String profileName = req.containsKey("profile_name") ? req.get("profile_name").toString() : "Medium Roast";
        if (req.containsKey("profile_id") && req.get("profile_id") != null) {
            long pid = Long.parseLong(req.get("profile_id").toString());
            if (pid == 1) profileName = "Medium Roast (Standard Filter)";
            else if (pid == 2) profileName = "Dark Roast (Espresso / South Indian Filter)";
        }

        // All finished roast batches are held in QC_HOLD until approved by Admin in Quality Control
        String qcStatus = "QC_HOLD";

        long count = roastRepository.count() + 1;
        String batchCode = "RST-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + String.format("%02d", count);

        // Create new Roasted Lot in MySQL
        InventoryLot roastedLot = new InventoryLot();
        roastedLot.setCode("LOT-" + batchCode);
        roastedLot.setProductId(primaryProductId);
        roastedLot.setProduct(primaryProductName + " (Roasted)");
        roastedLot.setName(primaryProductName + " (Roasted)");
        roastedLot.setStage("ROASTED");
        roastedLot.setLocation("PROD-01");
        roastedLot.setOwner("VTC");
        roastedLot.setStatus(qcStatus);
        roastedLot.setBagKind("SOUND");
        roastedLot.setBags(1);
        roastedLot.setKg(roasted);
        roastedLot.setAvailableKg("RELEASED".equals(qcStatus) ? roasted : 0.0);
        roastedLot.setResting(true);
        roastedLot.setReadyFrom(LocalDate.now().plusDays(1));
        roastedLot.setCreatedAt(LocalDateTime.now());
        InventoryLot savedLot = inventoryLotRepository.save(roastedLot);

        // Save RoastBatch to MySQL
        RoastBatch batch = new RoastBatch();
        batch.setBatchCode(batchCode);
        batch.setGreenKg(totalGreen);
        batch.setRoastedKg(roasted);
        batch.setLossPercent(loss);
        batch.setDurationMin(durationMin);
        batch.setAbnormal(abnormal);
        batch.setOperatorNotes(notes);
        batch.setInputsSummary(inputsSummary.toString());
        batch.setProfileName(profileName);
        batch.setRoasterName(currentUser != null ? currentUser.getFullName() : "Ramesh (Roaster)");
        batch.setQcStatus(qcStatus);
        batch.setOutputLotId(savedLot.getId());
        batch.setOutputLotCode(savedLot.getCode());
        batch.setCreatedAt(LocalDateTime.now());

        RoastBatch savedBatch = roastRepository.save(batch);

        // Autocomplete / progress increment on assigned roasting task
        if (currentUser != null) {
            try {
                List<Task> myRoastTasks = taskRepository.findByAssignedToAndKindAndStatusIn(
                        currentUser.getId(), "ROAST", Arrays.asList("OPEN", "IN_PROGRESS"));
                for (Task t : myRoastTasks) {
                    double prog = (t.getProgress() != null ? t.getProgress() : 0.0) + 1.0;
                    t.setProgress(prog);
                    if ("OPEN".equals(t.getStatus())) t.setStatus("IN_PROGRESS");
                    t.setUpdatedAt(LocalDateTime.now());
                    taskRepository.save(t);
                    break;
                }
            } catch (Exception ignored) {}
        }

        Map<String, Object> res = new HashMap<>();
        res.put("id", savedBatch.getId());
        res.put("code", savedBatch.getBatchCode());
        res.put("lot_id", savedLot.getId());
        res.put("output_kg", savedBatch.getRoastedKg());
        res.put("loss_pct", savedBatch.getLossPercent());
        res.put("qc_status", savedBatch.getQcStatus());
        return ResponseEntity.ok(res);
    }

    // Counter Sales Bills
    @GetMapping("/counter/bills")
    public List<CounterBill> getCounterBills() {
        return counterRepository.findTop50ByOrderByCreatedAtDesc();
    }

    @PostMapping("/counter/bills")
    public ResponseEntity<CounterBill> recordBill(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        CounterBill bill = new CounterBill();
        bill.setBillNumber("BILL-" + (System.currentTimeMillis() % 100000));
        bill.setCustomerName((String) req.getOrDefault("customer_name", "Walk-in Customer"));
        bill.setCustomerPhone((String) req.getOrDefault("customer_phone", "—"));
        bill.setTotalPaise(Long.valueOf(req.getOrDefault("total_paise", 0).toString()));
        bill.setPaymentMode((String) req.getOrDefault("payment_mode", "CASH"));
        bill.setCashierName(currentUser != null ? currentUser.getFullName() : "Cashier");

        CounterBill saved = counterRepository.save(bill);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/counter/customers")
    public List<Map<String, Object>> getCounterCustomers() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> c = new HashMap<>();
        c.put("customer", "Sample Customer");
        c.put("phone", "9000000001");
        c.put("visits", 1);
        c.put("spent", 840);
        c.put("first_seen", "27 Oct 2026, 5:36 pm");
        c.put("last_seen", "27 Oct 2026, 5:36 pm");
        list.add(c);
        return list;
    }
}


