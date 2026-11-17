# Vasavi Trading Company (VTC) CRM & ERP — System Design Document

**System Version:** 2.0.0 (Production Architecture)  
**Author:** Vasavi Trading Company Engineering  
**Target Environment:** VPS Deployment with Docker & Subdomain Routing  
**Primary Domain:** `https://crm.teancoffeeindia.com` (Internal Port `8088`)  

---

## 1. Executive Summary & Business Scope

Vasavi Trading Company (VTC) operates a commercial coffee processing, roasting, wholesale merchant, and counter sales facility. The VTC CRM & ERP application is an end-to-end operational operating system designed to eliminate manual paper registers, prevent inventory shrinkage, automate quality control, and enforce strict separation of duties across cash and inventory.

### Key Operational Capabilities
1. **Stock Inward:** Raw green coffee receipt, gate entries, gross/tare/net scale weighing, moisture/defect analysis, lot generation.
2. **Inventory Management:** Real-time stock ledgers across 4 stages (`GREEN`, `ROASTED`, `BLENDED`, `PACKED`) and 4 locations (`PROD-01`, `CTR-01`, `IN_TRANSIT`, `SUPPLIER`).
3. **Daily Roasting:** Batch roasting execution, automatic green bean lot deduction, roast loss % calculation, profile temperature tracking, cooling, and QC hold handover.
4. **Quality Control (QC):** Administrative approval/rejection gates preventing unapproved roasted batches from entering packaging or sales.
5. **Blending & Grinding:** Multi-origin formulations (Arabica/Robusta/Chicory), scale weight confirmation, grinding burr settings.
6. **Packaging:** Packaging ticket queues, target pack execution, run recording, good packs vs rejects reconciliation.
7. **Counter Sales (POS):** Fast counter checkout, loose coffee weighing, custom blend calculations, cash drawer float tracking, and ESC/POS thermal receipt printing on ATPOS AT 402 printers.
8. **Outside Roasting (Job-Work):** Customer-supplied coffee roasting, weight loss tracking, and service billing.
9. **Outgoing Sales & Dispatch:** B2B orders, credit approvals, multi-trip dispatches (Rapido, Uber, vehicle), digital delivery confirmation & POD.
10. **Finance & Tally:** Receivables aging (0–30, 31–60, 60+ days), collections verification, supplier payables matching, and TallyPrime XML/CSV export.
11. **Internal Comms:** Live bi-directional chat, team announcements, voice notes, and WebSocket broadcasts.
12. **Maintenance:** Machine and facility repair tickets, urgency tagging, and resolution tracking.

---

## 2. High-Level Architecture Diagram

```mermaid
flowchart TD
    subgraph Clients["1. Client Devices & Hardware"]
        Cashier["💻 Cash Counter POS (Chrome / Edge)"]
        Tablet["📱 Roastery Tablet (Operator)"]
        Owner["📱 Owner / Manager Mobile"]
        Printer["🖨️ ATPOS AT 402 Thermal Printer (80mm ESC/POS)"]
    end

    subgraph Internet["2. Public Internet & DNS"]
        DNS["GoDaddy / ShriCloud DNS\n(crm.teancoffeeindia.com -> 104.234.179.69)"]
    end

    subgraph VPS["3. VPS Host (Ubuntu 22.04 LTS - IP: 104.234.179.69)"]
        HostNginx["Host Nginx Reverse Proxy\n(Ports 80 & 443 with Let's Encrypt SSL)"]
        ExistingApp["Existing Website\n(teancoffeeindia.com :80/:443)"]
        
        subgraph DockerNet["Docker Bridge Network (vtc-net)"]
            subgraph FE["Container: vtc-crm-frontend (:8088 -> :80)"]
                FENginx["Nginx Web Server"]
                StaticFiles["SPA Assets (HTML5, JS, CSS, Lucide Icons)"]
                APIProxy["Reverse Proxy (/api/ -> backend:8080, /ws/ -> backend:8080)"]
            end

            subgraph BE["Container: vtc-crm-backend (:8080)"]
                Security["Spring Security (Stateless JWT Filter)"]
                REST["Spring REST Controllers (56+ Endpoints)"]
                WS["Spring WebSocket Handler (/ws/comms)"]
                Business["Service Layer (Business Logic & Formulas)"]
                JPA["Spring Data JPA (Hibernate 6)"]
            end

            subgraph DB["Container: vtc-crm-db (:3306)"]
                MySQL[("MySQL 8.0 Engine\n(Persistent Volume: vtc_db_data)")]
            end
        end
    end

    Clients -->|"DNS Query"| DNS
    Clients -->|"HTTPS (Port 443)"| HostNginx
    Cashier -.->|"USB / Bluetooth ESC/POS"| Printer

    HostNginx -->|"Host: teancoffeeindia.com"| ExistingApp
    HostNginx -->|"Host: crm.teancoffeeindia.com\n(proxy_pass http://127.0.0.1:8088)"| FENginx

    FENginx -->|"Static Requests (/)"| StaticFiles
    FENginx -->|"API Requests (/api/v1/)"| REST
    FENginx -->|"Realtime (/ws/comms)"| WS

    REST --> Security --> Business --> JPA
    WS --> Business
    JPA -->|"TCP Port 3306"| MySQL
```

---

## 3. Coffee Production & Inventory State Machine

The core business follows a strict linear state machine to maintain mathematical reconciliation of all inventory weights:

```mermaid
flowchart TD
    Inward["1. Stock Inward\n(Goods Receipt Note)\nSupplier, Gross/Tare/Net Kg, Bags"]
    GreenLot["2. Green Inventory Lot\n(Code: e.g. LOT-ARB-2611-01)\nStatus: RELEASED, Stage: GREEN"]
    
    Roast["3. Daily Roasting\nDeduct Green Lot Kg\nInput Kg -> Output Kg\nCalculate Loss %"]
    QCHold{"4. QC State Gate\nStatus: QC_HOLD\n(Output Lot Created)"}
    
    AdminReject["QC Rejected\nStatus: REJECTED\nAvailable Kg: 0"]
    AdminApprove["QC Approved by Admin\nStatus: RELEASED\nAvailable Kg: Output Kg"]

    Blend["5. Blending & Grinding\nFormula: Arabica + Robusta + Chicory\nStage: BLENDED / GROUND"]
    PackTicket["6. Packaging Ticket\nQueue Target Packs (e.g. 40 x 500g)"]
    PackRun["7. Packaging Run\nGood Packs + Rejects Recorded\nStage: PACKED"]

    POS["8A. Counter POS Sales\nLoose / Retail Packs\nATPOS Thermal Receipt"]
    B2B["8B. Wholesale Outgoing Order\nReserved -> Dispatched -> Delivered"]

    Inward --> GreenLot
    GreenLot --> Roast
    Roast --> QCHold
    QCHold -->|"Admin Rejects"| AdminReject
    QCHold -->|"Admin Approves"| AdminApprove
    AdminApprove --> Blend
    AdminApprove --> PackTicket
    Blend --> PackTicket
    PackTicket --> PackRun
    PackRun --> POS
    PackRun --> B2B
```

---

## 4. Database Entity-Relationship (ER) Architecture

The backend database runs on MySQL 8.0, composed of 18 relational tables managed via Hibernate:

```mermaid
erDiagram
    USERS ||--o{ TASKS : "assigned_to"
    USERS ||--o{ AUDIT_LOGS : "performed_by"
    USERS ||--o{ ROAST_BATCHES : "roasted_by"
    USERS ||--o{ MAINTENANCE_TICKETS : "raised_by"
    
    INWARD_RECEIPTS ||--|{ INVENTORY_LOTS : "creates"
    INVENTORY_LOTS ||--o{ ROAST_BATCHES : "source_lot"
    ROAST_BATCHES ||--o{ INVENTORY_LOTS : "output_lot"
    
    PACKING_TICKETS ||--o{ INVENTORY_LOTS : "consumes_source_lot"
    
    OUTGOING_ORDERS ||--o{ ORDER_DISPATCHES : "has_trips"
    OUTGOING_ORDERS ||--o{ CLIENT_COLLECTIONS : "settled_by"
    
    CONVERSATIONS ||--|{ MESSAGES : "contains"
    
    USERS {
        bigint id PK
        string username UK
        string full_name
        string role
        string tier
        string password_hash
        string pin_hash
        string status
        bigint reports_to_id
    }

    INWARD_RECEIPTS {
        bigint id PK
        string receipt_no UK
        string source_name
        string vehicle_no
        double gross_weight_kg
        double tare_weight_kg
        double net_weight_kg
        int bags
        double moisture_pct
        string state
        datetime created_at
    }

    INVENTORY_LOTS {
        bigint id PK
        string code UK
        string product_name
        string stage
        double kg
        double available_kg
        string status
        string location
        bigint receipt_id FK
    }

    ROAST_BATCHES {
        bigint id PK
        string batch_code UK
        bigint source_lot_id FK
        double input_weight_kg
        double output_weight_kg
        double loss_pct
        string roast_profile
        string qc_status
        bigint output_lot_id FK
        datetime created_at
    }

    PACKING_TICKETS {
        bigint id PK
        string product_name
        string demand_type
        int target_packs
        int grams_per_pack
        int done_packs
        int rejected_packs
        double coffee_used_kg
        string state
    }

    OUTGOING_ORDERS {
        bigint id PK
        bigint customer_id
        string customer
        string item
        string form
        double quantity_kg
        double reserved_kg
        double dispatched_kg
        bigint price_per_kg_paise
        int gst_rate_bp
        string state
    }

    ORDER_DISPATCHES {
        bigint id PK
        bigint order_id FK
        double kg
        string mode
        string booking_ref
        string handover_to
        datetime dispatched_at
        datetime delivered_at
        string pod_note
    }

    MAINTENANCE_TICKETS {
        bigint id PK
        string title
        string area
        string urgency
        string status
        string details
        string resolution
        bigint raised_by_id FK
        bigint assigned_to_id FK
    }

    CONVERSATIONS {
        bigint id PK
        string title
        string kind
        string audience
        string last_message_body
        datetime last_message_at
    }

    MESSAGES {
        bigint id PK
        bigint conversation_id FK
        bigint sender_id FK
        string sender_name
        text body
        datetime sent_at
    }
```

---

## 5. Security & Authentication Architecture

```mermaid
sequenceDiagram
    autonumber
    actor User as Client Browser (Axios)
    participant Nginx as Frontend Nginx (:8088)
    participant JwtFilter as JwtAuthenticationFilter
    participant SpringSec as SecurityFilterChain
    participant AuthCtrl as AuthController / Service
    participant DB as MySQL DB

    User->>Nginx: POST /api/v1/login { username, password/PIN }
    Nginx->>AuthCtrl: Proxy to Backend :8080
    AuthCtrl->>DB: Query User by username
    DB-->>AuthCtrl: User record with BCrypt hashes
    Note over AuthCtrl: Validates Password or PIN with BCrypt<br/>Generates JWT Token (HS256)
    AuthCtrl-->>User: 200 OK + JWT Token + User Profile & Permissions

    Note over User: Subsequent Requests include:<br/>Authorization: Bearer <JWT_TOKEN>
    User->>Nginx: GET /api/v1/roasts
    Nginx->>JwtFilter: Intercepts Request
    Note over JwtFilter: 1. Extracts Bearer token<br/>2. Validates signature with JWT_SECRET<br/>3. Extracts claims: userId, role, tier<br/>4. Populates SecurityContextHolder
    JwtFilter->>SpringSec: Passes through
    SpringSec->>DB: Executes query via JPA
    DB-->>User: 200 OK JSON Data
```

---

## 6. Real-Time WebSocket Communication Architecture

The system features real-time messaging across teams without polling:

```mermaid
flowchart LR
    Sender["Client A (Sender)"]
    WSHandler["ChatWebSocketHandler\n(ConcurrentHashMap<SessionId, WebSocketSession>)"]
    DB[("MySQL Database\n(vtc_messages)")]
    Receiver1["Client B (Receiver)"]
    Receiver2["Client C (Receiver)"]

    Sender -->|"1. ws://crm.../ws/comms (JSON: message)"| WSHandler
    WSHandler -->|"2. Save message & update last_message"| DB
    WSHandler -->|"3. Broadcast JSON payload"| Receiver1
    WSHandler -->|"3. Broadcast JSON payload"| Receiver2
```

---

## 7. Thermal Receipt Printing Subsystem (ATPOS AT 402)

* **Physical Paper Width:** `79.5 ± 0.5 mm` (Standard 80mm roll).
* **Printable Area:** `72.0 mm` (576 dots / 40 monospace characters per line).
* **Driver Architecture:** Dual-mode implementation:
  1. **Direct Web Bluetooth ESC/POS:** Streams binary commands directly to the thermal print head (`\x1b\x40` reset, `\x1b\x21` styling, `\x1d\x56\x42\x00` paper auto-cut).
  2. **High-Precision Browser CSS Print:** Standardized `@page { size: 80mm auto; margin: 0; }` with a 12mm trailing footer padding to protect the receipt text from being clipped by the automatic paper guillotine cutter.
