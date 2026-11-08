package com.vtc.crm.controller;

import com.vtc.crm.entity.CounterBill;
import com.vtc.crm.entity.RoastBatch;
import com.vtc.crm.entity.User;
import com.vtc.crm.repository.CounterBillRepository;
import com.vtc.crm.repository.RoastBatchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class OperationsAndSalesController {

    private final RoastBatchRepository roastRepository;
    private final CounterBillRepository counterRepository;

    public OperationsAndSalesController(
            RoastBatchRepository roastRepository,
            CounterBillRepository counterRepository) {
        this.roastRepository = roastRepository;
        this.counterRepository = counterRepository;
    }

    // Daily Roasting Batches
    @GetMapping("/roasts")
    public Object getRoasts(
            @RequestParam(required = false) String day,
            @RequestParam(required = false) String month) {
        if (month != null && !month.trim().isEmpty()) {
            Map<String, Object> res = new HashMap<>();
            Map<String, Object> days = new HashMap<>();
            List<RoastBatch> all = roastRepository.findAll();
            for (RoastBatch b : all) {
                if (b.getCreatedAt() != null) {
                    String d = b.getCreatedAt().toString().substring(0, 10);
                    if (d.startsWith(month)) {
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
            }
            res.put("days", days);
            return res;
        }

        List<RoastBatch> list = roastRepository.findTop30ByOrderByCreatedAtDesc();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (RoastBatch b : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("code", b.getBatchCode());
            m.put("roaster", b.getRoasterName());
            m.put("input_kg", b.getGreenKg());
            m.put("output_kg", b.getRoastedKg());
            m.put("loss_kg", b.getGreenKg() != null && b.getRoastedKg() != null ? b.getGreenKg() - b.getRoastedKg() : 0.0);
            m.put("duration_min", 12);
            m.put("profile", b.getProfileName());
            m.put("qc_status", b.getQcStatus() != null ? b.getQcStatus() : "RELEASED");
            m.put("abnormal", false);
            m.put("created_at", b.getCreatedAt() != null ? b.getCreatedAt().toString() : LocalDateTime.now().toString());
            rows.add(m);
        }
        return rows;
    }

    @GetMapping("/roasts/month-record")
    public Map<String, Object> getRoastsMonthRecord(@RequestParam(required = false) String month) {
        Map<String, Object> map = new HashMap<>();
        long count = roastRepository.count();
        map.put("batches", count);
        map.put("input_kg", 0);
        map.put("output_kg", 0);
        map.put("loss_pct", null);
        map.put("gas_kg", 0);
        map.put("gas_kg_per_roast", null);
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
        Double green = 0.0;
        if (req.containsKey("inputs") && req.get("inputs") instanceof List) {
            List<?> inputs = (List<?>) req.get("inputs");
            for (Object item : inputs) {
                if (item instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) item;
                    if (m.containsKey("kg")) {
                        green += Double.parseDouble(m.get("kg").toString());
                    }
                }
            }
        } else if (req.containsKey("green_kg")) {
            green = Double.valueOf(req.get("green_kg").toString());
        }
        if (green <= 0) green = 30.0;

        Double roasted = req.containsKey("output_kg") ? Double.valueOf(req.get("output_kg").toString()) : (req.containsKey("roasted_kg") ? Double.valueOf(req.get("roasted_kg").toString()) : 25.0);
        Double loss = ((green - roasted) / green) * 100.0;

        RoastBatch batch = new RoastBatch();
        batch.setBatchCode("BATCH-" + (System.currentTimeMillis() % 10000));
        batch.setGreenKg(green);
        batch.setRoastedKg(roasted);
        batch.setLossPercent(Math.round(loss * 10.0) / 10.0);
        batch.setProfileName((String) req.getOrDefault("profile_name", "Medium Roast"));
        batch.setRoasterName(currentUser != null ? currentUser.getFullName() : "Roaster");
        batch.setQcStatus("RELEASED");

        RoastBatch saved = roastRepository.save(batch);

        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("code", saved.getBatchCode());
        res.put("lot_id", saved.getId());
        res.put("output_kg", saved.getRoastedKg());
        res.put("loss_pct", saved.getLossPercent());
        res.put("qc_status", saved.getQcStatus());
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


