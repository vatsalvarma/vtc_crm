package com.vtc.crm.service;

import com.vtc.crm.entity.*;
import com.vtc.crm.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserDocumentRepository docRepository;
    private final MasterRecordRepository masterRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            UserDocumentRepository docRepository,
            MasterRecordRepository masterRepository,
            TaskRepository taskRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.docRepository = docRepository;
        this.masterRepository = masterRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsersAndMasters();
        seedTasks();
    }

    private void seedUsersAndMasters() {
        if (userRepository.count() > 0) return;

        // 1. Owner
        User owner = new User("owner", "Owner", "Admin", "password");
        owner.setPasswordHash(passwordEncoder.encode("admin123"));
        owner.setPinHash(passwordEncoder.encode("4828"));
        owner.setAvatarInitials("O");
        owner.setPhone("9849012345");
        owner.setEmail("owner@vasavitrading.com");
        owner.setStatus("ACTIVE");
        userRepository.save(owner);

        // 2. Office Admin
        User admin = new User("admin", "Office Admin", "Admin", "password");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setAvatarInitials("OA");
        admin.setReportsToId(owner.getId());
        admin.setReportsToName("Owner");
        admin.setStatus("ACTIVE");
        userRepository.save(admin);

        // 3. Manager
        User manager = new User("manager", "Manager", "Manager", "password");
        manager.setPasswordHash(passwordEncoder.encode("admin123"));
        manager.setAvatarInitials("M");
        manager.setReportsToId(owner.getId());
        manager.setReportsToName("Owner");
        manager.setStatus("ACTIVE");
        userRepository.save(manager);

        // 4. Roaster (PIN Login)
        User roaster = new User("roaster", "Ramesh (Roaster)", "Staff", "pin");
        roaster.setPinHash(passwordEncoder.encode("7580"));
        roaster.setAvatarInitials("RR");
        roaster.setReportsToId(manager.getId());
        roaster.setReportsToName("Manager");
        roaster.setStatus("ACTIVE");
        userRepository.save(roaster);

        // 5. Packer (PIN Login)
        User packer = new User("packer", "Prakash (Packer)", "Staff", "pin");
        packer.setPinHash(passwordEncoder.encode("1470"));
        packer.setAvatarInitials("PP");
        packer.setReportsToId(manager.getId());
        packer.setReportsToName("Manager");
        packer.setStatus("ACTIVE");
        userRepository.save(packer);

        // Profiles
        UserProfile roasterProfile = new UserProfile(roaster.getId());
        roasterProfile.setEmergencyContact("9888877777 (Brother)");
        roasterProfile.setAadhaarLast4("4821");
        roasterProfile.setPanLast4("789C");
        roasterProfile.setBankAccountLast4("9012");
        roasterProfile.setIfsc("SBIN0001234");
        roasterProfile.setAddress("Door 12-4, Coffee Board Colony, Araku Valley");
        roasterProfile.setJoiningDate("15 Jan 2024");
        profileRepository.save(roasterProfile);

        UserProfile packerProfile = new UserProfile(packer.getId());
        packerProfile.setEmergencyContact("9777766666 (Father)");
        packerProfile.setAadhaarLast4("1122");
        packerProfile.setPanLast4("334D");
        packerProfile.setBankAccountLast4("5566");
        packerProfile.setIfsc("HDFC0004321");
        packerProfile.setAddress("Plot 8, Industrial Estate, Visakhapatnam");
        packerProfile.setJoiningDate("01 Mar 2024");
        profileRepository.save(packerProfile);

        // Documents
        docRepository.save(new UserDocument(roaster.getId(), "Aadhaar Card", "aadhaar_roaster_verified.pdf"));
        docRepository.save(new UserDocument(roaster.getId(), "Employment Agreement", "employment_contract_2024.pdf"));
        docRepository.save(new UserDocument(packer.getId(), "Aadhaar Card", "aadhaar_packer_verified.pdf"));

        // Seed Master Records
        masterRepository.save(new MasterRecord("CUSTOMER", "CUST-001", "Sample Café, Banjara Hills", "ACTIVE"));
        masterRepository.save(new MasterRecord("CUSTOMER", "CUST-002", "Araku Valley Coffee House", "ACTIVE"));
        masterRepository.save(new MasterRecord("SUPPLIER", "SUP-001", "Coorg Plantation Estates", "ACTIVE"));
        masterRepository.save(new MasterRecord("PRODUCT", "BEAN-001", "Arabica Plantation A", "ACTIVE"));
        masterRepository.save(new MasterRecord("PRODUCT", "BEAN-002", "Robusta Cherry AB", "ACTIVE"));
        masterRepository.save(new MasterRecord("PRODUCT", "BEAN-003", "Chicory Premium Roasted", "ACTIVE"));
        masterRepository.save(new MasterRecord("PRODUCT", "BEAN-004", "Peaberry Special Reserve", "ACTIVE"));
    }

    private void seedTasks() {
        if (taskRepository.count() > 0) return;

        User owner = userRepository.findByUsername("owner").orElse(null);
        User roaster = userRepository.findByUsername("roaster").orElse(null);
        User packer = userRepository.findByUsername("packer").orElse(null);

        String ownerName = owner != null ? owner.getFullName() : "Owner";
        Long ownerId = owner != null ? owner.getId() : 1L;

        // 1. Pack 12 packs – ticket #1 Filter blend
        Task t1 = new Task();
        t1.setTitle("Pack 12 packs – ticket #1 Filter blend");
        t1.setKind("PACK");
        t1.setKindLabel("Packing");
        t1.setUnit("packs");
        t1.setTarget(12.0);
        t1.setProgress(12.0);
        t1.setStatus("SUBMITTED");
        t1.setSubmitNote("12 packs done, 1 rejected");
        t1.setDue("2026-11-10");
        t1.setAssignedBy(ownerName);
        t1.setAssignedById(ownerId);
        if (packer != null) {
            t1.setAssignedTo(packer.getId());
            t1.setAssignee(packer.getFullName());
        } else {
            t1.setAssignee("Prakash (Packer)");
        }
        taskRepository.save(t1);

        // 2. Roast 3 batches
        Task t2 = new Task();
        t2.setTitle("Roast 3 batches");
        t2.setKind("ROAST");
        t2.setKindLabel("Roasting");
        t2.setUnit("batches");
        t2.setTarget(3.0);
        t2.setProgress(3.0);
        t2.setStatus("IN_PROGRESS");
        t2.setDue("2026-11-10");
        t2.setAssignedBy(ownerName);
        t2.setAssignedById(ownerId);
        if (roaster != null) {
            t2.setAssignedTo(roaster.getId());
            t2.setAssignee(roaster.getFullName());
        } else {
            t2.setAssignee("Ramesh (Roaster)");
        }
        taskRepository.save(t2);

        // 3. Handle 10 counter bills
        Task t3 = new Task();
        t3.setTitle("Handle 10 counter bills");
        t3.setKind("COUNTER");
        t3.setKindLabel("Counter Sales");
        t3.setUnit("bills");
        t3.setTarget(10.0);
        t3.setProgress(0.0);
        t3.setStatus("OPEN");
        t3.setDue("2026-11-10");
        t3.setAssignedBy(ownerName);
        t3.setAssignedById(ownerId);
        if (roaster != null) {
            t3.setAssignedTo(roaster.getId());
            t3.setAssignee(roaster.getFullName());
        } else {
            t3.setAssignee("Ramesh (Roaster)");
        }
        taskRepository.save(t3);

        // 4. Clean roaster chaff collector
        Task t4 = new Task();
        t4.setTitle("Clean roaster chaff collector");
        t4.setKind("GENERAL");
        t4.setKindLabel("General");
        t4.setUnit("");
        t4.setTarget(null);
        t4.setProgress(0.0);
        t4.setStatus("OPEN");
        t4.setDue("2026-11-10");
        t4.setAssignedBy(ownerName);
        t4.setAssignedById(ownerId);
        if (roaster != null) {
            t4.setAssignedTo(roaster.getId());
            t4.setAssignee(roaster.getFullName());
        } else {
            t4.setAssignee("Ramesh (Roaster)");
        }
        taskRepository.save(t4);
    }
}

