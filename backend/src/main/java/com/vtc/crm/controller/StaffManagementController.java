package com.vtc.crm.controller;

import com.vtc.crm.entity.User;
import com.vtc.crm.entity.UserDocument;
import com.vtc.crm.repository.UserRepository;
import com.vtc.crm.service.AuthService;
import com.vtc.crm.service.StaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class StaffManagementController {

    private final StaffService staffService;
    private final UserRepository userRepository;
    private final AuthService authService;

    private static final List<String> ALL_PERMISSIONS = Arrays.asList(
            "dashboard.my_view", "dashboard.team_view", "tasks.view_own", "tasks.update_own", "tasks.view_all",
            "tasks.assign", "tasks.update_any", "tasks.approve", "goods.view", "goods.receive", "goods.reverse",
            "inventory.view", "inventory.transfer", "stock.cycle_count", "roast.view", "roast.execute",
            "outside.view", "outside.execute", "pack.view", "pack.execute", "counter.sell", "counter.cash",
            "counter.customers", "counter.insights", "outgoing.view", "outgoing.create", "maintenance.raise",
            "maintenance.manage", "team.view", "reports.operational", "recipe.view_public", "qc.view", "roast.qc_release",
            "masters.view", "masters.create_draft", "masters.approve", "system.settings", "system.users",
            "system.roles", "comms.oversight", "team.profiles", "team.hierarchy", "inward.receive",
            "roast.record", "pack.record", "counter.cash_verify", "system.grants", "system.audit", "system.preview_as"
    );

    public StaffManagementController(StaffService staffService, UserRepository userRepository, AuthService authService) {
        this.staffService = staffService;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    // 1. Staff List (from MySQL)
    @GetMapping("/admin/users")
    public List<Map<String, Object>> listStaff() {
        return staffService.getAllUsers();
    }

    // 2. Add New Staff (persisted to MySQL)
    @PostMapping("/admin/users")
    public ResponseEntity<?> addStaff(
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        try {
            String actor = currentUser != null ? currentUser.getUsername() : "admin";
            Map<String, Object> created = staffService.createUser(req, actor);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 3. Edit / Manage Staff
    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateStaff(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        try {
            String actor = currentUser != null ? currentUser.getUsername() : "admin";
            Map<String, Object> updated = staffService.updateUser(id, req, actor);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 4. Remove / Deactivate Staff
    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteStaff(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        try {
            String actor = currentUser != null ? currentUser.getUsername() : "admin";
            staffService.deleteUser(id, actor);
            return ResponseEntity.ok(Collections.singletonMap("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 5. Reset Staff PIN / Password (supporting both /secret and /reset-secret)
    @PostMapping({"/admin/users/{id}/secret", "/admin/users/{id}/reset-secret"})
    public ResponseEntity<?> resetSecret(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        try {
            String actor = currentUser != null ? currentUser.getUsername() : "admin";
            return ResponseEntity.ok(staffService.resetSecret(id, req, actor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // 6. User Access Details
    @GetMapping("/admin/users/{id}/access")
    public ResponseEntity<?> getUserAccess(@PathVariable Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "USER_NOT_FOUND"));
        }
        User user = opt.get();
        Map<String, Object> meData = authService.formatMeResponse(user);
        @SuppressWarnings("unchecked")
        List<String> perms = (List<String>) meData.getOrDefault("perms", ALL_PERMISSIONS);

        Map<String, String> why = new HashMap<>();
        for (String p : perms) {
            why.put(p, "owner".equalsIgnoreCase(user.getUsername()) ? "owner" : "role");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("user", authService.formatUserMap(user));
        res.put("perms", perms);
        res.put("why", why);
        res.put("grants", Collections.emptyList());
        res.put("ceilings", Collections.emptyList());
        return ResponseEntity.ok(res);
    }

    // 7. Preview what staff sees
    @GetMapping("/admin/users/{id}/preview")
    public ResponseEntity<?> getUserPreview(@PathVariable Long id) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "USER_NOT_FOUND"));
        }
        User user = opt.get();
        Map<String, Object> meData = authService.formatMeResponse(user);

        Map<String, Object> res = new HashMap<>();
        res.put("user", authService.formatUserMap(user));
        res.put("modules", meData.get("modules"));
        @SuppressWarnings("unchecked")
        Map<String, Object> prefs = (Map<String, Object>) meData.get("prefs");
        res.put("widgets", prefs != null ? prefs.get("widgets") : Arrays.asList("stock", "roasts", "quick_actions"));
        res.put("perms", meData.get("perms"));
        return ResponseEntity.ok(res);
    }

    // 8. User Grants (overrides)
    @PostMapping("/admin/users/{id}/grants")
    public ResponseEntity<?> grantAccess(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("id", System.currentTimeMillis());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/admin/grants/{grantId}")
    public ResponseEntity<?> deleteGrant(@PathVariable Long grantId) {
        return ResponseEntity.ok(Collections.singletonMap("ok", true));
    }

    // 9. Roles & Permissions List
    @GetMapping("/admin/roles")
    public List<Map<String, Object>> listRoles() {
        long adminCount = userRepository.countByRole("Admin");
        long managerCount = userRepository.countByRole("Manager");
        long staffCount = userRepository.countByRole("Staff");

        List<Map<String, Object>> roles = new ArrayList<>();

        Map<String, Object> adminRole = new HashMap<>();
        adminRole.put("id", 1);
        adminRole.put("key", "admin");
        adminRole.put("name", "Admin");
        adminRole.put("tier", "admin");
        adminRole.put("active", true);
        adminRole.put("locked", true);
        adminRole.put("is_system", true);
        adminRole.put("people", adminCount);
        adminRole.put("description", "Full administrative control over people, inventory, prices and audit logs.");
        adminRole.put("permissions", ALL_PERMISSIONS);
        roles.add(adminRole);

        List<String> managerPerms = new ArrayList<>();
        for (String p : ALL_PERMISSIONS) {
            if (!p.startsWith("system.")) managerPerms.add(p);
        }
        Map<String, Object> managerRole = new HashMap<>();
        managerRole.put("id", 2);
        managerRole.put("key", "manager");
        managerRole.put("name", "Manager");
        managerRole.put("tier", "manager");
        managerRole.put("active", true);
        managerRole.put("locked", false);
        managerRole.put("is_system", true);
        managerRole.put("people", managerCount);
        managerRole.put("description", "Operations supervisor over daily roasting, orders, dispatches and team tasks.");
        managerRole.put("permissions", managerPerms);
        roles.add(managerRole);

        List<String> staffPerms = Arrays.asList(
                "dashboard.my_view", "tasks.view_own", "tasks.update_own", "inward.receive",
                "inventory.view", "roast.view", "roast.execute", "roast.record", "pack.view",
                "pack.execute", "pack.record", "counter.sell"
        );
        Map<String, Object> staffRole = new HashMap<>();
        staffRole.put("id", 3);
        staffRole.put("key", "staff");
        staffRole.put("name", "Staff");
        staffRole.put("tier", "staff");
        staffRole.put("active", true);
        staffRole.put("locked", false);
        staffRole.put("is_system", true);
        staffRole.put("people", staffCount);
        staffRole.put("description", "Factory floor operators with PIN login: roasters, packers, retail counter cashiers.");
        staffRole.put("permissions", staffPerms);
        roles.add(staffRole);

        return roles;
    }

    @PostMapping("/admin/roles")
    public ResponseEntity<?> createRole(@RequestBody Map<String, Object> req) {
        String name = (String) req.getOrDefault("name", "Custom Role");
        String tier = (String) req.getOrDefault("tier", "staff");
        Map<String, Object> newRole = new HashMap<>();
        newRole.put("id", System.currentTimeMillis() % 100000);
        newRole.put("key", name.toLowerCase().replaceAll("\\s+", "_"));
        newRole.put("name", name);
        newRole.put("tier", tier);
        newRole.put("active", true);
        newRole.put("locked", false);
        newRole.put("is_system", false);
        newRole.put("people", 0);
        newRole.put("description", req.getOrDefault("description", ""));
        newRole.put("permissions", Collections.emptyList());
        return ResponseEntity.ok(newRole);
    }

    @PutMapping("/admin/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        return ResponseEntity.ok(Collections.singletonMap("ok", true));
    }

    // 10. Admin Catalog (Permissions & Widgets)
    @GetMapping("/admin/catalog")
    public Map<String, Object> getCatalog() {
        Map<String, Object> cat = new HashMap<>();

        List<Map<String, Object>> permissions = new ArrayList<>();
        permissions.add(createPerm("system.users", "Manage people and their sign-in", "admin", "action", "high", true, true));
        permissions.add(createPerm("system.roles", "Manage roles & permissions", "admin", "action", "high", true, true));
        permissions.add(createPerm("system.grants", "Give or remove personal access overrides", "admin", "action", "high", true, true));
        permissions.add(createPerm("system.settings", "Change company settings and prices", "admin", "action", "high", true, true));
        permissions.add(createPerm("system.audit", "View the system audit log", "admin", "view", "normal", true, true));
        permissions.add(createPerm("system.preview_as", "Preview what another person sees", "admin", "view", "normal", true, true));
        permissions.add(createPerm("masters.approve", "Approve master data (clients, suppliers, beans)", "admin", "action", "normal", true, true));
        permissions.add(createPerm("masters.create_draft", "Suggest draft master data", "admin", "action", "normal", true, false));
        permissions.add(createPerm("team.profiles", "View staff details and documents", "team", "view", "high", true, true));
        permissions.add(createPerm("team.hierarchy", "Change reporting hierarchy", "team", "action", "normal", true, true));
        permissions.add(createPerm("inward.receive", "Receive green coffee and stock inward", "inward", "action", "normal", true, false));
        permissions.add(createPerm("roast.record", "Record daily roasting batches", "roasting", "action", "normal", true, false));
        permissions.add(createPerm("pack.record", "Record packing & packaging", "packing", "action", "normal", true, false));
        permissions.add(createPerm("counter.sell", "Create bills at the retail counter", "counter", "action", "normal", true, false));
        permissions.add(createPerm("counter.cash_verify", "Open cash drawer and view cash today", "counter", "field", "high", true, true));

        List<Map<String, Object>> widgets = new ArrayList<>();
        widgets.add(createWidget("stock", "Beans in stock"));
        widgets.add(createWidget("pending_invoices", "Invoices to review"));
        widgets.add(createWidget("holds", "Quality holds"));
        widgets.add(createWidget("orders", "Sales orders"));
        widgets.add(createWidget("team", "Active team tasks"));
        widgets.add(createWidget("roasts", "Roasting batches"));
        widgets.add(createWidget("audit", "Audit activity"));
        widgets.add(createWidget("quick_actions", "Quick actions"));

        cat.put("permissions", permissions);
        cat.put("widgets", widgets);
        return cat;
    }

    // 11. Teams & Hierarchy
    @GetMapping("/teams")
    public List<Map<String, Object>> listTeams() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", 1);
        t1.put("name", "Roastery Floor");
        list.add(t1);

        Map<String, Object> t2 = new HashMap<>();
        t2.put("id", 2);
        t2.put("name", "Counter & Dispatch");
        list.add(t2);

        Map<String, Object> t3 = new HashMap<>();
        t3.put("id", 3);
        t3.put("name", "Quality & Lab");
        list.add(t3);

        return list;
    }

    @GetMapping("/admin/users/{id}/teams")
    public List<Integer> getUserTeams(@PathVariable Long id) {
        return Arrays.asList(1, 2);
    }

    @PutMapping("/admin/users/{id}/teams")
    public ResponseEntity<?> updateUserTeams(@PathVariable Long id, @RequestBody Object body) {
        return ResponseEntity.ok(Collections.singletonMap("ok", true));
    }

    // 12. Org Chart & Hierarchy
    @GetMapping("/team")
    public List<Map<String, Object>> getTeamHierarchy() {
        return staffService.getTeamHierarchy();
    }

    // 13. Staff Confidential Profile
    @GetMapping("/team/{id}/profile")
    public ResponseEntity<?> getStaffProfile(@PathVariable Long id) {
        return ResponseEntity.ok(staffService.getUserProfile(id));
    }

    @PutMapping("/team/{id}/profile")
    public ResponseEntity<?> updateStaffProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        String actor = currentUser != null ? currentUser.getUsername() : "admin";
        return ResponseEntity.ok(staffService.updateUserProfile(id, req, actor));
    }

    // 14. Staff Documents
    @GetMapping("/team/{id}/documents")
    public List<UserDocument> getStaffDocuments(@PathVariable Long id) {
        return staffService.getUserDocuments(id);
    }

    @PostMapping("/team/{id}/documents")
    public ResponseEntity<?> addStaffDocument(
            @PathVariable Long id,
            @RequestBody Map<String, Object> req,
            @AuthenticationPrincipal User currentUser) {
        String actor = currentUser != null ? currentUser.getUsername() : "admin";
        return ResponseEntity.ok(staffService.addUserDocument(id, req, actor));
    }

    private Map<String, Object> createPerm(String key, String label, String module, String kind, String risk, boolean live, boolean adminOnly) {
        Map<String, Object> p = new HashMap<>();
        p.put("key", key);
        p.put("label", label);
        p.put("module", module);
        p.put("kind", kind);
        p.put("risk", risk);
        p.put("live", live);
        p.put("admin_only", adminOnly);
        return p;
    }

    private Map<String, Object> createWidget(String id, String title) {
        Map<String, Object> w = new HashMap<>();
        w.put("id", id);
        w.put("title", title);
        return w;
    }
}
