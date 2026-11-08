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
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            UserProfileRepository profileRepository,
            UserDocumentRepository docRepository,
            MasterRecordRepository masterRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.docRepository = docRepository;
        this.masterRepository = masterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
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
}

