package com.vtc.crm.service;

import com.vtc.crm.entity.*;
import com.vtc.crm.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserDocumentRepository docRepository;
    private final MasterRecordRepository masterRepository;
    private final TaskRepository taskRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final InwardReceiptRepository inwardReceiptRepository;
    private final InventoryLotRepository inventoryLotRepository;
    private final RoastBatchRepository roastRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            UserDocumentRepository docRepository,
            MasterRecordRepository masterRepository,
            TaskRepository taskRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            InwardReceiptRepository inwardReceiptRepository,
            InventoryLotRepository inventoryLotRepository,
            RoastBatchRepository roastRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.docRepository = docRepository;
        this.masterRepository = masterRepository;
        this.taskRepository = taskRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.inwardReceiptRepository = inwardReceiptRepository;
        this.inventoryLotRepository = inventoryLotRepository;
        this.roastRepository = roastRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsersAndMasters();
        seedTasks();
        seedComms();
        seedInwardAndInventory();
        seedRoastBatches();
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

    private void seedComms() {
        if (conversationRepository.count() > 0) return;

        User owner = userRepository.findByUsername("owner").orElse(null);
        String ownerName = owner != null ? owner.getFullName() : "Owner";
        Long ownerId = owner != null ? owner.getId() : 1L;

        // 1. Announcements
        Conversation ann = new Conversation("Announcements", "announcement", "*");
        ann.setLastMessageAuthor(ownerName);
        ann.setLastMessageBody("Stock count this Saturday at 8 am. Everyone attend.");
        ann.setLastMessageAt(LocalDateTime.now().minusHours(2));
        Conversation savedAnn = conversationRepository.save(ann);

        Message m1 = new Message(
                savedAnn.getId(),
                ownerId,
                ownerName,
                "Admin",
                "Stock count this Saturday at 8 am. Everyone attend.",
                "all"
        );
        m1.setCreatedAt(LocalDateTime.now().minusHours(2));
        messageRepository.save(m1);

        // 2. Office / Accounts
        Conversation grp1 = new Conversation("Office / Accounts", "group", "1,2,3");
        grp1.setLastMessageAuthor(ownerName);
        grp1.setLastMessageBody("Welcome to the Office & Accounts team channel.");
        grp1.setLastMessageAt(LocalDateTime.now().minusHours(1));
        Conversation savedGrp1 = conversationRepository.save(grp1);

        Message m2 = new Message(
                savedGrp1.getId(),
                ownerId,
                ownerName,
                "Admin",
                "Welcome to the Office & Accounts team channel.",
                "all"
        );
        m2.setCreatedAt(LocalDateTime.now().minusHours(1));
        messageRepository.save(m2);

        // 3. Roastery & Main Store
        Conversation grp2 = new Conversation("Roastery & Main Store", "group", "1,3,4,5");
        grp2.setLastMessageAuthor("Ramesh (Roaster)");
        grp2.setLastMessageBody("Roaster is warmed up and ready for batch 1.");
        grp2.setLastMessageAt(LocalDateTime.now().minusMinutes(30));
        Conversation savedGrp2 = conversationRepository.save(grp2);

        Message m3 = new Message(
                savedGrp2.getId(),
                4L,
                "Ramesh (Roaster)",
                "Staff",
                "Roaster is warmed up and ready for batch 1.",
                "all"
        );
        m3.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        messageRepository.save(m3);
    }

    private void seedInwardAndInventory() {
        if (inwardReceiptRepository.count() > 0) return;

        // Ensure products have nominalKg set
        masterRepository.findByKind("PRODUCT").forEach(p -> {
            if (p.getNominalKg() == null) {
                p.setNominalKg("BEAN-003".equalsIgnoreCase(p.getCode()) ? 50.0 : 60.0);
                masterRepository.save(p);
            }
        });

        MasterRecord arabica = masterRepository.findByKind("PRODUCT").stream()
                .filter(p -> "BEAN-001".equalsIgnoreCase(p.getCode()))
                .findFirst()
                .orElse(null);

        Long arabicaId = arabica != null ? arabica.getId() : 4L;
        String arabicaName = arabica != null ? arabica.getName() : "Arabica Plantation A";
        double perBag = arabica != null && arabica.getNominalKg() != null ? arabica.getNominalKg() : 60.0;

        // Seed 1: IN-261027-01
        InwardReceipt r1 = new InwardReceipt();
        r1.setCode("IN-261027-01");
        r1.setSource("AUCTION");
        r1.setSourceName("Coorg Plantation Estates");
        r1.setProductId(arabicaId);
        r1.setProduct(arabicaName);
        r1.setBags(10);
        r1.setDamaged(1);
        r1.setPartial(1);
        r1.setPartialKg(35.0);
        r1.setNominalKg((8 * perBag) + (1 * perBag) + 35.0); // 480 + 60 + 35 = 575 kg
        r1.setAllocatableKg((8 * perBag) + 35.0); // 515 kg
        r1.setQuarantineKg(1 * perBag); // 60 kg
        r1.setInvoiceStatus("MATCHED");
        r1.setPaymentStatus("PAID");
        r1.setCreatedBy("Demo Manager");
        r1.setCreatedAt(LocalDateTime.now().minusDays(3));
        InwardReceipt savedR1 = inwardReceiptRepository.save(r1);

        // Sound Lot
        InventoryLot lotSound = new InventoryLot();
        lotSound.setCode("LOT-" + savedR1.getCode() + "-S");
        lotSound.setReceiptId(savedR1.getId());
        lotSound.setReceiptCode(savedR1.getCode());
        lotSound.setProductId(arabicaId);
        lotSound.setProduct(arabicaName);
        lotSound.setName(arabicaName);
        lotSound.setStage("RAW");
        lotSound.setLocation("PROD-01");
        lotSound.setOwner("VTC");
        lotSound.setStatus("RELEASED");
        lotSound.setBagKind("SOUND");
        lotSound.setBags(8);
        lotSound.setKg(8 * perBag);
        lotSound.setAvailableKg(8 * perBag);
        lotSound.setNominalKg(perBag);
        lotSound.setCreatedAt(LocalDateTime.now().minusDays(3));
        inventoryLotRepository.save(lotSound);

        // Damaged Lot
        InventoryLot lotDamaged = new InventoryLot();
        lotDamaged.setCode("LOT-" + savedR1.getCode() + "-D");
        lotDamaged.setReceiptId(savedR1.getId());
        lotDamaged.setReceiptCode(savedR1.getCode());
        lotDamaged.setProductId(arabicaId);
        lotDamaged.setProduct(arabicaName);
        lotDamaged.setName(arabicaName);
        lotDamaged.setStage("RAW");
        lotDamaged.setLocation("PROD-01");
        lotDamaged.setOwner("VTC");
        lotDamaged.setStatus("QUARANTINE");
        lotDamaged.setBagKind("DAMAGED");
        lotDamaged.setBags(1);
        lotDamaged.setKg(1 * perBag);
        lotDamaged.setAvailableKg(0.0);
        lotDamaged.setNominalKg(perBag);
        lotDamaged.setCreatedAt(LocalDateTime.now().minusDays(3));
        inventoryLotRepository.save(lotDamaged);

        // Partial Lot
        InventoryLot lotPartial = new InventoryLot();
        lotPartial.setCode("LOT-" + savedR1.getCode() + "-P");
        lotPartial.setReceiptId(savedR1.getId());
        lotPartial.setReceiptCode(savedR1.getCode());
        lotPartial.setProductId(arabicaId);
        lotPartial.setProduct(arabicaName);
        lotPartial.setName(arabicaName);
        lotPartial.setStage("RAW");
        lotPartial.setLocation("PROD-01");
        lotPartial.setOwner("VTC");
        lotPartial.setStatus("RELEASED");
        lotPartial.setBagKind("PARTIAL");
        lotPartial.setBags(1);
        lotPartial.setKg(35.0);
        lotPartial.setAvailableKg(35.0);
        lotPartial.setNominalKg(perBag);
        lotPartial.setCreatedAt(LocalDateTime.now().minusDays(3));
        inventoryLotRepository.save(lotPartial);
    }

    private void seedRoastBatches() {
        if (roastRepository.countByQcStatus("QC_HOLD") > 0) return;

        // Seed 1: RST-ROBCAB-261027-03 waiting on QC HOLD (as shown in reference image)
        RoastBatch holdBatch = new RoastBatch();
        holdBatch.setBatchCode("RST-ROBCAB-261027-03");
        holdBatch.setGreenKg(30.0);
        holdBatch.setRoastedKg(24.1);
        holdBatch.setLossPercent(19.67);
        holdBatch.setDurationMin(14);
        holdBatch.setProfileName("Dark Roast (Espresso / South Indian Filter)");
        holdBatch.setRoasterName("Ramesh (Roaster)");
        holdBatch.setQcStatus("QC_HOLD");
        holdBatch.setAbnormal(true);
        holdBatch.setInputsSummary("Robusta Cherry AB (30.0 kg)");
        holdBatch.setOperatorNotes("Temperature spiked slightly in finishing phase");
        holdBatch.setCreatedAt(LocalDateTime.now().minusHours(2));

        // Create the corresponding lot in inventory on QC_HOLD
        InventoryLot lot = new InventoryLot();
        lot.setCode("LOT-RST-ROBCAB-261027-03");
        lot.setProduct("Robusta Cherry AB (Roasted)");
        lot.setName("Robusta Cherry AB (Roasted)");
        lot.setStage("ROASTED");
        lot.setLocation("PROD-01");
        lot.setOwner("VTC");
        lot.setStatus("QC_HOLD");
        lot.setBagKind("SOUND");
        lot.setBags(1);
        lot.setKg(24.1);
        lot.setAvailableKg(0.0);
        lot.setResting(true);
        lot.setReadyFrom(java.time.LocalDate.now().plusDays(1));
        lot.setCreatedAt(LocalDateTime.now().minusHours(2));
        InventoryLot savedLot = inventoryLotRepository.save(lot);

        holdBatch.setOutputLotId(savedLot.getId());
        holdBatch.setOutputLotCode(savedLot.getCode());
        roastRepository.save(holdBatch);

        // Seed 2: RST-ARAPAS-261027-02 (RELEASED)
        if (roastRepository.findAll().stream().noneMatch(b -> "RST-ARAPAS-261027-02".equalsIgnoreCase(b.getBatchCode()))) {
            RoastBatch b2 = new RoastBatch();
            b2.setBatchCode("RST-ARAPAS-261027-02");
            b2.setGreenKg(60.0);
            b2.setRoastedKg(50.6);
            b2.setLossPercent(15.67);
            b2.setDurationMin(13);
            b2.setProfileName("Medium Roast (Standard Filter)");
            b2.setRoasterName("Ramesh (Roaster)");
            b2.setQcStatus("RELEASED");
            b2.setAbnormal(false);
            b2.setInputsSummary("Arabica Plantation A (60.0 kg)");
            b2.setCreatedAt(LocalDateTime.now().minusDays(1));
            roastRepository.save(b2);
        }

        // Seed 3: RST-ROBCAB-261027-01 (RELEASED)
        if (roastRepository.findAll().stream().noneMatch(b -> "RST-ROBCAB-261027-01".equalsIgnoreCase(b.getBatchCode()))) {
            RoastBatch b3 = new RoastBatch();
            b3.setBatchCode("RST-ROBCAB-261027-01");
            b3.setGreenKg(120.0);
            b3.setRoastedKg(101.0);
            b3.setLossPercent(15.83);
            b3.setDurationMin(15);
            b3.setProfileName("Dark Roast (Espresso)");
            b3.setRoasterName("Ramesh (Roaster)");
            b3.setQcStatus("RELEASED");
            b3.setAbnormal(false);
            b3.setInputsSummary("Robusta Cherry AB (120.0 kg)");
            b3.setCreatedAt(LocalDateTime.now().minusDays(2));
            roastRepository.save(b3);
        }
    }
}

