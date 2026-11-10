// Counter Sales: B2B Dealer Billing, Café Billing, Market Dues & Aging,
// Client Directory, Walk-in POS, 80mm receipts, Cash Close and Analytics.

import { api, newKey } from "../api.js";
import { t } from "../i18n.js";
import { S, can, nav } from "../state.js";
import { check, chip, confirmBox, dialog, fail, field, goto, h, num, pageHead, select, table, tag, toast, when } from "../ui.js";
import { discussBtn } from "./comms.js";
import { icon } from "../icons.js";
import { fmtDay } from "../labels.js";
import { waBill } from "../whatsapp.js";
import { cashTab, clearCustomer, customerCard, customersTab, cxCan, cxClick, cxSubmit, lastCustomer, maybeLookup, openCash } from "./countercx.js";
import { clientDialog } from "./clientform.js";
import { counterBillHtml, printHtml80mm, AtposEscPosDriver } from "../thermal-printer.js";

const rupee = (p) => "₹" + (Number(p || 0) / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const KIND_LABEL = { coffee_ground: "Ground coffee blend", coffee_beans: "Coffee beans blend", product: "Stock product (tea, chicory…)" };

// Distinct carts for counter, dealer, and cafe modes
let cart = { lines: [], discount: 0, reason: "", name: "", phone: "", clientId: null, clientType: "walk_in", billType: "counter" };
let selectedDealer: any = null;
let selectedCafe: any = null;

let dealerSearchQ = "";
let cafeSearchQ = "";
let dealerGroupFilter = "all"; // "all", "raw_beans", "roasted_beans", "chicory"
let cafeGroupFilter = "all";   // "all", "shared", "other"
let duesFilter = "all";        // "all", "dealer", "cafe"
let clientsFilter = "all";     // "all", "dealer", "cafe", "unsorted"

const TABS: [string, string, () => boolean][] = [
  ["dealer", "Dealer bill", () => true],
  ["cafe", "Café bill", () => true],
  ["dues", "Who owes", () => true],
  ["clients", "Clients", () => true],
  ["sell", "Sell", () => can("counter.sell")],
  ["bills", "Today's bills", () => can("counter.sell") || can("counter.refund")],
  ["close", "Day close", () => can("counter.close") || can("counter.close_approve") || can("counter.reopen_day")],
  ["customers", "Customers", cxCan.customers],
  ["cash", "Cash today 🔒", cxCan.cash],
];

const sub = () => (S.sub ||= { tab: "dealer" });

export const counter = {
  paths() {
    const s = sub();
    return [
      "/counter/setup",
      "/clients",
      "/dues",
      "/blends",
      ...(s.tab === "bills" ? [`/counter/sales${s.day ? "?day=" + s.day : ""}`] : []),
      ...(s.tab === "close" ? ["/counter/cash-close"] : []),
      ...(s.tab === "customers" ? [`/counter/customers?q=${encodeURIComponent(s.q || "")}`] : []),
    ];
  },
  render() {
    const s = sub(), setup = S.data["/counter/setup"] || { items: [], incoming: [], settings: {}, stock: {} };
    // Safety fallback for setup
    setup.items = Array.isArray(setup.items) ? setup.items : [];
    setup.incoming = Array.isArray(setup.incoming) ? setup.incoming : [];
    setup.settings = setup.settings || { custom_coffee_rate_paise: 90000, custom_chicory_rate_paise: 25000, custom_chicory_product_id: 10 };
    setup.stock = setup.stock || { coffee_ground: 40, coffee_beans: 60, custom_chicory: 30, resting: {} };

    const tabs = `<div class="tabs">${TABS.filter(([, , ok]) => ok()).map(([k, l]) => `<button class="tab ${s.tab === k ? "on" : ""}" data-ctab="${k}">${l}</button>`).join("")}</div>`;
    const incoming = setup.incoming.length && can("counter.sell") ? `<div class="hint warn row between">📦 ${setup.incoming.length} transfer${setup.incoming.length > 1 ? "s" : ""} from production waiting to be counted in.
      <span class="row">${setup.incoming.map((x) => `<button class="btn btn-small" data-receive="${x.id}:${x.kg_sent}">Receive ${h(x.transit_code || "#" + x.id)} · ${num(x.kg_sent, 3)} kg</button>`).join("")}</span></div>` : "";

    const views: Record<string, () => string> = {
      dealer: () => dealerTab(setup),
      cafe: () => cafeTab(setup),
      dues: () => duesTab(),
      clients: () => clientsTab(),
      sell: () => sellTab(setup),
      bills: () => billsTab(),
      close: () => closeTab(),
      customers: () => customersTab(),
      cash: () => cashTab(),
    };

    const renderFn = views[s.tab] || views.dealer;
    const body = renderFn();
    return pageHead(`${S.me?.settings?.short_name || "VTC"} / ${S.me?.user?.role_name || "Sales"}`, "Counter Sales", "B2B Dealer, Café, and Retail billing with live client outstandings.") + tabs + incoming + body;
  },
};

// ---------------------------------------------------------------- DEALER BILL
function dealerTab(setup: any) {
  const clientsData = S.data["/clients"]?.clients || [];
  const dealers = clientsData.filter((c: any) => c.active && (c.client_type === "dealer" || c.client_type === "unsorted"));
  const unsortedCount = clientsData.filter((c: any) => c.active && c.client_type === "unsorted").length;

  const filteredDealers = dealers.filter((d: any) => {
    if (!dealerSearchQ) return true;
    const q = dealerSearchQ.toLowerCase();
    return d.name.toLowerCase().includes(q) || (d.phone && d.phone.includes(q)) || (d.contact && d.contact.toLowerCase().includes(q));
  });

  let pickerHtml = "";
  if (!selectedDealer) {
    pickerHtml = `
      <section class="card mb-b">
        <div class="row between">
          <h3 class="m0">Choose the dealer first</h3>
          <button class="btn btn-primary btn-small" data-newdealer="+ New dealer">+ New dealer</button>
        </div>
        <div class="mt">
          <input type="search" class="item-search" placeholder="Search by name or phone..." value="${h(dealerSearchQ)}" data-dealersearch aria-label="Search dealers"/>
        </div>
        <div class="client-pick">
          ${filteredDealers.length ? filteredDealers.map((d: any) => `
            <button class="pick-row" data-pickdealer="${d.id}">
              <div>
                <b>${h(d.name)}</b>
                ${d.phone ? `<br><span class="tiny">${h(d.phone)}</span>` : ""}
              </div>
              <div class="row">
                ${d.balance_paise > 0 ? `<span class="due-tag">· old dues ${rupee(d.balance_paise)}</span>` : '<span class="tiny">No dues</span>'}
                <span class="icon-btn">→</span>
              </div>
            </button>
          `).join("") : '<div class="empty">No dealers matching search. Click "+ New dealer".</div>'}
        </div>
        ${unsortedCount > 0 ? `<p class="tiny sub">${unsortedCount} older client is not marked Dealer or Café yet — sort them in Clients → Clients.</p>` : ""}
      </section>
    `;
  } else {
    pickerHtml = `
      <div class="hint row between mb-b" style="border-left: 4px solid var(--accent); background: rgba(245, 158, 11, 0.08);">
        <div>
          <b>Dealer: ${h(selectedDealer.name)}</b>
          ${selectedDealer.phone ? ` · ${h(selectedDealer.phone)}` : ""}
          ${selectedDealer.gstin ? ` · GSTIN ${h(selectedDealer.gstin)}` : ""}
          ${selectedDealer.balance_paise > 0 ? ` · <span class="due-tag">Outstanding: ${rupee(selectedDealer.balance_paise)}</span>` : " · No dues"}
        </div>
        <button class="btn btn-small" data-cleardealer="1">Change dealer</button>
      </div>
    `;
  }

  // Items filtering
  const allItems = setup.items || [];
  let displayItems = allItems;
  if (dealerGroupFilter === "raw_beans") displayItems = allItems.filter((i: any) => i.item_group === "raw_beans");
  else if (dealerGroupFilter === "roasted_beans") displayItems = allItems.filter((i: any) => i.item_group === "roasted_beans");
  else if (dealerGroupFilter === "chicory") displayItems = allItems.filter((i: any) => i.item_group === "chicory");

  const subtotal = cart.lines.reduce((s, l) => s + l.amount, 0);
  const total = subtotal - cart.discount;

  return `
    ${pickerHtml}
    <div class="pos">
      <section class="pos-items-area">
        <div class="seg-tabs">
          <button class="seg-tab-btn ${dealerGroupFilter === "all" ? "active" : ""}" data-dgroup="all">All</button>
          <button class="seg-tab-btn ${dealerGroupFilter === "raw_beans" ? "active" : ""}" data-dgroup="raw_beans">Raw beans</button>
          <button class="seg-tab-btn ${dealerGroupFilter === "roasted_beans" ? "active" : ""}" data-dgroup="roasted_beans">Roasted beans</button>
          <button class="seg-tab-btn ${dealerGroupFilter === "chicory" ? "active" : ""}" data-dgroup="chicory">Chicory</button>
        </div>
        <div class="pos-items">
          ${displayItems.map((i: any) => `
            <button class="pos-tile" data-additem="${i.id}">
              <b>${h(i.name)}</b>
              <span class="pos-price">${rupee(i.price_per_kg_paise)}<small>/kg</small></span>
              <span class="tiny">${i.stock_kg > 0 ? `${num(i.stock_kg, 2)} kg available` : "Available to weigh"}</span>
            </button>
          `).join("")}
        </div>
      </section>

      <section class="card pos-bill">
        <h3>Dealer bill</h3>
        ${selectedDealer ? `<div class="sub">Billing to: <b>${h(selectedDealer.name)}</b></div>` : ""}
        ${cart.lines.length ? `<div class="bill-lines">${cart.lines.map((l, i) => `
          <div class="bill-line">
            <div>
              <b>${h(l.name)}</b><br>
              <span class="tiny">${num(l.weight_kg * 1000, 0)} g (${num(l.weight_kg, 2)} kg) × ${rupee(l.rate)}/kg</span>
            </div>
            <b>${rupee(l.amount)}</b>
            <button class="icon-btn" data-rmline="${i}" aria-label="Remove">✕</button>
          </div>
        `).join("")}</div>` : '<div class="empty">Tap an item to add it.</div>'}

        <div class="kv"><span>Subtotal</span><b>${rupee(subtotal)}</b></div>
        ${cart.discount ? `<div class="kv"><span>Discount</span><b>− ${rupee(cart.discount)}</b></div>` : ""}
        <div class="kv total"><span>Bill total</span><b>${rupee(total)}</b></div>

        <div class="row mt">
          ${cart.lines.length ? '<button class="btn" data-action="clearCart">Clear</button>' : ""}
          <button class="btn btn-primary btn-big grow" data-action="dealerPay" ${selectedDealer && cart.lines.length ? "" : "disabled"}>
            ${selectedDealer ? `Take payment ${rupee(total)}` : "Choose the dealer first"}
          </button>
        </div>
      </section>
    </div>
  `;
}

// ---------------------------------------------------------------- CAFÉ BILL
function cafeTab(setup: any) {
  const clientsData = S.data["/clients"]?.clients || [];
  const cafes = clientsData.filter((c: any) => c.active && (c.client_type === "cafe" || c.client_type === "unsorted"));
  const unsortedCount = clientsData.filter((c: any) => c.active && c.client_type === "unsorted").length;

  const filteredCafes = cafes.filter((c: any) => {
    if (!cafeSearchQ) return true;
    const q = cafeSearchQ.toLowerCase();
    return c.name.toLowerCase().includes(q) || (c.phone && c.phone.includes(q)) || (c.contact && c.contact.toLowerCase().includes(q));
  });

  let pickerHtml = "";
  if (!selectedCafe) {
    pickerHtml = `
      <section class="card mb-b">
        <div class="row between">
          <h3 class="m0">Choose the café first — their own blends and agreed prices then show</h3>
          <button class="btn btn-primary btn-small" data-newcafe="+ New café">+ New café</button>
        </div>
        <div class="mt">
          <input type="search" class="item-search" placeholder="Search by name or phone..." value="${h(cafeSearchQ)}" data-cafesearch aria-label="Search cafes"/>
        </div>
        <div class="client-pick">
          ${filteredCafes.length ? filteredCafes.map((c: any) => `
            <button class="pick-row" data-pickcafe="${c.id}">
              <div>
                <b>${h(c.name)}</b>
                ${c.phone ? `<br><span class="tiny">${h(c.phone)}</span>` : ""}
              </div>
              <div class="row">
                ${c.balance_paise > 0 ? `<span class="due-tag">· old dues ${rupee(c.balance_paise)}</span>` : '<span class="tiny">No dues</span>'}
                <span class="icon-btn">→</span>
              </div>
            </button>
          `).join("") : '<div class="empty">No cafés matching search. Click "+ New café".</div>'}
        </div>
        ${unsortedCount > 0 ? `<p class="tiny sub">${unsortedCount} older client is not marked Dealer or Café yet — sort them in Clients → Clients.</p>` : ""}
      </section>
    `;
  } else {
    pickerHtml = `
      <div class="hint row between mb-b" style="border-left: 4px solid #6366f1; background: rgba(99, 102, 241, 0.08);">
        <div>
          <b>Café: ${h(selectedCafe.name)}</b>
          ${selectedCafe.phone ? ` · ${h(selectedCafe.phone)}` : ""}
          ${selectedCafe.balance_paise > 0 ? ` · <span class="due-tag">Outstanding: ${rupee(selectedCafe.balance_paise)}</span>` : " · No dues"}
        </div>
        <button class="btn btn-small" data-clearcafe="1">Change café</button>
      </div>
    `;
  }

  const blends = S.data["/blends"] || [];
  const items = setup.items || [];
  const sharedBlends = blends.filter((b: any) => b.is_shared);

  let catalogItems: any[] = [];
  if (cafeGroupFilter === "all" || cafeGroupFilter === "shared") {
    catalogItems = sharedBlends.map((b: any) => ({ ...b, is_blend: true, price_per_kg_paise: b.base_price_paise }));
  }
  if (cafeGroupFilter === "all" || cafeGroupFilter === "other") {
    catalogItems = catalogItems.concat(items.filter((i: any) => i.item_group !== "raw_beans"));
  }

  const subtotal = cart.lines.reduce((s, l) => s + l.amount, 0);
  const total = subtotal - cart.discount;

  return `
    ${pickerHtml}
    <div class="pos">
      <section class="pos-items-area">
        <div class="seg-tabs">
          <button class="seg-tab-btn ${cafeGroupFilter === "all" ? "active" : ""}" data-cgroup="all">All</button>
          <button class="seg-tab-btn ${cafeGroupFilter === "shared" ? "active" : ""}" data-cgroup="shared">Shared blends</button>
          <button class="seg-tab-btn ${cafeGroupFilter === "other" ? "active" : ""}" data-cgroup="other">Other</button>
        </div>
        <div class="pos-items">
          ${catalogItems.map((i: any) => `
            <button class="pos-tile" data-additem="${i.id}" ${i.is_blend ? `data-blend="1"` : ""}>
              <b>${h(i.name)}</b>
              <span class="pos-price">${rupee(i.price_per_kg_paise)}<small>/kg</small></span>
              <span class="tiny">${i.is_blend ? "Signature roast ratio" : "Extras & Powder"}</span>
            </button>
          `).join("")}
        </div>
      </section>

      <section class="card pos-bill">
        <h3>Café bill</h3>
        ${selectedCafe ? `<div class="sub">Billing to: <b>${h(selectedCafe.name)}</b></div>` : ""}
        ${cart.lines.length ? `<div class="bill-lines">${cart.lines.map((l, i) => `
          <div class="bill-line">
            <div>
              <b>${h(l.name)}</b><br>
              <span class="tiny">${num(l.weight_kg * 1000, 0)} g (${num(l.weight_kg, 2)} kg) × ${rupee(l.rate)}/kg</span>
            </div>
            <b>${rupee(l.amount)}</b>
            <button class="icon-btn" data-rmline="${i}" aria-label="Remove">✕</button>
          </div>
        `).join("")}</div>` : '<div class="empty">Tap an item to add it.</div>'}

        <div class="kv"><span>Subtotal</span><b>${rupee(subtotal)}</b></div>
        ${cart.discount ? `<div class="kv"><span>Discount</span><b>− ${rupee(cart.discount)}</b></div>` : ""}
        <div class="kv total"><span>Bill total</span><b>${rupee(total)}</b></div>

        <div class="row mt">
          ${cart.lines.length ? '<button class="btn" data-action="clearCart">Clear</button>' : ""}
          <button class="btn btn-primary btn-big grow" data-action="cafePay" ${selectedCafe && cart.lines.length ? "" : "disabled"}>
            ${selectedCafe ? `Take payment ${rupee(total)}` : "Choose the café first"}
          </button>
        </div>
      </section>
    </div>
  `;
}

// ---------------------------------------------------------------- WHO OWES (Market Dues)
function duesTab() {
  const d = S.data["/dues"] || { clients: [], totals: { balance_paise: 0, "0_30": 0, "31_60": 0, "61_90": 0, "90_plus": 0 }, overdue_days: 30 };
  const t = d.totals || {};
  const od = d.overdue_days || 30;

  const clients = d.clients || [];
  const dealerClients = clients.filter((c: any) => c.client_type === "dealer");
  const cafeClients = clients.filter((c: any) => c.client_type === "cafe");

  const dealerTotal = dealerClients.reduce((sum: number, c: any) => sum + (c.balance_paise > 0 ? c.balance_paise : 0), 0);
  const cafeTotal = cafeClients.reduce((sum: number, c: any) => sum + (c.balance_paise > 0 ? c.balance_paise : 0), 0);

  let displayClients = clients;
  if (duesFilter === "dealer") displayClients = dealerClients;
  else if (duesFilter === "cafe") displayClients = cafeClients;

  return `
    <div class="grid mb-b">
      <section class="card stat-due span4">
        <div class="tiny upper">TOTAL DUES IN THE MARKET</div>
        <div class="due-big">${rupee(t.balance_paise)}</div>
        <div class="tiny mt" style="color: var(--dim);">Dealers ${rupee(dealerTotal)} · Cafés ${rupee(cafeTotal)}</div>
      </section>
      <section class="card span8">
        <div class="tiny upper">HOW OLD</div>
        <div class="aging">
          <div class="age-cell a0"><span>0–30 days</span><b>${rupee(t["0_30"])}</b></div>
          <div class="age-cell a1"><span>31–60 days</span><b>${rupee(t["31_60"])}</b></div>
          <div class="age-cell a2"><span>61–90 days</span><b>${rupee(t["61_90"])}</b></div>
          <div class="age-cell a3"><span>Over 90 days</span><b>${rupee(t["90_plus"])}</b></div>
        </div>
        <p class="tiny sub mt">Warning while billing when dues are older than ${od} days.</p>
      </section>
    </div>

    <div class="toolbar between">
      <div class="seg-tabs m0" style="border: 0; padding: 0;">
        <button class="seg-tab-btn ${duesFilter === "all" ? "active" : ""}" data-duesfilter="all">All</button>
        <button class="seg-tab-btn ${duesFilter === "dealer" ? "active" : ""}" data-duesfilter="dealer">Dealers (${dealerClients.length})</button>
        <button class="seg-tab-btn ${duesFilter === "cafe" ? "active" : ""}" data-duesfilter="cafe">Cafés (${cafeClients.length})</button>
      </div>
      <div class="row">
        <button class="btn btn-small wa" data-action="waReminders">${icon("message-circle", 14)} WhatsApp reminders</button>
        <button class="btn btn-small" data-action="monthPdf">↓ Month-end statements (PDF)</button>
        <button class="btn btn-small" data-action="excelDues">↓ Excel of dues</button>
      </div>
    </div>

    <section class="card">
      ${table(["CLIENT", "TYPE", "BALANCE", "0-30", "31-60", "61-90", "90+", "OLDEST", ""], displayClients.map((r: any) => `
        <tr>
          <td>
            <b>${h(r.name)}</b>
            ${r.phone ? `<br><span class="tiny">${h(r.phone)}</span>` : ""}
          </td>
          <td><span class="chip">${r.client_type === "cafe" ? "Café" : "Dealer"}</span></td>
          <td><b class="${r.balance_paise > 0 ? "neg" : ""}">${rupee(r.balance_paise)}</b></td>
          <td>${rupee(r.buckets?.["0_30"])}</td>
          <td>${rupee(r.buckets?.["31_60"])}</td>
          <td>${rupee(r.buckets?.["61_90"])}</td>
          <td>${rupee(r.buckets?.["90_plus"])}</td>
          <td>${r.oldest_days ? `<span class="${r.oldest_days > od ? "neg" : ""}">${r.oldest_days} days</span>` : "—"}</td>
          <td class="nowrap">
            <button class="btn btn-small btn-primary" data-collect="${r.id}">Collect</button>
            <button class="btn btn-small" data-statement="${r.id}">Statement</button>
            ${r.phone && r.balance_paise > 0 ? `<button class="btn btn-small wa" data-remind="${r.id}">WhatsApp</button>` : ""}
          </td>
        </tr>
      `), "No market dues outstanding.")}
    </section>
  `;
}

// ---------------------------------------------------------------- CLIENT DIRECTORY
function clientsTab() {
  const d = S.data["/clients"] || { clients: [], overdue_days: 30 };
  const clients = d.clients || [];
  const od = d.overdue_days || 30;

  const dealers = clients.filter((c: any) => c.client_type === "dealer");
  const cafes = clients.filter((c: any) => c.client_type === "cafe");
  const unsorted = clients.filter((c: any) => c.client_type === "unsorted");

  let displayClients = clients;
  if (clientsFilter === "dealer") displayClients = dealers;
  else if (clientsFilter === "cafe") displayClients = cafes;
  else if (clientsFilter === "unsorted") displayClients = unsorted;

  return `
    <div class="toolbar between">
      <div class="seg-tabs m0" style="border: 0; padding: 0;">
        <button class="seg-tab-btn ${clientsFilter === "all" ? "active" : ""}" data-cfilter="all">All</button>
        <button class="seg-tab-btn ${clientsFilter === "dealer" ? "active" : ""}" data-cfilter="dealer">Dealers (${dealers.length})</button>
        <button class="seg-tab-btn ${clientsFilter === "cafe" ? "active" : ""}" data-cfilter="cafe">Cafés (${cafes.length})</button>
        <button class="seg-tab-btn ${clientsFilter === "unsorted" ? "active" : ""}" data-cfilter="unsorted">Not sorted (${unsorted.length})</button>
      </div>
      <div class="row">
        <button class="btn btn-primary btn-small" data-newdealer="+ New dealer">+ New dealer</button>
        <button class="btn btn-primary btn-small" data-newcafe="+ New café">+ New café</button>
      </div>
    </div>

    ${unsorted.length ? `
      <div class="hint warn mb-b">
        ${unsorted.length} client from before needs to be marked Dealer or Café before billing. Use the Dealer / Café buttons in the list.
      </div>
    ` : ""}

    <section class="card">
      ${table(["CLIENT", "TYPE", "CONTACT", "GSTIN / STATE", "BALANCE", "OLDEST", ""], displayClients.map((r: any) => `
        <tr class="${r.active ? "" : "muted"}">
          <td>
            <b>${h(r.name)}</b>${r.active ? "" : " " + chip("hidden")}
            ${r.address ? `<br><span class="tiny">${h(r.address)}</span>` : ""}
          </td>
          <td>
            ${r.client_type === "unsorted" ? `
              <div class="row">
                <button class="btn btn-small" data-settype="${r.id}:dealer">Dealer</button>
                <button class="btn btn-small" data-settype="${r.id}:cafe">Café</button>
              </div>
            ` : `<span class="chip ${r.client_type === "cafe" ? "tea" : ""}">${r.client_type === "cafe" ? "Café" : "Dealer"}</span>`}
          </td>
          <td>
            ${h(r.contact || "—")}
            ${r.phone ? `<br><span class="tiny">${h(r.phone)}</span>` : ""}
          </td>
          <td>
            ${h(r.gstin || "—")}
            ${r.state_code && r.state_code !== "36" ? ' <span class="chip">IGST</span>' : ' <span class="tiny">Telangana</span>'}
          </td>
          <td><b class="${r.balance_paise > 0 ? "neg" : ""}">${rupee(r.balance_paise)}</b></td>
          <td>${r.oldest_days ? `<span class="${r.oldest_days > od ? "neg" : ""}">${r.oldest_days} days</span>` : "—"}</td>
          <td class="nowrap">
            <button class="btn btn-small" data-editclient="${r.id}">Edit</button>
            <button class="btn btn-small" data-statement="${r.id}">Statement</button>
            <button class="btn btn-small btn-quiet" data-toggleactive="${r.id}:${r.active ? 0 : 1}">${r.active ? "Hide" : "Show"}</button>
          </td>
        </tr>
      `), "No clients found in this category.")}
    </section>
  `;
}

// ---------------------------------------------------------------- RETAIL SELL
function sellTab(setup: any) {
  const items = setup.items.filter((i: any) => i.active);
  const onWay = setup.incoming.reduce((n: number, x: any) => n + x.kg_sent, 0);
  const whyOut = (i: any) => (i.kind === "product" ? "None at the counter"
    : !(setup.stock?.[i.kind] > 0) ? `No ${i.kind === "coffee_ground" ? "ground coffee" : "roasted beans"} at the counter`
    : "No chicory at the counter — send chicory too") + (onWay ? ` · ${num(onWay, 3)} kg on the way: press Receive above` : "");

  if (setup.day_closed) return `<div class="closed-banner"><div><b>Counter closed for today</b><br><span class="sub">Today's bills were closed at ${new Date(setup.day_closed.created_at).toLocaleTimeString("en-IN", { timeStyle: "short" })}. Selling opens again tomorrow${can("counter.reopen_day") ? ", or reopen it from Day close" : ""}.</span></div>
    <button class="btn" data-ctab="close">See today's register →</button></div>`;
  if (!items.length) return `<div class="empty"><b>No price list yet</b>${can("system.settings") ? "Add the counter's items and prices." + goto("admin", "prices", "Open Prices") : "Ask the admin to publish the counter price list."}</div>`;

  const st = setup.settings || {};
  const customReady = Boolean(st.custom_coffee_rate_paise && st.custom_chicory_rate_paise && st.custom_chicory_product_id);
  const subtotal = cart.lines.reduce((s, l) => s + l.amount, 0);
  const total = subtotal - cart.discount;

  return `<div class="pos">
    <section class="pos-items">${items.map((i: any) => `<button class="pos-tile ${i.stock_kg <= 0 ? "out" : ""}" data-additem="${i.id}">
        <b>${h(i.name)}</b><span class="pos-price">${rupee(i.price_per_kg_paise)}<small>/kg</small></span>
        <span class="tiny">${i.stock_kg > 0 ? `${num(i.stock_kg, 2)} kg at counter` : whyOut(i)}</span></button>`).join("")}
        <button class="pos-tile other" data-action="customBlend"><b>Others…</b><span class="pos-price">Any ratio</span>
        <span class="tiny">${customReady ? "e.g. 80/20, 90/10 coffee : chicory" : "Not set up — admin sets the rates in Prices"}</span></button>
        ${!customReady && can("system.settings") ? goto("admin", "prices", "Set custom blend rates") : ""}</section>
    <section class="card pos-bill"><h3>Current bill</h3>
      ${cart.lines.length ? `<div class="bill-lines">${cart.lines.map((l, i) => `<div class="bill-line"><div><b>${h(l.name)}</b><br><span class="tiny">${num(l.weight_kg * 1000, 0)} g × ${rupee(l.rate)}/kg</span></div><b>${rupee(l.amount)}</b><button class="icon-btn" data-rmline="${i}" aria-label="Remove">✕</button></div>`).join("")}</div>`
        : '<div class="empty">Tap an item to add it.</div>'}
      <div class="kv"><span>Subtotal</span><b>${rupee(subtotal)}</b></div>
      ${cart.discount ? `<div class="kv"><span>Discount · ${h(cart.reason)}</span><b>− ${rupee(cart.discount)}</b></div>` : ""}
      <div class="kv total"><span>Total to collect</span><b>${rupee(total)}</b></div>
      <div class="form-grid mt">${field("cust_name", "Customer name (optional)", "text", `maxlength="60" value="${h(cart.name)}" data-cart="name"`)}${field("cust_phone", "Phone (optional)", "tel", `maxlength="15" value="${h(cart.phone)}" data-cart="phone" inputmode="tel"`)}</div>
      ${customerCard()}
      ${restWarning(setup)}
      <div class="row mt">${can("counter.discount") && cart.lines.length ? `<button class="btn" data-action="discount">Discount</button>` : ""}
        ${cart.lines.length ? '<button class="btn" data-action="clearCart">Clear</button>' : ""}
        <button class="btn btn-primary btn-big grow" data-action="pay" ${cart.lines.length ? "" : "disabled"}>Take payment ${rupee(total)}</button></div>
    </section></div>`;
}

function restWarning(setup: any) {
  const resting = (setup.stock && setup.stock.resting) || {};
  const need: Record<string, number> = { coffee_ground: 0, coffee_beans: 0 };
  for (const l of cart.lines) {
    const it = l.custom ? { kind: "coffee_ground", coffee_pct: l.custom.coffee_pct } : setup.items.find((i: any) => i.id === l.item_id);
    if (it && it.kind in need) need[it.kind] += l.weight_kg * (it.coffee_pct || 0) / 100;
  }
  const warn = Object.entries<any>(resting).filter(([pool, r]) => need[pool] > (setup.stock[pool] || 0) - r.kg + 1e-6);
  return warn.map(([pool, r]) => {
    const ready = Math.max(0, (setup.stock[pool] || 0) - r.kg), what = pool === "coffee_ground" ? "ground coffee" : "coffee beans";
    return `<div class="hint warn mt">${icon("hourglass", 16)} ${ready > 0.0005 ? `Only ${num(ready, 2)} kg of ${what} here has finished resting.` : `The ${what} here is still resting.`}
      This bill uses coffee that is ready from ${fmtDay(r.ready_from)}. Tell the customer, or sell less.</div>`;
  }).join("");
}

// ---------------------------------------------------------------- ADD ITEM TO BILL
async function addItem(id: any, isBlend = false) {
  let item: any;
  if (isBlend) {
    const blends = S.data["/blends"] || [];
    const b = blends.find((x: any) => x.id === Number(id));
    if (!b) return;
    item = { id: b.id, name: b.name, price_per_kg_paise: b.base_price_paise, kind: "coffee_ground", is_blend: true };
  } else {
    const setup = S.data["/counter/setup"] || { items: [] };
    item = setup.items.find((i: any) => i.id === Number(id));
  }
  if (!item) return;

  const r = await dialog({
    title: item.name, submit: "Add to bill",
    body: `<p class="sub">${rupee(item.price_per_kg_paise)} per kg${item.kind !== "product" && item.coffee_pct ? ` · ${item.coffee_pct}% coffee` : ""}</p>
      <div class="presets">${[100, 250, 500, 1000, 5000, 10000, 30000].map((g) => `<button type="button" class="preset" data-g="${g}">${g >= 1000 ? g / 1000 + " kg" : g + " g"}</button>`).join("")}</div>
      <div class="form-grid">${field("grams", "Weight on the scale (grams)", "number", 'required min="1" step="1" inputmode="numeric"')}${field("rupees", "…or customer asks for ₹", "number", 'min="1" step="1" inputmode="numeric"')}</div>
      <p class="tiny" id="calc">Enter the actual weighed quantity. A rupee amount gives the target weight to weigh.</p>`,
    onReady(d) {
      const g = d.querySelector("[name=grams]") as HTMLInputElement, rs = d.querySelector("[name=rupees]") as HTMLInputElement, calc = d.querySelector("#calc") as HTMLElement;
      const show = () => { if (g.value) calc.textContent = `${g.value} g (${(Number(g.value) / 1000).toFixed(2)} kg) = ${rupee(Math.round((Number(g.value) / 1000) * item.price_per_kg_paise))}`; };
      d.querySelectorAll("[data-g]").forEach((b: any) => b.addEventListener("click", () => { g.value = b.dataset.g; rs.value = ""; show(); }));
      rs.addEventListener("input", () => { if (rs.value) { g.value = String(Math.round((Number(rs.value) * 100 / item.price_per_kg_paise) * 1000)); show(); calc.textContent += " — weigh this amount"; } });
      g.addEventListener("input", show);
    },
  });
  if (!r) return;
  const w = Math.round(Number(r.grams)) / 1000;
  cart.lines.push({ item_id: item.id, name: item.name, rate: item.price_per_kg_paise, weight_kg: w, amount: Math.round(w * item.price_per_kg_paise) });
  cart.discount = 0;
  nav.rerender();
}

// ---------------------------------------------------------------- COLLECT PAYMENT DIALOG
async function collectDialog(cl: any) {
  const r = await dialog({
    title: `Collect from ${cl.name}`,
    submit: "Save payment",
    body: `
      <div class="kv"><span>${cl.balance_paise < 0 ? "Advance with us" : "Balance due"}</span><b class="${cl.balance_paise > 0 ? "neg" : ""}">${rupee(Math.abs(cl.balance_paise))}</b></div>
      <div class="form-grid mt">
        ${field("amount", "Amount received (₹)", "number", `required min="0.01" step="0.01" inputmode="decimal" value="${cl.balance_paise > 0 ? cl.balance_paise / 100 : ""}"`)}
        <div class="field">
          <label for="f_m">Payment Mode</label>
          <select id="f_m" name="method">
            <option value="CASH">Cash (goes into drawer)</option>
            <option value="UPI">UPI</option>
            <option value="BANK">Bank transfer (NEFT/RTGS)</option>
            <option value="CHEQUE">Cheque</option>
          </select>
        </div>
      </div>
      ${field("reference", "UTR / Cheque no. (optional)", "text", 'maxlength="60" autocomplete="off"')}
      ${field("note", "Note (optional)", "text", 'maxlength="200" autocomplete="off"')}
      <p class="tiny sub">Collections settle the oldest unpaid bills first.</p>
    `
  });
  if (!r) return;
  try {
    const res = await api(`/clients/${cl.id}/collect`, "POST", {
      amount_paise: Math.round(Number(r.amount) * 100),
      method: r.method,
      reference: r.reference,
      note: r.note
    });
    toast(`Payment recorded. ${cl.name}'s balance is now ${res.balance_paise <= 0 ? "clear" : rupee(res.balance_paise)}.`);
    await nav.go("counter", S.sub);
  } catch (e) {
    fail(e);
  }
}

// ---------------------------------------------------------------- STATEMENT DIALOG
async function statementDialog(cid: number) {
  try {
    const d = await api(`/clients/${cid}`);
    const cl = d.client, sm = d.summary;
    await dialog({
      title: `${cl.name} — statement`,
      submit: null,
      wide: true,
      body: `
        <div class="row mb">
          ${chip(sm.balance_paise < 0 ? `Advance ${rupee(-sm.balance_paise)}` : `Balance ${rupee(sm.balance_paise)}`, sm.balance_paise > 0 ? "bad" : "")}
          ${sm.oldest_days ? chip(`Oldest unpaid ${sm.oldest_days} days`, sm.oldest_days > 30 ? "bad" : "") : ""}
          ${cl.phone ? chip(cl.phone) : ""}
          ${cl.gstin ? chip("GSTIN " + cl.gstin) : ""}
        </div>
        ${table(["Date", "Details", "Charged (Dr)", "Received (Cr)", "Balance"], (d.statement || []).map((r: any) =>
          `<tr><td class="nowrap">${h(r.day)}</td><td>${h(r.what)}</td><td>${r.debit ? rupee(r.debit) : ""}</td><td>${r.credit ? rupee(r.credit) : ""}</td><td><b>${rupee(r.balance)}</b></td></tr>`), "No bills or payments yet.")}
        ${d.collections && d.collections.length ? `<h3 class="mt">Payments received</h3>${table(["When", "Amount", "How", "Reference", "By"], d.collections.map((k: any) =>
          `<tr><td class="nowrap">${when(k.createdAt || k.created_at)}</td><td>${rupee(k.amountPaise || k.amount_paise)}</td><td>${k.method}</td><td>${h(k.reference || "—")}</td><td>${h(k.createdByName || k.by_name || "Admin")}</td></tr>`))}` : ""}
        <div class="row mt">
          ${cl.phone && sm.balance_paise > 0 ? `<button type="button" class="btn wa" data-remind="${cl.id}">${icon("message-circle", 14)} Send reminder on WhatsApp</button>` : ""}
        </div>
      `,
      onReady(dl) {
        dl.querySelector("[data-remind]")?.addEventListener("click", () => triggerReminder(cl.id));
      }
    });
  } catch (e) {
    fail(e);
  }
}

async function triggerReminder(cid: number) {
  try {
    const r = await api(`/clients/${cid}/reminder`);
    if (r.link) {
      window.open(r.link, "_blank");
    } else {
      toast("No valid phone number for WhatsApp.");
    }
  } catch (e) {
    fail(e);
  }
}

// ---------------------------------------------------------------- CHECKOUT & PAYMENT
async function checkoutBill(client: any = null, billType = "counter") {
  const total = cart.lines.reduce((s, l) => s + l.amount, 0) - cart.discount;
  const methods = client ? ["CASH", "UPI", "CREDIT", "SPLIT"] : ["CASH", "UPI", "CARD", "SPLIT"];

  const r = await dialog({
    title: `Collect ${rupee(total)} · ${client ? client.name : "Walk-in"}`,
    submit: "Complete sale",
    body: `
      <div class="seg wide-seg" id="paySeg">
        ${methods.map((m, i) => `<label class="seg-btn ${i ? "" : "on"}"><input type="radio" name="method" value="${m}" ${i ? "" : "checked"}/> ${m === "CREDIT" ? "Charge to account (Credit)" : m === "SPLIT" ? "Cash + UPI" : m}</label>`).join("")}
      </div>
      <div id="payCash" class="mt">
        ${field("tendered", "Cash received (₹)", "number", `min="${total / 100}" step="any" inputmode="decimal"`)}
        <p class="tiny" id="change"></p>
      </div>
      <div id="payRef" hidden class="mt">
        ${field("reference", "UPI / Bank reference", "text", 'maxlength="40"')}
      </div>
      <div id="payCredit" hidden class="mt">
        <p class="hint">Will be added to ${client?.name || "dealer"}'s outstanding balance.</p>
        ${field("credit_reason", "Purchase order / Credit note", "text", 'maxlength="120"')}
      </div>
    `,
    onReady(d) {
      const sync = () => {
        const m = (d.querySelector("[name=method]:checked") as HTMLInputElement).value;
        d.querySelectorAll("#paySeg .seg-btn").forEach((l: any) => l.classList.toggle("on", l.querySelector("input").checked));
        (d.querySelector("#payCash") as HTMLElement).hidden = m !== "CASH";
        (d.querySelector("#payRef") as HTMLElement).hidden = m !== "UPI";
        (d.querySelector("#payCredit") as HTMLElement).hidden = m !== "CREDIT";
      };
      d.querySelectorAll("[name=method]").forEach((x) => x.addEventListener("change", sync));
      const ten = d.querySelector("[name=tendered]") as HTMLInputElement, ch = d.querySelector("#change") as HTMLElement;
      ten?.addEventListener("input", () => {
        const c = Math.round(Number(ten.value) * 100) - total;
        ch.textContent = c >= 0 ? `Give back ${rupee(c)}` : `Short by ${rupee(-c)}`;
      });
      sync();
    }
  });
  if (!r) return;

  let payments: any[] = [];
  if (r.method === "CREDIT") {
    payments = [];
  } else if (r.method === "CASH") {
    payments = [{ method: "CASH", amount_paise: total }];
  } else if (r.method === "UPI") {
    payments = [{ method: "UPI", amount_paise: total, reference: r.reference || "" }];
  } else {
    payments = [{ method: r.method, amount_paise: total }];
  }

  try {
    const payload = {
      lines: cart.lines.map((l) => ({ name: l.name, weight_kg: l.weight_kg, rate: l.rate, amount: l.amount })),
      total_paise: total,
      discount_paise: cart.discount,
      discount_reason: cart.reason,
      customer_name: client ? client.name : cart.name || "Walk-in Customer",
      customer_phone: client ? client.phone : cart.phone || "",
      client_id: client ? client.id : null,
      client_type: client ? client.client_type : "walk_in",
      bill_type: billType,
      payments
    };

    const bill = await api("/counter/sales", "POST", payload, newKey());
    cart = { lines: [], discount: 0, reason: "", name: "", phone: "", clientId: null, clientType: "walk_in", billType: "counter" };
    selectedDealer = null;
    selectedCafe = null;
    await nav.go("counter", S.sub);
    receiptDialog(bill, r.method === "CASH" && r.tendered ? Math.round(Number(r.tendered) * 100) - total : null, client?.phone || payload.customer_phone);
  } catch (e) {
    fail(e);
  }
}

// ---------------------------------------------------------------- RECEIPT (80mm ATPOS AT 402)
function receiptHtml(b: any, copy = false, label = "") {
  return counterBillHtml(b, copy, label);
}

export function printReceipt(b: any, copy = false, labels = ["CUSTOMER COPY"]) {
  const html = labels.map((l) => counterBillHtml(b, copy, l)).join("");
  printHtml80mm(html);
}

async function receiptDialog(b: any, change: number | null = null, phone = "") {
  await dialog({
    title: `Bill ${b.bill_no} saved`,
    submit: null,
    body: `
      ${change != null && change >= 0 ? `<div class="hint"><b>Give back ${rupee(change)}</b></div>` : ""}
      <div class="receipt-preview">${counterBillHtml(b, false, "CUSTOMER COPY")}</div>
      <div class="row mt" style="gap:8px; flex-wrap:wrap">
        <button type="button" class="btn" data-copies="1">🖨 Customer copy</button>
        <button type="button" class="btn btn-primary" data-copies="2">🖨 Customer + office copy</button>
        <button type="button" class="btn btn-quiet" data-directescpos title="Direct ESC/POS to ATPOS AT 402 via Bluetooth">📡 ATPOS BT Direct</button>
      </div>
      ${phone ? `<div class="row mt"><button type="button" class="btn wa" data-wa>${icon("message-circle", 16)} Send bill on WhatsApp</button></div>` : ""}
      <p class="tiny mt" style="text-align:center">Standard 80mm roll format for ATPOS AT 402 / ESC-POS.</p>
    `,
    onReady(d) {
      d.querySelectorAll("[data-copies]").forEach((x: any) =>
        x.addEventListener("click", () => printReceipt(b, false, x.dataset.copies === "2" ? ["CUSTOMER COPY", "OFFICE COPY"] : ["CUSTOMER COPY"]))
      );
      d.querySelector("[data-directescpos]")?.addEventListener("click", async () => {
        try {
          const connected = await AtposEscPosDriver.connectBluetooth();
          if (connected) {
            const bytes = AtposEscPosDriver.billToEscPos(b, "CUSTOMER COPY");
            const ok = await AtposEscPosDriver.sendRaw(bytes);
            if (ok) toast("Printed directly to ATPOS AT 402 via Bluetooth.");
            else toast("Failed sending ESC/POS commands to printer.", true);
          }
        } catch (e: any) {
          fail(e);
        }
      });
      d.querySelector("[data-wa]")?.addEventListener("click", () => waBill(b, phone));
    }
  });
}

// ---------------------------------------------------------------- TODAY'S BILLS
function billsTab() {
  const s = sub(), data = S.data[`/counter/sales${s.day ? "?day=" + s.day : ""}`] || { sales: [], totals: { bills: 0, total_paise: 0 }, day: new Date().toISOString().slice(0, 10) };
  return `
    <div class="toolbar between">
      <div class="row">
        ${chip(`${data.totals?.bills || 0} bills`)}
        ${chip(`${rupee(data.totals?.total_paise || 0)} total`)}
      </div>
      <input type="date" class="search" value="${data.day}" data-billday aria-label="Day"/>
    </div>
    <section class="card">
      ${table(["Bill", "Time", "Cashier", "Customer", "Paid by", "Total", "Status", ""], (data.sales || []).map((b: any) =>
        `<tr>
          <td><b>${h(b.bill_no)}</b></td>
          <td>${new Date(b.created_at).toLocaleTimeString("en-IN", { timeStyle: "short" })}</td>
          <td>${h(b.cashier)}</td>
          <td>${h(b.customer_name || "Walk-in")}</td>
          <td>${h(b.methods || "CASH")}</td>
          <td><b>${rupee(b.total_paise)}</b></td>
          <td>${tag(b.status === "PAID" ? "DONE" : b.status, b.status)}</td>
          <td><button class="btn btn-small" data-reprintbill="${b.id || b.bill_no}">🖨 Print</button></td>
        </tr>`), "No bills recorded on this day.")}
    </section>
  `;
}

// ---------------------------------------------------------------- DAY CLOSE
function closeTab() {
  const d = S.data["/counter/cash-close"] || [];
  return `
    <section class="card span6">
      <h3>Day close & Register</h3>
      <p class="sub">Tally the drawer cash and lock today's sales register.</p>
      <div class="kv"><span>Status</span><b>Ready for count</b></div>
      <div class="kv"><span>Opening drawer float</span><b>₹500.00</b></div>
    </section>
  `;
}

// ---------------------------------------------------------------- EVENTS
export async function counterClick(el: HTMLElement) {
  const ds = el.dataset, s = sub();
  if (ds.ctab) {
    S.sub = { tab: ds.ctab };
    if (ds.ctab === "cash") await openCash();
    return nav.go("counter", S.sub), true;
  }
  if (await cxClick(ds)) return true;

  if (ds.reprintbill) {
    const data = S.data[`/counter/sales${s.day ? "?day=" + s.day : ""}`] || { sales: [] };
    const bill = (data.sales || []).find((x: any) => String(x.id) === ds.reprintbill || String(x.bill_no) === ds.reprintbill);
    if (bill) receiptDialog(bill);
    return true;
  }

  // Dealer tab actions
  if (ds.newdealer) { if (await clientDialog(null, "dealer")) await nav.go("counter", s); return true; }
  if (ds.newcafe) { if (await clientDialog(null, "cafe")) await nav.go("counter", s); return true; }
  if (ds.pickdealer) {
    const clients = S.data["/clients"]?.clients || [];
    selectedDealer = clients.find((c: any) => c.id === Number(ds.pickdealer));
    return nav.rerender(), true;
  }
  if (ds.cleardealer) { selectedDealer = null; return nav.rerender(), true; }
  if (ds.dgroup) { dealerGroupFilter = ds.dgroup; return nav.rerender(), true; }
  if (ds.action === "dealerPay") return checkoutBill(selectedDealer, "dealer"), true;

  // Cafe tab actions
  if (ds.pickcafe) {
    const clients = S.data["/clients"]?.clients || [];
    selectedCafe = clients.find((c: any) => c.id === Number(ds.pickcafe));
    return nav.rerender(), true;
  }
  if (ds.clearcafe) { selectedCafe = null; return nav.rerender(), true; }
  if (ds.cgroup) { cafeGroupFilter = ds.cgroup; return nav.rerender(), true; }
  if (ds.action === "cafePay") return checkoutBill(selectedCafe, "cafe"), true;

  // Dues & Clients actions
  if (ds.duesfilter) { duesFilter = ds.duesfilter; return nav.rerender(), true; }
  if (ds.cfilter) { clientsFilter = ds.cfilter; return nav.rerender(), true; }
  if (ds.collect) {
    const clients = S.data["/clients"]?.clients || [];
    const cl = clients.find((c: any) => c.id === Number(ds.collect));
    if (cl) collectDialog(cl);
    return true;
  }
  if (ds.statement) {
    statementDialog(Number(ds.statement));
    return true;
  }
  if (ds.remind) {
    triggerReminder(Number(ds.remind));
    return true;
  }
  if (ds.editclient) {
    const clients = S.data["/clients"]?.clients || [];
    const cl = clients.find((c: any) => c.id === Number(ds.editclient));
    if (cl && (await clientDialog(cl))) await nav.go("counter", s);
    return true;
  }
  if (ds.toggleactive) {
    const [id, on] = ds.toggleactive.split(":");
    await api(`/clients/${id}`, "PUT", { active: on === "1" });
    await nav.go("counter", s);
    return true;
  }
  if (ds.settype) {
    const [id, ctype] = ds.settype.split(":");
    await api(`/clients/${id}`, "PUT", { client_type: ctype });
    toast(`Client categorized as ${ctype}.`);
    await nav.go("counter", s);
    return true;
  }

  // Cart operations
  if (ds.additem) return addItem(ds.additem, Boolean(ds.blend)), true;
  if (ds.rmline) { cart.lines.splice(Number(ds.rmline), 1); cart.discount = 0; return nav.rerender(), true; }
  if (ds.action === "clearCart") { cart.lines = []; cart.discount = 0; return nav.rerender(), true; }
  if (ds.action === "pay") return checkoutBill(null, "counter"), true;
  if (ds.action === "waReminders") {
    toast("Opening WhatsApp reminders for delinquent accounts...");
    const overdue = (S.data["/dues"]?.clients || []).filter((c: any) => c.oldest_days > 30);
    if (overdue.length) triggerReminder(overdue[0].id);
    else toast("No delinquent accounts over 30 days.");
    return true;
  }

  return false;
}

export function counterInput(el: HTMLInputElement) {
  if (el.hasAttribute("data-dealersearch")) {
    dealerSearchQ = el.value;
    nav.rerender();
    return true;
  }
  if (el.hasAttribute("data-cafesearch")) {
    cafeSearchQ = el.value;
    nav.rerender();
    return true;
  }
  if (el.dataset.cart) {
    (cart as any)[el.dataset.cart] = el.value;
    return true;
  }
  if (el.hasAttribute("data-billday")) { sub().day = el.value; nav.go("counter", S.sub); return true; }
  return false;
}

export async function counterSubmit(form: HTMLFormElement) {
  if (await cxSubmit(form)) return;
}


// ---------------------------------------------------------------- EXPORTS FOR CATALOGUE & SCAN
export function pricesTable(setup: any) {
  const items = setup.items || [];
  return `<section class="card">${table(["Item", "Kind", "Price / kg", "Blend recipe", "GST %", "Version", "Counter stock", ""], items.map((i: any) =>
    `<tr class="${i.active ? "" : "muted"}"><td><b>${h(i.name)}</b></td><td>${h(KIND_LABEL[i.kind] || i.kind)}</td><td>${rupee(i.price_per_kg_paise)}</td>
     <td>${i.kind === "product" ? h(i.stock_name || "") : `${i.coffee_pct}% coffee${i.coffee_pct < 100 ? ` + ${100 - i.coffee_pct}% ${h(i.chicory_name || "")}` : ""}`}</td>
     <td>${i.gst_rate_bp == null ? '<span class="tiny">not set</span>' : i.gst_rate_bp / 100 + "%"}</td><td>v${i.version || 1}</td><td>${num(i.stock_kg, 2)} kg</td>
     <td class="nowrap"><button class="btn btn-small" data-edititem="${i.id}">Edit</button> <button class="btn btn-small" data-toggleitem="${i.id}:${i.active ? 0 : 1}">${i.active ? "Hide" : "Show"}</button></td></tr>`), "No items yet. Add the counter's coffee blends, tea and other items.")}</section>`;
}

export async function itemDialog(item: any = null) {
  const setup = S.data["/counter/setup"] || { products: [] };
  const products = (setup.products || []).map((p: any) => [p.id, p.name]);
  const chicoryDefault = item?.chicory_product_id || (setup.products || []).find((p: any) => p.category === "CHICORY")?.id || "";
  const r = await dialog({ title: item ? `Edit ${item.name}` : "Add counter item", submit: item ? "Publish new price" : "Add item",
    body: `${field("name", "Name on bill", "text", `required maxlength="60" value="${h(item?.name || "")}"`)}
      ${select("kind", "What is it?", Object.entries(KIND_LABEL), "", item?.kind || "coffee_ground")}
      ${field("price", "Price per kg (₹, tax included)", "number", `required min="1" step="0.01" value="${item ? item.price_per_kg_paise / 100 : ""}"`)}
      <div id="mixBox" class="form-grid">${field("coffee_pct", "Coffee %", "number", `min="1" max="100" step="1" value="${item?.coffee_pct ?? 100}"`, "e.g. 70 for 70/30")}
        ${select("chicory_product_id", "Chicory comes from", [["", "Choose chicory stock…"], ...products], "", chicoryDefault)}</div>
      <div id="stockBox">${select("stock_product_id", "Stock product", [["", "Choose…"], ...products], "", item?.stock_product_id || "")}</div>
      <div class="form-grid">${field("gst", "GST % (from accountant)", "number", `min="0" max="28" step="0.01" value="${item?.gst_rate_bp != null ? item.gst_rate_bp / 100 : ""}"`)}${field("hsn", "HSN code", "text", `maxlength="12" value="${h(item?.hsn || "")}"`)}</div>`,
    onReady(d) {
      const k = d.querySelector("[name=kind]") as HTMLSelectElement, pct = d.querySelector("[name=coffee_pct]") as HTMLInputElement, chic = d.querySelector("[name=chicory_product_id]") as HTMLSelectElement, stock = d.querySelector("[name=stock_product_id]") as HTMLSelectElement;
      const sync = () => { const prod = k.value === "product"; (d.querySelector("#mixBox") as HTMLElement).hidden = prod; (d.querySelector("#stockBox") as HTMLElement).hidden = !prod;
        chic.required = !prod && Number(pct.value) < 100; stock.required = prod; pct.required = !prod; };
      k.addEventListener("change", sync); pct.addEventListener("input", sync); sync();
    } });
  if (!r) return;
  const body = { name: r.name, kind: r.kind, price_per_kg_paise: Math.round(Number(r.price) * 100), hsn: r.hsn,
    gst_rate_bp: r.gst === "" ? null : Math.round(Number(r.gst) * 100), coffee_pct: Number(r.coffee_pct || 0),
    chicory_product_id: r.chicory_product_id ? Number(r.chicory_product_id) : null, stock_product_id: r.stock_product_id ? Number(r.stock_product_id) : null };
  try {
    await api(item ? `/counter/items/${item.id}` : "/counter/items", item ? "PUT" : "POST", body);
    toast(item ? "New price published." : "Item added.");
    await nav.go(S.view, S.sub);
  } catch (e) { fail(e); }
}

export async function receiveTransfer(id: any, sent: any, code = "") {
  const r = await dialog({ title: `Receive ${code || "transfer #" + id}`, submit: "Confirm received",
    body: `<p class="sub">Production sent <b>${num(sent, 3)} kg</b>. Weigh or count what actually arrived.</p>${field("kg", "Kg received", "number", `required min="0" max="${sent}" step="0.001" value="${sent}"`)}<p class="tiny">Less than sent is recorded as a shortage and the managers are alerted.</p>` });
  if (!r) return;
  try { const x = await api(`/transfers/${id}/receive`, "POST", { kg_received: Number(r.kg) }); toast(x.state === "SHORTAGE" ? `Received with ${x.shortage_kg} kg short — managers alerted.` : "Received into counter stock."); await nav.go(S.view, S.sub); } catch (e) { fail(e); }
}
