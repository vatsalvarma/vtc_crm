package com.vtc.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtc.crm.entity.Client;
import com.vtc.crm.entity.ClientCollection;
import com.vtc.crm.entity.CounterBill;
import com.vtc.crm.entity.User;
import com.vtc.crm.repository.ClientCollectionRepository;
import com.vtc.crm.repository.ClientRepository;
import com.vtc.crm.repository.CounterBillRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class ClientAndCounterController {

    private final ClientRepository clientRepository;
    private final ClientCollectionRepository collectionRepository;
    private final CounterBillRepository counterBillRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClientAndCounterController(
            ClientRepository clientRepository,
            ClientCollectionRepository collectionRepository,
            CounterBillRepository counterBillRepository) {
        this.clientRepository = clientRepository;
        this.collectionRepository = collectionRepository;
        this.counterBillRepository = counterBillRepository;
    }

    @PostConstruct
    public void seedInitialData() {
        if (clientRepository.count() == 0) {
            // Seed matching screenshots
            LocalDate today = LocalDate.now();
            clientRepository.save(new Client(
                    "Kumar Traders", "Kumar", "9000000002", "Bengaluru",
                    "29ABCDE1234F1Z5", "dealer", "29", 1200000L,
                    today.minusDays(79).toString()
            ));
            clientRepository.save(new Client(
                    "Deccan Coffee Co", "Suresh", "9000000004", "Secunderabad",
                    "36ABCDE9999F1Z5", "dealer", "36", 0L, null
            ));
            clientRepository.save(new Client(
                    "Cafe Aroma", "Ravi", "9000000001", "Road 12, Banjara Hills",
                    "36ABCDE1234F1Z5", "cafe", "36", 500000L,
                    today.minusDays(44).toString()
            ));
            clientRepository.save(new Client(
                    "Brew Point", "Kiran", "9000000003", "Kondapur",
                    "", "cafe", "36", 180000L,
                    today.minusDays(16).toString()
            ));
            Client unsorted = new Client(
                    "Old Town Coffee Works", "Venkat", "9000000005", "Governorpet, Vijayawada",
                    "", "unsorted", "36", 0L, null
            );
            clientRepository.save(unsorted);
        }
    }

    // ---------------------------------------------------- /counter/setup
    @GetMapping("/counter/setup")
    public Map<String, Object> getCounterSetup() {
        Map<String, Object> setup = new HashMap<>();

        Map<String, Object> settings = new HashMap<>();
        settings.put("custom_coffee_rate_paise", 90000);
        settings.put("custom_chicory_rate_paise", 25000);
        settings.put("custom_chicory_product_id", 10);
        settings.put("discount_cap_pct", 10);
        settings.put("overdue_days", 30);
        settings.put("opening_cash_paise", 50000);
        settings.put("short_name", "VTC");
        settings.put("shop_name", "Vasavi Trading Company");
        setup.put("settings", settings);

        // Catalog items
        List<Map<String, Object>> items = new ArrayList<>();
        // Raw beans
        items.add(item(1L, "Plantation A (Raw Green Coffee)", "Plantation A (Green)", 38000, "raw_beans", "product", 0, false, 50.0));
        items.add(item(2L, "Robusta Cherry AB (Coorg/Wayanad)", "Robusta Cherry AB (Green)", 32000, "raw_beans", "product", 0, false, 40.0));
        items.add(item(3L, "Arabica Parchment (Raw Green)", "Arabica Parchment (Green)", 42000, "raw_beans", "product", 0, false, 35.0));

        // Roasted beans
        items.add(item(4L, "Commercial Strong (Roasted Beans)", "Commercial Strong (Beans)", 65000, "roasted_beans", "coffee_beans", 100, false, 30.0));
        items.add(item(5L, "Royal Hotel Blend (Roasted Beans)", "Royal Hotel (Beans)", 75000, "roasted_beans", "coffee_beans", 100, false, 25.0));
        items.add(item(6L, "Pure Arabica Roast (Roasted Beans)", "Arabica Roast (Beans)", 90000, "roasted_beans", "coffee_beans", 100, false, 18.0));

        // Chicory
        items.add(item(7L, "Jamnagar Roast Chicory", "Roast Chicory", 25000, "chicory", "product", 0, false, 60.0));

        // Blends / Ground
        items.add(item(8L, "Commercial Strong 60:40 (Ground)", "Commercial Strong 60/40", 52000, "blend", "coffee_ground", 60, true, 30.0));
        items.add(item(9L, "Royal Hotel Blend 70:30 (Ground)", "Royal Hotel 70/30", 62000, "blend", "coffee_ground", 70, true, 28.0));
        items.add(item(10L, "Special Chicory Mix (Custom)", "Chicory Mix", 25000, "chicory", "product", 0, false, 40.0));

        setup.put("items", items);

        // Blends for Cafe
        List<Map<String, Object>> blends = new ArrayList<>();
        blends.add(blend(1L, "Royal Hotel Blend 70:30", "Royal 70:30", 62000, true, "#6366f1"));
        blends.add(blend(2L, "Commercial Strong 60:40", "Strong 60:40", 52000, true, "#d97706"));
        blends.add(blend(3L, "Executive Pure 85:15", "Exec 85:15", 78000, true, "#10b981"));
        blends.add(blend(4L, "Signature House Blend 80:20", "House 80:20", 72000, true, "#78350f"));
        setup.put("blends", blends);

        // Stock
        Map<String, Object> stock = new HashMap<>();
        stock.put("coffee_ground", 45.0);
        stock.put("coffee_beans", 73.0);
        stock.put("custom_chicory", 40.0);
        stock.put("resting", Collections.emptyMap());
        setup.put("stock", stock);

        setup.put("incoming", Collections.emptyList());
        setup.put("cash_paise", 50000);
        setup.put("day_closed", null);

        return setup;
    }

    private Map<String, Object> item(Long id, String name, String billName, int pricePaise, String group, String kind, int coffeePct, boolean mixChicory, double stockKg) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("bill_name", billName);
        m.put("price_paise", pricePaise);
        m.put("price_per_kg_paise", pricePaise);
        m.put("item_group", group);
        m.put("kind", kind);
        m.put("coffee_pct", coffeePct);
        m.put("mix_chicory", mixChicory);
        m.put("stock_kg", stockKg);
        m.put("active", true);
        return m;
    }

    private Map<String, Object> blend(Long id, String name, String billName, int basePricePaise, boolean isShared, String color) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("bill_name", billName);
        m.put("base_price_paise", basePricePaise);
        m.put("is_shared", isShared);
        m.put("color_tag", color);
        m.put("active", true);
        return m;
    }

    // ---------------------------------------------------- /clients
    @GetMapping("/clients")
    public Map<String, Object> getClients(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String context) {

        List<Client> all = clientRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Client cl : all) {
            if (type != null && !type.equalsIgnoreCase("all") && !type.equalsIgnoreCase(cl.getClientType())) {
                continue;
            }
            if (q != null && !q.trim().isEmpty()) {
                String term = q.toLowerCase();
                boolean match = cl.getName().toLowerCase().contains(term)
                        || (cl.getPhone() != null && cl.getPhone().contains(term))
                        || (cl.getContact() != null && cl.getContact().toLowerCase().contains(term));
                if (!match) continue;
            }

            Map<String, Object> row = buildClientRow(cl, today);
            rows.add(row);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("clients", rows);
        res.put("overdue_days", 30);
        return res;
    }

    @PostMapping("/clients")
    public Map<String, Object> addClient(@RequestBody Map<String, Object> body) {
        Client client = new Client();
        client.setName((String) body.getOrDefault("name", "New Client"));
        client.setContact((String) body.getOrDefault("contact", ""));
        client.setPhone((String) body.getOrDefault("phone", ""));
        client.setAddress((String) body.getOrDefault("address", ""));
        client.setGstin((String) body.getOrDefault("gstin", ""));
        client.setClientType((String) body.getOrDefault("client_type", "dealer"));
        client.setStateCode((String) body.getOrDefault("state_code", "36"));
        if (body.get("credit_limit_paise") != null) {
            client.setCreditLimitPaise(Long.valueOf(body.get("credit_limit_paise").toString()));
        }
        if (body.get("opening_paise") != null) {
            client.setOpeningPaise(Long.valueOf(body.get("opening_paise").toString()));
        }
        client.setOpeningDate((String) body.get("opening_date"));
        client.setActive(true);

        Client saved = clientRepository.save(client);
        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("ok", true);
        return res;
    }

    @PutMapping("/clients/{id}")
    public Map<String, Object> updateClient(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Client cl = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));

        if (body.containsKey("active")) {
            cl.setActive(Boolean.valueOf(body.get("active").toString()));
        }
        if (body.containsKey("name")) cl.setName((String) body.get("name"));
        if (body.containsKey("contact")) cl.setContact((String) body.get("contact"));
        if (body.containsKey("phone")) cl.setPhone((String) body.get("phone"));
        if (body.containsKey("address")) cl.setAddress((String) body.get("address"));
        if (body.containsKey("gstin")) cl.setGstin((String) body.get("gstin"));
        if (body.containsKey("client_type")) cl.setClientType((String) body.get("client_type"));
        if (body.containsKey("state_code")) cl.setStateCode((String) body.get("state_code"));
        if (body.containsKey("credit_limit_paise") && body.get("credit_limit_paise") != null) {
            cl.setCreditLimitPaise(Long.valueOf(body.get("credit_limit_paise").toString()));
        }
        if (body.containsKey("opening_paise") && body.get("opening_paise") != null) {
            cl.setOpeningPaise(Long.valueOf(body.get("opening_paise").toString()));
        }
        if (body.containsKey("opening_date")) cl.setOpeningDate((String) body.get("opening_date"));
        cl.setUpdatedAt(LocalDateTime.now());

        clientRepository.save(cl);
        return Collections.singletonMap("ok", true);
    }

    @PostMapping("/clients/categorize-legacy")
    public Map<String, Object> categorizeLegacy(@RequestBody Map<String, String> body) {
        for (Map.Entry<String, String> e : body.entrySet()) {
            try {
                Long cid = Long.valueOf(e.getKey());
                String ctype = e.getValue();
                clientRepository.findById(cid).ifPresent(c -> {
                    c.setClientType(ctype);
                    clientRepository.save(c);
                });
            } catch (Exception ignored) {}
        }
        return Collections.singletonMap("ok", true);
    }

    @GetMapping("/clients/{id}")
    public Map<String, Object> getClientDetails(@PathVariable Long id) {
        Client cl = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));
        LocalDate today = LocalDate.now();
        Map<String, Object> sm = computeSummary(cl, today);

        // Build running statement
        List<Map<String, Object>> statement = new ArrayList<>();
        long balance = 0;
        if (cl.getOpeningPaise() != null && cl.getOpeningPaise() > 0) {
            Map<String, Object> op = new HashMap<>();
            op.put("day", cl.getOpeningDate() != null ? cl.getOpeningDate() : cl.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            op.put("what", "Opening balance (before this app)");
            op.put("debit", cl.getOpeningPaise());
            op.put("credit", 0L);
            balance += cl.getOpeningPaise();
            op.put("balance", balance);
            statement.add(op);
        }

        List<ClientCollection> collections = collectionRepository.findByClientIdOrderByCreatedAtDesc(id);
        for (ClientCollection col : collections) {
            if (col.getCancelledAt() == null) {
                Map<String, Object> cp = new HashMap<>();
                cp.put("day", col.getDay());
                cp.put("what", "Received " + col.getMethod() + (col.getReference() != null && !col.getReference().isEmpty() ? " " + col.getReference() : ""));
                cp.put("debit", 0L);
                cp.put("credit", col.getAmountPaise());
                balance -= col.getAmountPaise();
                cp.put("balance", balance);
                statement.add(cp);
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("client", cl);
        res.put("summary", sm);
        res.put("statement", statement);
        res.put("collections", collections);
        res.put("overdue_days", 30);
        return res;
    }

    @PostMapping("/clients/{id}/collect")
    public Map<String, Object> collectPayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {

        Client cl = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));
        Long amountPaise = Long.valueOf(req.get("amount_paise").toString());
        String method = (String) req.getOrDefault("method", "CASH");
        String reference = (String) req.getOrDefault("reference", "");
        String note = (String) req.getOrDefault("note", "");

        ClientCollection collection = new ClientCollection(
                cl.getId(),
                LocalDate.now().toString(),
                amountPaise,
                method,
                reference,
                note,
                currentUser != null ? currentUser.getFullName() : "Admin"
        );
        ClientCollection saved = collectionRepository.save(collection);

        Map<String, Object> sm = computeSummary(cl, LocalDate.now());
        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("balance_paise", sm.get("balance_paise"));
        res.put("ok", true);
        return res;
    }

    @GetMapping("/clients/{id}/reminder")
    public Map<String, Object> getReminder(@PathVariable Long id) {
        Client cl = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("CLIENT_NOT_FOUND"));
        Map<String, Object> sm = computeSummary(cl, LocalDate.now());
        long bal = (Long) sm.get("balance_paise");
        int oldest = (Integer) sm.get("oldest_days");

        String contact = cl.getContact() != null && !cl.getContact().isEmpty() ? cl.getContact() : cl.getName();
        String oldestStr = oldest > 0 ? " Your oldest unpaid bill is " + oldest + " days old." : "";
        String text = String.format("Dear %s, this is a reminder from Vasavi Trading Company. Your balance is Rs. %.2f.%s Please arrange the payment. Thank you.",
                contact, bal / 100.0, oldestStr);

        String phone = cl.getPhone() != null ? cl.getPhone().replaceAll("\\D", "") : "";
        String link = phone.length() >= 10 ? "https://wa.me/91" + phone + "?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) : null;

        Map<String, Object> res = new HashMap<>();
        res.put("text", text);
        res.put("phone", phone);
        res.put("link", link);
        return res;
    }

    // ---------------------------------------------------- /dues
    @GetMapping("/dues")
    public Map<String, Object> getDues(@RequestParam(required = false) String type) {
        List<Client> clients = clientRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        LocalDate today = LocalDate.now();

        long totalBal = 0;
        long totalAdvance = 0;
        long b0_30 = 0, b31_60 = 0, b61_90 = 0, b90_plus = 0;

        for (Client cl : clients) {
            if (type != null && !type.equalsIgnoreCase("all") && !type.equalsIgnoreCase(cl.getClientType())) {
                continue;
            }
            Map<String, Object> row = buildClientRow(cl, today);
            long bal = (Long) row.get("balance_paise");
            if (bal != 0) {
                rows.add(row);
                if (bal > 0) totalBal += bal;
                else totalAdvance += -bal;

                Map<String, Long> b = (Map<String, Long>) row.get("buckets");
                b0_30 += b.getOrDefault("0_30", 0L);
                b31_60 += b.getOrDefault("31_60", 0L);
                b61_90 += b.getOrDefault("61_90", 0L);
                b90_plus += b.getOrDefault("90_plus", 0L);
            }
        }

        rows.sort((a, b) -> Long.compare((Long) b.get("balance_paise"), (Long) a.get("balance_paise")));

        Map<String, Object> totals = new HashMap<>();
        totals.put("balance_paise", totalBal);
        totals.put("advance_paise", totalAdvance);
        totals.put("0_30", b0_30);
        totals.put("31_60", b31_60);
        totals.put("61_90", b61_90);
        totals.put("90_plus", b90_plus);

        Map<String, Object> res = new HashMap<>();
        res.put("clients", rows);
        res.put("totals", totals);
        res.put("overdue_days", 30);
        return res;
    }

    // ---------------------------------------------------- /blends
    @GetMapping("/blends")
    public List<Map<String, Object>> getBlends(@RequestParam(required = false) Long cafe_id) {
        List<Map<String, Object>> blends = new ArrayList<>();
        blends.add(blend(1L, "Royal Hotel Blend 70:30", "Royal 70:30", 62000, true, "#6366f1"));
        blends.add(blend(2L, "Commercial Strong 60:40", "Strong 60:40", 52000, true, "#d97706"));
        blends.add(blend(3L, "Executive Pure 85:15", "Exec 85:15", 78000, true, "#10b981"));
        blends.add(blend(4L, "Signature House Blend 80:20", "House 80:20", 72000, true, "#78350f"));
        return blends;
    }

    // ---------------------------------------------------- /counter/sales (List and Record bills)
    @GetMapping("/counter/sales")
    public Map<String, Object> getCounterSales(@RequestParam(required = false) String day) {
        String queryDay = day != null ? day : LocalDate.now().toString();
        List<CounterBill> all = counterBillRepository.findAll();
        List<Map<String, Object>> sales = new ArrayList<>();
        long totalPaise = 0;
        for (CounterBill b : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("bill_no", b.getBillNumber());
            m.put("created_at", b.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
            m.put("cashier", b.getCashierName());
            m.put("customer_name", b.getCustomerName());
            m.put("total_paise", b.getTotalPaise());
            m.put("methods", b.getPaymentMode());
            m.put("status", b.getStatus());
            sales.add(m);
            totalPaise += b.getTotalPaise();
        }
        Map<String, Object> totals = new HashMap<>();
        totals.put("bills", sales.size());
        totals.put("total_paise", totalPaise);

        Map<String, Object> res = new HashMap<>();
        res.put("day", queryDay);
        res.put("sales", sales);
        res.put("totals", totals);
        res.put("pending_refunds", Collections.emptyList());
        res.put("closed", null);
        return res;
    }

    @PostMapping("/counter/sales")
    public ResponseEntity<?> recordSale(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {

        String billNo = "CTR-" + (System.currentTimeMillis() % 1000000);
        Long totalPaise = Long.valueOf(req.getOrDefault("total_paise", 0).toString());
        Long discountPaise = Long.valueOf(req.getOrDefault("discount_paise", 0).toString());
        String discountReason = (String) req.getOrDefault("discount_reason", "");
        String customerName = (String) req.getOrDefault("customer_name", "Walk-in Customer");
        String customerPhone = (String) req.getOrDefault("customer_phone", "—");
        Long clientId = req.get("client_id") != null ? Long.valueOf(req.get("client_id").toString()) : null;
        String clientType = (String) req.getOrDefault("client_type", "walk_in");
        String billType = (String) req.getOrDefault("bill_type", "counter");

        List<Map<String, Object>> linesReq = (List<Map<String, Object>>) req.getOrDefault("lines", Collections.emptyList());
        List<Map<String, Object>> paymentsReq = (List<Map<String, Object>>) req.getOrDefault("payments", Collections.emptyList());

        String paymentMode = "CASH";
        long paidPaise = 0;
        for (Map<String, Object> p : paymentsReq) {
            paymentMode = (String) p.getOrDefault("method", "CASH");
            paidPaise += Long.valueOf(p.getOrDefault("amount_paise", 0).toString());
        }

        long duePaise = Math.max(0, totalPaise - paidPaise);

        CounterBill bill = new CounterBill();
        bill.setBillNumber(billNo);
        bill.setCustomerName(customerName);
        bill.setCustomerPhone(customerPhone);
        bill.setClientId(clientId);
        bill.setClientType(clientType);
        bill.setBillType(billType);
        bill.setTotalPaise(totalPaise);
        bill.setSubtotalPaise(totalPaise + discountPaise);
        bill.setDiscountPaise(discountPaise);
        bill.setDiscountReason(discountReason);
        bill.setPaidPaise(paidPaise);
        bill.setDuePaise(duePaise);
        bill.setPaymentMode(paymentMode);
        bill.setCashierName(currentUser != null ? currentUser.getFullName() : "Admin");
        bill.setStatus("PAID");

        try {
            bill.setItemsJson(objectMapper.writeValueAsString(linesReq));
        } catch (Exception e) {
            bill.setItemsJson("[]");
        }

        CounterBill saved = counterBillRepository.save(bill);

        // If billed to a client with credit/due, it is now tracked in their balance
        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("bill_no", saved.getBillNumber());
        res.put("created_at", saved.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        res.put("cashier", saved.getCashierName());
        res.put("customer_name", saved.getCustomerName());
        res.put("total_paise", saved.getTotalPaise());
        res.put("subtotal_paise", saved.getSubtotalPaise());
        res.put("discount_paise", saved.getDiscountPaise());
        res.put("status", saved.getStatus());
        res.put("reprints", 0);

        Map<String, Object> header = new HashMap<>();
        header.put("company_name", "Vasavi Trading Company");
        header.put("company_address", "Vijayawada, Andhra Pradesh");
        header.put("company_gstin", "37ABCDE1234F1Z5");
        header.put("tax_setup_approved", true);
        res.put("header", header);

        List<Map<String, Object>> billLines = new ArrayList<>();
        for (Map<String, Object> l : linesReq) {
            Map<String, Object> bl = new HashMap<>();
            bl.put("item_name", l.getOrDefault("name", "Coffee Product"));
            bl.put("weight_kg", l.getOrDefault("weight_kg", 1.0));
            bl.put("rate_paise", l.getOrDefault("rate", 60000));
            bl.put("amount_paise", l.getOrDefault("amount", 60000));
            billLines.add(bl);
        }
        res.put("lines", billLines);
        res.put("payments", paymentsReq.isEmpty() ? Collections.singletonList(Collections.singletonMap("method", "CASH")) : paymentsReq);

        return ResponseEntity.ok(res);
    }

    private Map<String, Object> buildClientRow(Client cl, LocalDate today) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", cl.getId());
        m.put("name", cl.getName());
        m.put("contact", cl.getContact() != null ? cl.getContact() : "");
        m.put("phone", cl.getPhone() != null ? cl.getPhone() : "");
        m.put("address", cl.getAddress() != null ? cl.getAddress() : "");
        m.put("gstin", cl.getGstin() != null ? cl.getGstin() : "");
        m.put("client_type", cl.getClientType() != null ? cl.getClientType() : "dealer");
        m.put("state_code", cl.getStateCode() != null ? cl.getStateCode() : "36");
        m.put("active", cl.getActive());
        m.put("credit_limit_paise", cl.getCreditLimitPaise());
        m.put("opening_paise", cl.getOpeningPaise());
        m.put("opening_date", cl.getOpeningDate());

        Map<String, Object> sm = computeSummary(cl, today);
        m.put("balance_paise", sm.get("balance_paise"));
        m.put("oldest_days", sm.get("oldest_days"));
        m.put("buckets", sm.get("buckets"));
        return m;
    }

    private Map<String, Object> computeSummary(Client cl, LocalDate today) {
        long balance = cl.getOpeningPaise() != null ? cl.getOpeningPaise() : 0L;
        int oldestDays = 0;

        if (cl.getOpeningDate() != null && !cl.getOpeningDate().isEmpty() && balance > 0) {
            try {
                LocalDate opDate = LocalDate.parse(cl.getOpeningDate());
                oldestDays = (int) ChronoUnit.DAYS.between(opDate, today);
                if (oldestDays < 0) oldestDays = 0;
            } catch (Exception ignored) {}
        }

        // Subtract non-cancelled collections
        List<ClientCollection> cols = collectionRepository.findByClientIdOrderByCreatedAtDesc(cl.getId());
        long collected = 0;
        for (ClientCollection c : cols) {
            if (c.getCancelledAt() == null) {
                collected += c.getAmountPaise();
            }
        }
        balance = Math.max(0, balance - collected);
        if (balance == 0) oldestDays = 0;

        Map<String, Long> buckets = new HashMap<>();
        buckets.put("0_30", 0L);
        buckets.put("31_60", 0L);
        buckets.put("61_90", 0L);
        buckets.put("90_plus", 0L);

        if (balance > 0) {
            if (oldestDays <= 30) buckets.put("0_30", balance);
            else if (oldestDays <= 60) buckets.put("31_60", balance);
            else if (oldestDays <= 90) buckets.put("61_90", balance);
            else buckets.put("90_plus", balance);
        }

        Map<String, Object> sm = new HashMap<>();
        sm.put("balance_paise", balance);
        sm.put("oldest_days", oldestDays);
        sm.put("buckets", buckets);
        return sm;
    }
}
