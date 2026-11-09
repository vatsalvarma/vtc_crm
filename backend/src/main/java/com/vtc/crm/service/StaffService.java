package com.vtc.crm.service;

import com.vtc.crm.entity.*;
import com.vtc.crm.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StaffService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserDocumentRepository docRepository;
    private final AuditLogRepository auditRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public StaffService(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            UserDocumentRepository docRepository,
            AuditLogRepository auditRepository,
            PasswordEncoder passwordEncoder,
            AuthService authService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.docRepository = docRepository;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public List<Map<String, Object>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> res = new ArrayList<>();
        for (User u : users) {
            res.add(authService.formatUserMap(u));
        }
        return res;
    }

    @Transactional
    public Map<String, Object> createUser(Map<String, Object> req, String actorUsername) {
        String username = (String) req.get("username");
        String fullName = (String) req.get("full_name");
        String role = (String) req.get("role");
        String loginType = (String) req.get("login_type");
        String startingSecret = (String) req.get("starting_secret");
        if (startingSecret == null && req.containsKey("secret")) {
            startingSecret = (String) req.get("secret");
        }

        if (role == null && req.containsKey("role_id")) {
            Object rId = req.get("role_id");
            if ("1".equals(rId.toString())) role = "Admin";
            else if ("2".equals(rId.toString())) role = "Manager";
            else role = "Staff";
        }

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("USERNAME_REQUIRED");
        }
        username = username.trim().toLowerCase();

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("USERNAME_TAKEN");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = username;
        }

        if (role == null || role.trim().isEmpty()) {
            role = "Staff";
        }

        if (loginType == null || (!"pin".equals(loginType) && !"password".equals(loginType))) {
            loginType = "Staff".equalsIgnoreCase(role) ? "pin" : "password";
        }

        User user = new User(username, fullName, role, loginType);
        String secret = (startingSecret != null && !startingSecret.trim().isEmpty())
                ? startingSecret.trim()
                : ("pin".equals(loginType) ? "1234" : "VTC-Welcome26");
        String encoded = passwordEncoder.encode(secret);
        user.setPinHash(encoded);
        user.setPasswordHash(encoded);

        user.setMustChangeSecret(false);
        user.setStatus("ACTIVE");
        user.setAvatarInitials(getInitials(fullName));

        if (req.get("reports_to_id") != null) {
            Long repId = Long.valueOf(req.get("reports_to_id").toString());
            userRepository.findById(repId).ifPresent(r -> {
                user.setReportsToId(r.getId());
                user.setReportsToName(r.getFullName());
            });
        }

        User saved = userRepository.save(user);

        // Auto-create empty profile
        profileRepository.save(new UserProfile(saved.getId()));

        auditRepository.save(new AuditLog(
                actorUsername != null ? actorUsername : "system",
                "ADD_STAFF",
                "USER",
                saved.getId().toString(),
                "Added " + saved.getFullName() + " (@" + saved.getUsername() + ") with role " + saved.getRole()
        ));

        return authService.formatUserMap(saved);
    }

    @Transactional
    public Map<String, Object> updateUser(Long id, Map<String, Object> req, String actorUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (req.containsKey("full_name")) user.setFullName((String) req.get("full_name"));
        if (req.containsKey("role")) user.setRole((String) req.get("role"));
        if (req.containsKey("role_id")) {
            Object rId = req.get("role_id");
            if ("1".equals(rId.toString())) user.setRole("Admin");
            else if ("2".equals(rId.toString())) user.setRole("Manager");
            else user.setRole("Staff");
        }
        if (req.containsKey("active")) {
            boolean act = Boolean.parseBoolean(req.get("active").toString());
            user.setStatus(act ? "ACTIVE" : "LOCKED");
        }
        if (req.containsKey("status")) user.setStatus((String) req.get("status"));
        if (req.containsKey("phone")) user.setPhone((String) req.get("phone"));
        if (req.containsKey("email")) user.setEmail((String) req.get("email"));

        if (req.containsKey("reports_to_id")) {
            Object repObj = req.get("reports_to_id");
            if (repObj == null || "".equals(repObj)) {
                user.setReportsToId(null);
                user.setReportsToName(null);
            } else {
                Long repId = Long.valueOf(repObj.toString());
                userRepository.findById(repId).ifPresent(r -> {
                    user.setReportsToId(r.getId());
                    user.setReportsToName(r.getFullName());
                });
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        auditRepository.save(new AuditLog(
                actorUsername != null ? actorUsername : "system",
                "UPDATE_STAFF",
                "USER",
                saved.getId().toString(),
                "Updated details for @" + saved.getUsername()
        ));

        return authService.formatUserMap(saved);
    }

    @Transactional
    public void deleteUser(Long id, String actorUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if ("owner".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("CANNOT_DELETE_OWNER");
        }

        user.setStatus("LOCKED");
        userRepository.save(user);

        auditRepository.save(new AuditLog(
                actorUsername != null ? actorUsername : "system",
                "DEACTIVATE_STAFF",
                "USER",
                user.getId().toString(),
                "Deactivated staff member @" + user.getUsername()
        ));
    }

    @Transactional
    public Map<String, Object> resetSecret(Long id, Map<String, Object> req, String actorUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        String newSecret = (String) req.get("new_secret");
        if (newSecret == null || newSecret.trim().isEmpty()) {
            newSecret = (String) req.get("secret");
        }
        if (newSecret == null || newSecret.trim().isEmpty()) {
            throw new RuntimeException("SECRET_REQUIRED");
        }

        String encoded = passwordEncoder.encode(newSecret.trim());
        user.setPinHash(encoded);
        user.setPasswordHash(encoded);

        user.setStatus("ACTIVE"); // Unlock on reset
        user.setMustChangeSecret(false);
        userRepository.save(user);

        auditRepository.save(new AuditLog(
                actorUsername != null ? actorUsername : "system",
                "RESET_SECRET",
                "USER",
                user.getId().toString(),
                "Reset " + user.getLoginType().toUpperCase() + " for @" + user.getUsername()
        ));

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("username", user.getUsername());
        return res;
    }

    public List<Map<String, Object>> getTeamHierarchy() {
        List<User> all = userRepository.findAll();
        List<Map<String, Object>> res = new ArrayList<>();
        for (User u : all) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", u.getId());
            node.put("full_name", u.getFullName());
            node.put("username", u.getUsername());
            node.put("role", u.getRole());
            node.put("status", u.getStatus());
            node.put("reports_to", u.getReportsToName() != null ? u.getReportsToName() : "—");
            node.put("reports_to_id", u.getReportsToId());
            res.add(node);
        }
        return res;
    }

    public Map<String, Object> getUserProfile(Long userId) {
        UserProfile p = profileRepository.findById(userId)
                .orElseGet(() -> profileRepository.save(new UserProfile(userId)));

        Map<String, Object> res = new HashMap<>();
        res.put("user_id", p.getUserId());
        res.put("emergency_contact", p.getEmergencyContact() != null ? p.getEmergencyContact() : "—");
        res.put("aadhaar_last4", p.getAadhaarLast4() != null ? p.getAadhaarLast4() : "—");
        res.put("pan_last4", p.getPanLast4() != null ? p.getPanLast4() : "—");
        res.put("bank_account_last4", p.getBankAccountLast4() != null ? p.getBankAccountLast4() : "—");
        res.put("ifsc", p.getIfsc() != null ? p.getIfsc() : "—");
        res.put("address", p.getAddress() != null ? p.getAddress() : "—");
        res.put("joining_date", p.getJoiningDate() != null ? p.getJoiningDate() : "—");
        return res;
    }

    @Transactional
    public Map<String, Object> updateUserProfile(Long userId, Map<String, Object> req, String actorUsername) {
        UserProfile p = profileRepository.findById(userId)
                .orElseGet(() -> new UserProfile(userId));

        if (req.containsKey("emergency_contact")) p.setEmergencyContact((String) req.get("emergency_contact"));
        if (req.containsKey("aadhaar_last4")) p.setAadhaarLast4((String) req.get("aadhaar_last4"));
        if (req.containsKey("pan_last4")) p.setPanLast4((String) req.get("pan_last4"));
        if (req.containsKey("bank_account_last4")) p.setBankAccountLast4((String) req.get("bank_account_last4"));
        if (req.containsKey("ifsc")) p.setIfsc((String) req.get("ifsc"));
        if (req.containsKey("address")) p.setAddress((String) req.get("address"));
        if (req.containsKey("joining_date")) p.setJoiningDate((String) req.get("joining_date"));

        p.setUpdatedAt(LocalDateTime.now());
        UserProfile saved = profileRepository.save(p);

        auditRepository.save(new AuditLog(
                actorUsername != null ? actorUsername : "system",
                "UPDATE_PROFILE",
                "USER_PROFILE",
                userId.toString(),
                "Updated personal / banking profile for staff ID " + userId
        ));

        return getUserProfile(saved.getUserId());
    }

    public List<UserDocument> getUserDocuments(Long userId) {
        return docRepository.findByUserId(userId);
    }

    @Transactional
    public UserDocument addUserDocument(Long userId, Map<String, Object> req, String actorUsername) {
        String docType = (String) req.get("doc_type");
        String fileName = (String) req.get("file_name");
        UserDocument doc = new UserDocument(userId, docType != null ? docType : "Document", fileName != null ? fileName : "upload.pdf");
        UserDocument saved = docRepository.save(doc);

        auditRepository.save(new AuditLog(
                actorUsername != null ? actorUsername : "system",
                "UPLOAD_DOCUMENT",
                "DOCUMENT",
                saved.getId().toString(),
                "Uploaded " + saved.getDocType() + " for staff ID " + userId
        ));

        return saved;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
