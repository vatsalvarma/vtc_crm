// In-browser mock database & seed store for VTC Portal
// Matches exact sample data from seed.py & Claude artifact preview (P4i73q3tPkVueKokaAoiY3)

export interface User {
  id: number;
  username: string;
  full_name: string;
  role_name: string;
  tier: "management" | "staff";
  is_owner: boolean;
  pin?: string;
  password?: string;
  must_change_secret?: boolean;
  prefs: {
    lang: string;
    large_text: boolean;
    shortcuts: string[];
    widgets: string[];
    welcome?: string;
  };
  perms: string[];
  modules: string[];
  live_modules: string[];
  settings: Record<string, any>;
}

export const ALL_MODULES = [
  "dashboard", "tasks", "inward", "inventory", "roasting",
  "blending", "qc", "outside", "packing", "counter",
  "orders", "dispatch", "comms", "maintenance", "team",
  "finance", "masters", "exceptions", "reports", "admin"
];

export const ALL_PERMISSIONS = [
  "dashboard.my_view", "dashboard.team_view",
  "tasks.view_own", "tasks.update_own", "tasks.view_all", "tasks.assign", "tasks.update_any", "tasks.approve",
  "goods.view", "goods.receive", "goods.reverse", "invoice.metadata", "invoice.reconcile", "invoice.file", "invoice.cost",
  "supplier.pay.status", "supplier.pay.record", "supplier.pay.approve", "supplier.pay.amount", "supplier.pay.proof",
  "inventory.view", "inventory.cards", "inventory.transfer", "stock.cycle_count", "stock.cycle_approve",
  "inventory.adjust", "inventory.approve_adjust", "inventory.verify_quarantine",
  "roast.view", "roast.execute", "roast.qc_release",
  "recipe.view_public", "recipe.view_secret", "recipe.execute_secret", "recipe.edit",
  "grind.execute", "qc.view", "qc.configure", "outside.view", "outside.execute",
  "pack.view", "pack.create", "pack.execute", "pack.qa_approve",
  "counter.sell", "counter.close", "counter.close_approve", "counter.reopen_day", "counter.refund", "counter.price_override", "counter.discount", "counter.customers", "counter.insights",
  "outgoing.view", "outgoing.create", "outgoing.credit.request", "outgoing.credit.approve", "outgoing.price_override",
  "dispatch.handover", "documents.price_hidden", "documents.commercial",
  "customer.contacts", "comms.read", "comms.chat", "comms.announce", "comms.oversight",
  "maintenance.raise", "maintenance.manage",
  "team.view", "team.profiles", "team.hierarchy",
  "finance.view", "finance.collection.record", "finance.collection.verify", "finance.export", "finance.margin",
  "masters.view", "masters.view_drafts", "masters.create_draft", "masters.approve",
  "exceptions.view", "reports.operational", "reports.commercial",
  "system.users", "system.roles", "system.grants", "system.settings", "system.preview_as", "system.audit"
];

export const DEFAULT_SETTINGS = {
  company_name: "Vasavi Trading Company",
  short_name: "VASAVI",
  tagline: "TRADING COMPANY",
  accent: "roast",
  languages_enabled: ["en", "te", "hi"],
  default_language: "en",
  idle_shared_min: 15,
  idle_personal_min: 480,
  location_label: "PROD-01",
  quiet_start_hour: 21,
  quiet_end_hour: 7,
  staff_can_message_admins: false,
  voice_retention_days: 180,
  company_address: "Besant Road, Governorpet, Vijayawada, AP - 520002",
  company_gstin: "37AAAAA0000A1Z5",
  tax_setup_approved: true,
  bill_prefix: "CTR",
  invoice_prefix: "INV",
  counter_opening_float_paise: 200000,
  discount_cap_pct: 5,
  refund_manager_cap_paise: 50000,
  correction_manager_cap_kg: 2,
  low_stock_alert_kg: 100,
  roast_rest_days: 13,
  custom_coffee_rate_paise: 90000,
  custom_chicory_rate_paise: 30000,
};

export const SEED_USERS: User[] = [
  {
    id: 1,
    username: "owner",
    password: "VTC-Owner-Demo26",
    pin: "4820",
    full_name: "Owner (Demo)",
    role_name: "Admin",
    tier: "management",
    is_owner: true,
    must_change_secret: false,
    prefs: {
      lang: "en",
      large_text: false,
      shortcuts: ["inward", "roasting", "inventory"],
      widgets: ["stock", "pending_invoices", "holds", "orders", "team", "roasts", "audit", "quick_actions"],
      welcome: "Good afternoon, Owner"
    },
    perms: ALL_PERMISSIONS,
    modules: ALL_MODULES,
    live_modules: ALL_MODULES,
    settings: DEFAULT_SETTINGS
  },
  {
    id: 2,
    username: "admin",
    password: "VTC-Admin-Demo26",
    pin: "1234",
    full_name: "Office Admin (Demo)",
    role_name: "Admin",
    tier: "management",
    is_owner: false,
    must_change_secret: false,
    prefs: {
      lang: "en",
      large_text: false,
      shortcuts: ["inward", "roasting", "inventory"],
      widgets: ["stock", "pending_invoices", "holds", "orders", "team", "roasts", "audit", "quick_actions"],
      welcome: "Good afternoon, Admin"
    },
    perms: ALL_PERMISSIONS.filter((p) => p !== "system.roles" && p !== "system.grants"),
    modules: ALL_MODULES.filter((m) => m !== "admin"),
    live_modules: ALL_MODULES,
    settings: DEFAULT_SETTINGS
  },
  {
    id: 3,
    username: "manager",
    password: "VTC-Manager-Demo26",
    pin: "5678",
    full_name: "Demo Manager",
    role_name: "Manager",
    tier: "management",
    is_owner: false,
    must_change_secret: false,
    prefs: {
      lang: "en",
      large_text: false,
      shortcuts: ["tasks", "inventory", "orders"],
      widgets: ["team", "my_tasks", "stock", "holds", "orders", "packing", "roasts", "quick_actions"],
      welcome: "Good afternoon, Manager"
    },
    perms: [
      "comms.announce", "comms.chat", "comms.read", "counter.close", "counter.price_override",
      "counter.refund", "counter.sell", "dashboard.my_view", "dashboard.team_view", "dispatch.handover",
      "documents.price_hidden", "exceptions.view", "finance.collection.record", "goods.receive", "goods.view",
      "grind.execute", "inventory.transfer", "inventory.view", "maintenance.manage", "maintenance.raise",
      "masters.create_draft", "masters.view", "masters.view_drafts", "outgoing.create", "outgoing.credit.request",
      "outgoing.view", "outside.execute", "outside.view", "pack.create", "pack.execute", "pack.view", "qc.view",
      "recipe.view_public", "reports.operational", "roast.execute", "roast.view", "stock.cycle_count",
      "tasks.approve", "tasks.assign", "tasks.update_any", "tasks.update_own", "tasks.view_all", "tasks.view_own", "team.view"
    ],
    modules: ["dashboard", "tasks", "inward", "inventory", "roasting", "blending", "qc", "outside", "packing", "counter", "orders", "dispatch", "comms", "maintenance", "team", "exceptions", "reports"],
    live_modules: ALL_MODULES,
    settings: DEFAULT_SETTINGS
  },
  {
    id: 4,
    username: "roaster",
    pin: "7580",
    full_name: "Demo Roaster",
    role_name: "Roaster",
    tier: "staff",
    is_owner: false,
    must_change_secret: false,
    prefs: {
      lang: "en",
      large_text: true,
      shortcuts: ["roasting", "blending", "inventory"],
      widgets: ["my_tasks", "quick_actions", "incoming", "roasts", "packing", "stock"],
      welcome: "Good afternoon, Roaster"
    },
    perms: [
      "dashboard.my_view", "tasks.view_own", "tasks.update_own", "goods.view", "goods.receive",
      "inventory.view", "inventory.transfer", "stock.cycle_count", "roast.view", "roast.execute",
      "recipe.view_public", "grind.execute", "outside.view", "outside.execute", "comms.read", "comms.chat",
      "maintenance.raise", "team.view"
    ],
    modules: ["dashboard", "tasks", "inward", "inventory", "roasting", "blending", "outside", "comms", "maintenance"],
    live_modules: ALL_MODULES,
    settings: DEFAULT_SETTINGS
  },
  {
    id: 5,
    username: "packer",
    pin: "1470",
    full_name: "Demo Packer",
    role_name: "Packer",
    tier: "staff",
    is_owner: false,
    must_change_secret: false,
    prefs: {
      lang: "en",
      large_text: true,
      shortcuts: ["packing", "inventory", "tasks"],
      widgets: ["my_tasks", "quick_actions", "packing", "stock"],
      welcome: "Good afternoon, Packer"
    },
    perms: [
      "dashboard.my_view", "tasks.view_own", "tasks.update_own", "goods.view", "inventory.view",
      "pack.view", "pack.execute", "comms.read", "comms.chat", "maintenance.raise", "team.view"
    ],
    modules: ["dashboard", "tasks", "inventory", "packing", "comms", "maintenance"],
    live_modules: ALL_MODULES,
    settings: DEFAULT_SETTINGS
  }
];

export const INITIAL_DATA = {
  inward: [
    {
      id: 4,
      receipt_code: "IN-261027-04",
      created_at: "2026-10-27T10:15:00Z",
      source: "AUCTION",
      source_name: "Sample Coffee Auction House",
      coffee_name: "Assam CTC tea (sample)",
      product_id: 1,
      bags: 2,
      damaged: 0,
      partial: 0,
      kg_in: 50,
      invoice_ref: "SUP-4",
      invoice_status: "PENDING",
      payment_status: "PENDING",
      invoice_amount_paise: null,
      paid_paise: 0,
    },
    {
      id: 3,
      receipt_code: "IN-261027-03",
      created_at: "2026-10-27T09:40:00Z",
      source: "AUCTION",
      source_name: "Sample Coffee Auction House",
      coffee_name: "Chicory granules (sample)",
      product_id: 2,
      bags: 4,
      damaged: 0,
      partial: 0,
      kg_in: 100,
      invoice_ref: "SUP-3",
      invoice_status: "PENDING",
      payment_status: "PENDING",
      invoice_amount_paise: null,
      paid_paise: 0,
    },
    {
      id: 2,
      receipt_code: "IN-261027-02",
      created_at: "2026-10-26T16:20:00Z",
      source: "AUCTION",
      source_name: "Sample Coffee Auction House",
      coffee_name: "Arabica Plantation A (sample)",
      product_id: 3,
      bags: 6,
      damaged: 0,
      partial: 0,
      kg_in: 300,
      invoice_ref: "SUP-2",
      invoice_status: "MATCHED",
      payment_status: "PAID",
      invoice_amount_paise: 11400000,
      paid_paise: 11400000,
    },
    {
      id: 1,
      receipt_code: "IN-261027-01",
      created_at: "2026-10-26T11:00:00Z",
      source: "AUCTION",
      source_name: "Sample Coffee Auction House",
      coffee_name: "Robusta Cherry AB (sample)",
      product_id: 4,
      bags: 12,
      damaged: 1,
      partial: 2,
      kg_in: 661.5,
      invoice_ref: "SUP-1",
      invoice_status: "MATCHED",
      payment_status: "PAID",
      invoice_amount_paise: 16206750,
      paid_paise: 16206750,
    }
  ],

  masters_products: [
    { id: 1, kind: "product", name: "Assam CTC tea (sample)", nominal_kg: 25, category: "OTHER", state: "ACTIVE" },
    { id: 2, kind: "product", name: "Chicory granules (sample)", nominal_kg: 25, category: "CHICORY", state: "ACTIVE" },
    { id: 3, kind: "product", name: "Arabica Plantation A (sample)", nominal_kg: 50, category: "GREEN_COFFEE", state: "ACTIVE" },
    { id: 4, kind: "product", name: "Robusta Cherry AB (sample)", nominal_kg: 50, category: "GREEN_COFFEE", state: "ACTIVE" }
  ],

  masters_suppliers: [
    { id: 1, kind: "supplier", name: "Sample Coffee Auction House", phone: "98480 12345", state: "ACTIVE" },
    { id: 2, kind: "supplier", name: "Coorg Planters Syndicate", phone: "98480 54321", state: "ACTIVE" }
  ],

  masters_customers: [
    { id: 1, kind: "customer", name: "Sample Café, Banjara Hills", phone: "90000 00001", state: "ACTIVE" },
    { id: 2, kind: "customer", name: "Sample Dealer, Secunderabad", phone: "90000 00002", state: "ACTIVE" },
    { id: 3, kind: "customer", name: "Manager's draft client", phone: "90000 00003", state: "DRAFT" }
  ],

  pending: {
    dashboard: { count: 0, tasks: [], waiting: [] },
    tasks: {
      count: 1,
      tasks: [{ id: 101, title: "Clean roaster chaff collector", status: "OPEN", target: null, progress: 0, unit: "task", mine: true, assigned_to: 1 }],
      waiting: []
    },
    inward: { count: 0, tasks: [], waiting: [] },
    inventory: { count: 1, tasks: [], waiting: [{ count: 1, label: "damaged-bag lots to verify" }] },
    roasting: {
      count: 1,
      tasks: [
        {
          id: 2,
          title: "Roast 3 batches",
          due: "2026-10-27",
          assignee: "Demo Roaster",
          assigned_to: 4,
          progress: 3,
          target: 3,
          unit: "batches",
          status: "IN_PROGRESS"
        }
      ],
      waiting: []
    },
    qc: { count: 1, tasks: [], waiting: [{ count: 1, label: "roast sample awaiting cup test" }] },
    packing: {
      count: 1,
      tasks: [
        {
          id: 1,
          title: "Pack 12 packs — ticket #1 Filter blend",
          due: "2026-10-27",
          assignee: "Demo Packer",
          assigned_to: 5,
          progress: 12,
          target: 12,
          unit: "packs",
          status: "SUBMITTED"
        }
      ],
      waiting: [
        { count: 1, label: "packing tickets not finished" }
      ]
    },
    counter: {
      count: 1,
      tasks: [
        {
          id: 3,
          title: "Handle 10 counter bills",
          due: "2026-10-27",
          assignee: "Demo Roaster",
          assigned_to: 4,
          progress: 0,
          target: 10,
          unit: "bills",
          status: "OPEN"
        }
      ],
      waiting: []
    },
    maintenance: { count: 1, tasks: [], waiting: [{ count: 1, label: "sealer machine ticket open" }] },
    admin: { count: 1, tasks: [], waiting: [{ count: 1, label: "suggested records to approve" }] }
  },

  golive: {
    state: {
      state: "SETUP",
      confirmed_at: null,
      confirmed_by_name: null,
      cash_paise: null
    },
    counts: {
      clients: 2,
      suppliers: 1,
      beans: 4,
      customers: 1
    },
    stock: [] as any[],
    dues: [] as any[]
  },

  summary: {
    tasks: 1,
    received_bags: 12,
    beans_in_stock: 4,
    beans_low: 0,
    roasts: 3,
    packing: 1,
    team_tasks: 4,
    to_review: 1,
    held: 3,
    orders: 2,
    pending_invoices: 3
  },

  tasks: [
    {
      id: 1,
      title: "Pack 12 packs — ticket #1 Filter blend",
      kind: "PACK",
      kind_label: "Packing",
      due: "2026-10-27",
      assigned_by: "Demo Manager",
      submit_note: "12 packs done, 1 rejected",
      assignee: "Demo Packer",
      assigned_to: 5,
      progress: 12,
      target: 12,
      unit: "packs",
      status: "SUBMITTED"
    },
    {
      id: 2,
      title: "Roast 3 batches",
      kind: "ROAST",
      kind_label: "Roasting",
      due: "2026-10-27",
      assigned_by: "Demo Manager",
      assignee: "Demo Roaster",
      assigned_to: 4,
      progress: 3,
      target: 3,
      unit: "batches",
      status: "IN_PROGRESS"
    },
    {
      id: 3,
      title: "Handle 10 counter bills",
      kind: "COUNTER",
      kind_label: "Counter sales",
      due: "2026-10-27",
      assigned_by: "Demo Manager",
      assignee: "Demo Roaster",
      assigned_to: 4,
      progress: 0,
      target: 10,
      unit: "bills",
      status: "OPEN"
    },
    {
      id: 4,
      title: "Clean roaster chaff collector",
      kind: "GENERAL",
      kind_label: "General work",
      due: "",
      assigned_by: "Demo Manager",
      assignee: "Demo Roaster",
      assigned_to: 4,
      progress: 0,
      target: null,
      unit: "task",
      status: "OPEN"
    }
  ],

  inventory: [
    // Product 3: Arabica Plantation A (sample)
    { id: 101, product_id: 3, name: "Arabica Plantation A (sample)", product: "Arabica Plantation A (sample)", stage: "RAW", bag_kind: "SOUND", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 240, available_kg: 240, nominal_kg: 50, bags: 5, receipt_code: "IN-261027-01", created_at: "2026-10-27T09:30:00" },
    { id: 102, product_id: 3, name: "Roasted Arabica Plantation A", product: "Roasted Arabica Plantation A", stage: "ROASTED", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 36.6, available_kg: 36.6, resting: true, ready_from: "2026-11-09", receipt_code: "ROAST-01", created_at: "2026-10-27T10:30:00" },

    // Product 1: Assam CTC tea (sample)
    { id: 103, product_id: 1, name: "Assam CTC tea (sample)", product: "Assam CTC tea (sample)", stage: "RAW", bag_kind: "SOUND", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 40, available_kg: 40, nominal_kg: 25, bags: 2, receipt_code: "IN-261027-04", created_at: "2026-10-27T11:00:00" },
    { id: 104, product_id: 1, name: "Assam CTC tea (sample)", product: "Assam CTC tea (sample)", stage: "RAW", location: "CTR-01", owner: "VTC", status: "RELEASED", kg: 9.55, available_kg: 9.55, receipt_code: "TR-01", created_at: "2026-10-27T12:00:00" },

    // Product 2: Chicory granules (sample)
    { id: 105, product_id: 2, name: "Chicory granules (sample)", product: "Chicory granules (sample)", stage: "RAW", bag_kind: "SOUND", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 85, available_kg: 85, nominal_kg: 25, bags: 3, receipt_code: "IN-261027-03", created_at: "2026-10-27T11:30:00" },
    { id: 106, product_id: 2, name: "Chicory granules (sample)", product: "Chicory granules (sample)", stage: "RAW", location: "CTR-01", owner: "VTC", status: "RELEASED", kg: 14.45, available_kg: 14.45, receipt_code: "TR-02", created_at: "2026-10-27T12:30:00" },

    // Product 4: Robusta Cherry AB (sample)
    { id: 107, product_id: 4, name: "Robusta Cherry AB (sample)", product: "Robusta Cherry AB (sample)", stage: "RAW", bag_kind: "SOUND", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 320, available_kg: 320, nominal_kg: 50, bags: 6, receipt_code: "IN-261027-02", created_at: "2026-10-27T09:00:00" },
    { id: 108, product_id: 4, name: "Robusta Cherry AB (sample)", product: "Robusta Cherry AB (sample)", stage: "RAW", bag_kind: "PARTIAL", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 41.5, available_kg: 41.5, bags: 1, receipt_code: "IN-261027-02", created_at: "2026-10-27T09:00:00" },
    { id: 109, product_id: 4, name: "Robusta Cherry AB (sample)", stage: "RAW", bag_kind: "DAMAGED", location: "PROD-01", owner: "VTC", status: "QUARANTINE", kg: 50, available_kg: 0, bags: 1, receipt_code: "IN-261027-02", created_at: "2026-10-27T09:00:00" },
    { id: 110, product_id: 4, name: "Roasted Robusta", stage: "ROASTED", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 77.25, available_kg: 77.25, resting: true, ready_from: "2026-11-09", receipt_code: "ROAST-02", created_at: "2026-10-27T13:00:00" },
    { id: 111, product_id: 4, name: "Roasted Robusta", stage: "ROASTED", location: "PROD-01", owner: "VTC", status: "QC_HOLD", kg: 1.65, available_kg: 0, receipt_code: "ROAST-03", created_at: "2026-10-27T14:00:00" },
    { id: 112, product_id: 4, name: "Ground Robusta", stage: "GROUND", location: "PROD-01", owner: "VTC", status: "QC_HOLD", kg: 9.4, available_kg: 0, receipt_code: "GRD-01", created_at: "2026-10-27T15:00:00" },
    { id: 113, product_id: 4, name: "Robusta at counter", stage: "RAW", location: "CTR-01", owner: "VTC", status: "QC_HOLD", kg: 19.05, available_kg: 0, receipt_code: "TR-03", created_at: "2026-10-27T16:00:00" },

    // Other: Blends, packs & customers' coffee (Screenshot 3)
    { id: 114, code: "BLD-MIX-261027-01", name: "Blend: House filter blend v1 (job #1)", stage: "BLENDED", location: "PROD-01", owner: "VTC", status: "RELEASED", kg: 19.9, available_kg: 19.9, resting: true, ready_from: "2026-11-09", receipt_code: "BLD-01", created_at: "2026-10-27T16:30:00" }
  ] as any[],

  transfers: [
    {
      id: 3,
      transit_code: "#3",
      lot_code: "Assam CTC tea (sample) · IN-261027-04 · Sound bags",
      lot_name: "",
      kg_sent: 10,
      kg_received: 9.8,
      state: "SHORTAGE",
      sent_by_name: "Demo Roaster",
      created_at: "2026-10-27T17:36:00",
      received_by_name: "Demo Manager"
    },
    {
      id: 2,
      transit_code: "#2",
      lot_code: "Chicory granules (sample) · IN-261027-03 · Sound bags",
      lot_name: "",
      kg_sent: 15,
      kg_received: 15,
      state: "RECEIVED",
      sent_by_name: "Demo Roaster",
      created_at: "2026-10-27T17:36:00",
      received_by_name: "Demo Manager"
    },
    {
      id: 1,
      transit_code: "#1",
      lot_code: "Ground (South Indian filter) from lot #7",
      lot_name: "",
      kg_sent: 20,
      kg_received: 20,
      state: "RECEIVED",
      sent_by_name: "Demo Roaster",
      created_at: "2026-10-27T17:36:00",
      received_by_name: "Demo Manager"
    }
  ] as any[],

  stock_cards: [
    { product_id: 3, name: "Arabica Plantation A (sample)", label: "Arabica Plantation A (sample)", code: "ARAPAS", category: "green_coffee", color: "#8a5a3c", used_30_kg: 20.3, low_kg: null, pinned: true },
    { product_id: 1, name: "Assam CTC tea (sample)", label: "Assam CTC tea (sample)", code: "ASSCTC", category: "tea", color: "#8a5a3c", used_30_kg: 2.0, low_kg: null, pinned: true },
    { product_id: 2, name: "Chicory granules (sample)", label: "Chicory granules (sample)", code: "CHIGS", category: "chicory", color: "#8a5a3c", used_30_kg: 3.0, low_kg: null, pinned: true },
    { product_id: 4, name: "Robusta Cherry AB (sample)", label: "Robusta Cherry AB (sample)", code: "ROBCAB", category: "green_coffee", color: "#8a5a3c", used_30_kg: 35.2, low_kg: 100, pinned: true }
  ] as any[],

  roast_profiles: [
    { id: 1, name: "City+ (sample)", notes: "205°C drop, 16 min", active: true },
    { id: 2, name: "Medium filter (sample)", notes: "212°C drop", active: true },
    { id: 3, name: "Dark South Indian (sample)", notes: "218°C drop, high body", active: true }
  ],

  roasts: [
    { id: 1, batch_no: "ROAST-1027-01", bean_name: "Plantation A (Raw Green)", green_kg: 60, roasted_kg: 50.1, loss_pct: 16.5, drop_temp: "204°C", status: "RELEASED", created_at: "Today, 09:30 AM" },
    { id: 2, batch_no: "ROAST-1027-02", bean_name: "Robusta Cherry AB", green_kg: 60, roasted_kg: 50.4, loss_pct: 16.0, drop_temp: "206°C", status: "RELEASED", created_at: "Today, 11:15 AM" },
    { id: 3, batch_no: "ROAST-1027-03", bean_name: "French Chicory", green_kg: 50, roasted_kg: 46.5, loss_pct: 7.0, drop_temp: "190°C", status: "RELEASED", created_at: "Today, 01:45 PM" }
  ],

  roasts_records: [
    {
      id: 3,
      code: "ROAST-1027-03",
      created_at: "2026-10-27T13:45:00Z",
      roaster: "Demo Roaster",
      inputs: "#105 (50.000 kg)",
      input_kg: 50,
      output_kg: 46.5,
      loss_kg: 3.5,
      loss_pct: 7.0,
      duration_min: 14,
      profile: "Dark South Indian (sample)",
      qc_status: "RELEASED",
      abnormal: false,
      notes: ""
    },
    {
      id: 2,
      code: "ROAST-1027-02",
      created_at: "2026-10-27T11:15:00Z",
      roaster: "Demo Roaster",
      inputs: "#107 (60.000 kg)",
      input_kg: 60,
      output_kg: 50.4,
      loss_kg: 9.6,
      loss_pct: 16.0,
      duration_min: 16,
      profile: "Medium filter (sample)",
      qc_status: "RELEASED",
      abnormal: false,
      notes: ""
    },
    {
      id: 1,
      code: "ROAST-1027-01",
      created_at: "2026-10-27T09:30:00Z",
      roaster: "Demo Roaster",
      inputs: "#101 (60.000 kg)",
      input_kg: 60,
      output_kg: 50.1,
      loss_kg: 9.9,
      loss_pct: 16.5,
      duration_min: 15,
      profile: "City+ (sample)",
      qc_status: "RELEASED",
      abnormal: false,
      notes: ""
    }
  ] as any[],

  orders: [
    {
      id: 2,
      created_at: "2026-10-27T17:36:00",
      customer: "Sample Dealer, Secunderabad",
      customer_id: 2,
      item: "Roasted Arabica beans",
      form: "ROASTED",
      quantity_kg: 12,
      dispatched_kg: 0,
      reserved_kg: 0,
      price_per_kg_paise: 61000,
      gst_rate_bp: 500,
      credit_requested: true,
      credit_note: "No approved credit terms for this customer",
      state: "CREDIT_REVIEW",
      created_by: 3,
      invoices: [],
      dispatches: [],
      customer_phone: "90000 00002",
      cleared: false,
      clearance_note: ""
    },
    {
      id: 1,
      created_at: "2026-10-27T17:36:00",
      customer: "Sample Café, Banjara Hills",
      customer_id: 1,
      item: "Roasted Robusta AB, medium",
      form: "ROASTED",
      quantity_kg: 10,
      dispatched_kg: 10,
      reserved_kg: 0,
      price_per_kg_paise: 52000,
      gst_rate_bp: 500,
      credit_requested: true,
      credit_note: "Within approved credit terms",
      state: "DELIVERED",
      created_by: 3,
      invoices: [{ id: 1, number: "INV-2610-0001" }],
      dispatches: [
        {
          id: 1,
          mode: "RAPIDO",
          kg: 10,
          delivered_kg: 10,
          delivered_at: "2026-10-27T17:36:00",
          handover_to: "Rapido Rider",
          booking_ref: "RAP-88129",
          created_at: "2026-10-27T17:36:00",
          pod_note: "Delivered to manager"
        }
      ],
      customer_phone: "90000 00001",
      cleared: true,
      clearance_note: "Delivered"
    }
  ],

  outside_jobs: [] as any[],

  packing_jobs: [
    {
      id: 1,
      demand_type: "STOCK",
      product_name: "Filter blend",
      packs_target: 20,
      pack_g: 500,
      packs_done: 12,
      input_kg: 6.2,
      rejected_packs: 1,
      finished_status: "QC_HOLD",
      state: "IN_PROGRESS",
      source_lot_id: 114,
      created_at: "2026-10-27T10:00:00Z"
    }
  ] as any[],

  comms_inbox: [
    {
      id: 1,
      kind: "announcement",
      title: "Announcements",
      unread: 0,
      last: { author: "Owner", body: "Stock count this Saturday at 8 am. Everyone please be on time." }
    },
    {
      id: 2,
      kind: "group",
      title: "Office / Accounts",
      unread: 0,
      last: null
    }
  ] as any[],

  comms_oversight: [
    { id: 3, kind: "group", title: "Roastery", messages: 2, last_at: "2026-10-27T17:36:00" },
    { id: 4, kind: "record", title: "Roast batch #3", record_type: "roast", messages: 1, last_at: "2026-10-27T17:36:00" }
  ] as any[],

  comms_conversations: {
    1: {
      conversation: { id: 1, kind: "announcement", title: "Announcements" },
      members: ["Office Admin (Demo)", "Owner (Demo)", "Demo Manager", "Demo Roaster", "Demo Packer"],
      notice: "Work messages can be read by the admin for oversight. Please keep them about work.",
      may_post: true,
      roles: [
        { id: 1, name: "Admin" },
        { id: 2, name: "Manager" },
        { id: 3, name: "Roaster" },
        { id: 4, name: "Packer" }
      ],
      teams: [
        { id: 1, name: "Roastery Floor" },
        { id: 2, name: "Accounts" }
      ],
      messages: [
        {
          id: 1,
          author: "Owner (Demo)",
          author_id: 1,
          created_at: "2026-10-27T17:36:00",
          body: "Stock count this Saturday at 8 am. Everyone please be on time.",
          receipts: { seen: [], not_seen: ["Office Admin", "Demo Manager", "Demo Roaster", "Demo Packer"], total: 4 }
        }
      ]
    },
    2: {
      conversation: { id: 2, kind: "group", title: "Office / Accounts" },
      members: ["Office Admin (Demo)", "Owner (Demo)"],
      notice: "Work messages can be read by the admin for oversight. Please keep them about work.",
      may_post: true,
      roles: [],
      teams: [],
      messages: []
    },
    3: {
      conversation: { id: 3, kind: "group", title: "Roastery" },
      members: ["Demo Roaster", "Demo Manager", "Owner (Demo)"],
      notice: "Work messages can be read by the admin for oversight. Please keep them about work.",
      may_post: false,
      oversight: true,
      roles: [],
      teams: [],
      messages: [
        { id: 1, author: "Demo Roaster", author_id: 4, created_at: "2026-10-27T17:30:00", body: "Drum warmed to 200°C." },
        { id: 2, author: "Demo Manager", author_id: 3, created_at: "2026-10-27T17:36:00", body: "Go ahead with batch #1." }
      ]
    },
    4: {
      conversation: { id: 4, kind: "record", title: "Roast batch #3", record_type: "roast" },
      members: ["Demo Roaster", "Owner (Demo)"],
      notice: "Discussion attached to this record",
      may_post: false,
      oversight: true,
      roles: [],
      teams: [],
      messages: [
        { id: 1, author: "Demo Roaster", author_id: 4, created_at: "2026-10-27T17:36:00", body: "Chicory roast finished at 190°C drop." }
      ]
    }
  } as Record<number, any>,

  receivables: {
    aging: {
      current: 346000,
      "1-30": 0,
      "31-60": 0,
      "60+": 0
    },
    pending: [],
    invoices: [
      {
        id: 1,
        number: "INV-2610-0001",
        customer: "Sample Café, Banjara Hills",
        customer_phone: "90000 00001",
        total_paise: 546000,
        paid_paise: 200000,
        due_paise: 346000,
        due_date: "2026-11-11",
        days_late: 0,
        status: "PART_PAID"
      }
    ]
  },

  payables: {
    receipts: [
      {
        id: 4,
        created_at: "2026-10-27T17:36:00",
        source_name: "Sample Coffee Auction House",
        product: "Assam CTC tea (sample)",
        bags: 2,
        invoice_ref: null,
        invoice_status: "PENDING",
        invoice_amount_paise: null,
        paid_paise: 0,
        payment_status: "PENDING"
      },
      {
        id: 3,
        created_at: "2026-10-27T17:36:00",
        source_name: "Sample Coffee Auction House",
        product: "Chicory granules (sample)",
        bags: 4,
        invoice_ref: null,
        invoice_status: "PENDING",
        invoice_amount_paise: null,
        paid_paise: 0,
        payment_status: "PENDING"
      },
      {
        id: 2,
        created_at: "2026-10-27T17:36:00",
        source_name: "Sample Coffee Auction House",
        product: "Arabica Plantation A (sample)",
        bags: 6,
        invoice_ref: "SAMPLE-INV-118",
        invoice_status: "MATCHED",
        invoice_amount_paise: 3600000,
        paid_paise: 0,
        payment_status: "PENDING"
      },
      {
        id: 1,
        created_at: "2026-10-27T17:36:00",
        source_name: "Sample Coffee Auction House",
        product: "Robusta Cherry AB (sample)",
        bags: 12,
        invoice_ref: null,
        invoice_status: "PENDING",
        invoice_amount_paise: null,
        paid_paise: 0,
        payment_status: "PENDING"
      }
    ],
    payments: [
      {
        id: 1,
        receipt_id: 2,
        amount_paise: 3600000,
        reference: "NEFT-30021 (sample)",
        paid_on: "2026-10-27",
        recorded_by: 1,
        recorded_by_name: "Owner (Demo)",
        state: "PENDING_APPROVAL"
      }
    ]
  },

  exceptions: [
    {
      id: 1,
      title: "Damaged bags to verify — lot #2",
      detail: "Robusta Cherry AB (sample) · IN-261027-01 · Damaged bags",
      category: "receipt variance",
      opened_at: "2026-10-27T17:36:00",
      severity: "high",
      view: "inventory"
    },
    {
      id: 2,
      title: "Roast #3 waiting for quality decision",
      detail: "30 → 24.1 kg",
      category: "qc hold",
      opened_at: "2026-10-27T17:36:00",
      severity: "high",
      view: "qc"
    },
    {
      id: 3,
      title: "Transfer #3 arrived short by 0.2 kg",
      detail: "",
      category: "transfer shortage",
      opened_at: "2026-10-27T17:36:00",
      severity: "high",
      view: "inventory"
    },
    {
      id: 4,
      title: "Order #2 (Sample Dealer, Secunderabad) on credit hold",
      detail: "No approved credit terms for this customer",
      category: "credit hold",
      opened_at: "2026-10-27T17:36:00",
      severity: "high",
      view: "orders"
    },
    {
      id: 5,
      title: "Invoice not matched within 3 days — receipt #1 (O-34)",
      detail: "Sample Coffee Auction House",
      category: "invoice difference",
      opened_at: "2026-10-27T17:36:00",
      severity: "normal",
      view: "finance"
    },
    {
      id: 6,
      title: "Unverified customer payment — INV-2610-0001",
      detail: "UPI ₹2,000.00 from Sample Café, Banjara Hills",
      category: "unverified collection",
      opened_at: "2026-10-27T17:36:00",
      severity: "normal",
      view: "finance"
    },
    {
      id: 7,
      title: "Supplier payment awaiting second approval",
      detail: "NEFT ₹36,000.00 for receipt #2",
      category: "payment approval",
      opened_at: "2026-10-27T17:36:00",
      severity: "normal",
      view: "finance"
    }
  ],

  opening_dues: {
    pending: [],
    dues: [
      {
        id: 1,
        party: "Sample Café, Banjara Hills",
        party_phone: "90000 00001",
        side: "CLIENT",
        amount_paise: 1500000,
        paid_paise: 1500000,
        due_paise: 0,
        due_date: "2026-10-15",
        note: "Legacy balance pre-portal"
      }
    ]
  },

  tally_exports: [
    {
      id: 1,
      batch_no: "TALLY-20261027-01",
      created_at: "2026-10-27T17:36:00",
      period_from: "2026-10-01",
      period_to: "2026-10-27",
      voucher_count: 14,
      control_total_paise: 18450000,
      checksum: "a3f5b78912cd4e6f890123456789abcdef0123456789abcdef0123456789abcd",
      state: "ACCEPTED",
      result_note: "Imported into TallyPrime by Admin",
      content: "Voucher Number,Date,Party,Voucher Type,Amount\nINV-2610-0001,2026-10-27,Sample Café Banjara Hills,Sales,5460.00\n"
    }
  ]
};

// Local storage persistent manager
const DB_KEY = "vtc_preview_db_v2";
const AUTH_KEY = "vtc_preview_auth_user";

export function getDb(): typeof INITIAL_DATA {
  try {
    const raw = localStorage.getItem(DB_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (!parsed.roasts_records) parsed.roasts_records = INITIAL_DATA.roasts_records;
      if (!parsed.roast_profiles || !parsed.roast_profiles[0]?.active) parsed.roast_profiles = INITIAL_DATA.roast_profiles;
      if (!parsed.pending?.roasting?.tasks?.length) parsed.pending.roasting = INITIAL_DATA.pending.roasting;
      if (!parsed.outside_jobs) parsed.outside_jobs = INITIAL_DATA.outside_jobs;
      if (!parsed.packing_jobs || !parsed.packing_jobs.length) parsed.packing_jobs = INITIAL_DATA.packing_jobs;
      if (!parsed.pending?.packing?.tasks?.length) parsed.pending.packing = INITIAL_DATA.pending.packing;
      if (!parsed.pending?.counter?.tasks?.length) parsed.pending.counter = INITIAL_DATA.pending.counter;
      if (parsed.masters_customers?.[0]?.name === "Sample Café") parsed.masters_customers[0].name = "Sample Café, Banjara Hills";
      if (!parsed.orders || !parsed.orders[0]?.state) parsed.orders = INITIAL_DATA.orders;
      if (parsed.masters_customers?.[1]?.name === "Sample Dealer") parsed.masters_customers[1].name = "Sample Dealer, Secunderabad";
      if (!parsed.receivables || parsed.receivables.aging?.current !== 346000) parsed.receivables = INITIAL_DATA.receivables;
      if (!parsed.payables) parsed.payables = INITIAL_DATA.payables;
      if (!parsed.exceptions || !parsed.exceptions.length) parsed.exceptions = INITIAL_DATA.exceptions;
      if (!parsed.pending?.admin || parsed.pending.admin.waiting?.[0]?.label !== "suggested records to approve") parsed.pending.admin = INITIAL_DATA.pending.admin;
      if (parsed.masters_customers?.[2] && parsed.masters_customers[2].name === "Manager's draft client" && !parsed.masters_customers[2].approved) {
        parsed.masters_customers[2].state = "DRAFT";
      }
      if (!parsed.opening_dues) parsed.opening_dues = INITIAL_DATA.opening_dues;
      if (!parsed.tally_exports) parsed.tally_exports = INITIAL_DATA.tally_exports;
      if (!parsed.golive) parsed.golive = INITIAL_DATA.golive;
      if (!parsed.golive.counts) parsed.golive.counts = INITIAL_DATA.golive.counts;
      return parsed;
    }
  } catch {}
  return JSON.parse(JSON.stringify(INITIAL_DATA));
}

export function saveDb(data: typeof INITIAL_DATA) {
  try {
    localStorage.setItem(DB_KEY, JSON.stringify(data));
  } catch {}
}

export function resetDb() {
  localStorage.removeItem(DB_KEY);
  localStorage.removeItem(AUTH_KEY);
}

export function getCurrentUser(): User | null {
  try {
    const saved = localStorage.getItem(AUTH_KEY);
    if (saved) {
      const u = JSON.parse(saved);
      const match = SEED_USERS.find((x) => x.id === u.id);
      if (match) return match;
    }
  } catch {}
  return null;
}

export function setCurrentUser(u: User | null) {
  if (u) {
    localStorage.setItem(AUTH_KEY, JSON.stringify({ id: u.id, username: u.username }));
  } else {
    localStorage.removeItem(AUTH_KEY);
  }
}

export const AVAILABLE_WIDGETS = [
  { id: "stock", title: "Beans in stock" },
  { id: "pending_invoices", title: "Invoices to review" },
  { id: "holds", title: "Quality holds" },
  { id: "orders", title: "Sales orders" },
  { id: "team", title: "Active team tasks" },
  { id: "roasts", title: "Roasting batches" },
  { id: "audit", title: "Audit activity" },
  { id: "incoming", title: "Received bags" },
  { id: "my_tasks", title: "My assigned tasks" },
  { id: "packing", title: "Packing jobs open" },
  { id: "review", title: "Work to review" },
  { id: "quick_actions", title: "Quick actions" }
];

export function formatMeResponse(user: User) {
  return {
    user: {
      id: user.id,
      username: user.username,
      full_name: user.full_name,
      role_name: user.role_name,
      role_id: user.id,
      tier: user.tier,
      is_owner: user.is_owner,
      must_change_secret: user.must_change_secret || false,
      login_type: user.pin ? "pin" : "password",
      device_mode: "personal"
    },
    perms: user.is_owner ? ALL_PERMISSIONS : Array.from(new Set([...(user.perms || ALL_PERMISSIONS), "counter.customers", "counter.insights"])),
    modules: user.modules || ALL_MODULES,
    live_modules: user.live_modules || ALL_MODULES,
    prefs: {
      lang: user.prefs?.lang || "en",
      large_text: user.prefs?.large_text || false,
      shortcuts: user.prefs?.shortcuts || ["inward", "roasting", "inventory"],
      widgets: user.prefs?.widgets || ["stock", "pending_invoices", "holds", "orders", "team", "roasts", "audit", "quick_actions"],
      welcome: user.prefs?.welcome || `Good afternoon, ${user.full_name.split(" ")[0]}`,
      sizes: { quick_actions: "l" }
    },
    settings: {
      ...DEFAULT_SETTINGS,
      ...(user.settings || {})
    },
    idle_minutes: user.tier === "staff" ? 15 : 480,
    available_widgets: AVAILABLE_WIDGETS
  };
}
