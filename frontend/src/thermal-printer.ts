// ATPOS AT 402 & Standard 80mm ESC/POS Thermal Receipt Printer Driver
// Paper Width: 79.5 ± 0.5mm, Printable Width: 72mm (48 standard columns / 576 dots)
// Supports: Browser 80mm Thermal Print (window.print) + Direct Web Bluetooth / WebUSB ESC/POS

import { h, num } from "./ui.js";

const rupee = (p: any): string => "₹" + (Number(p || 0) / 100).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const LINE_LEN = 40; // 40-42 monospace chars comfortably fit ATPOS 80mm roll without wrapping
const SEP_LINE = "-".repeat(LINE_LEN);
const DBL_LINE = "=".repeat(LINE_LEN);

/** Format two text columns to left and right margins within line length */
function formatRow(left: string, right: string, maxLen = LINE_LEN): string {
  const l = String(left || "");
  const r = String(right || "");
  const spaces = Math.max(1, maxLen - l.length - r.length);
  return l + " ".repeat(spaces) + r;
}

/** Print HTML directly using the 80mm print stylesheet */
export function printHtml80mm(html: string) {
  let area = document.getElementById("print-area");
  if (!area) {
    area = document.createElement("div");
    area.id = "print-area";
    document.body.appendChild(area);
  }
  area.className = "thermal-receipt-print";
  area.innerHTML = html;
  window.print();
}

/** Generate HTML for Counter Sales Bill (Customer Copy or Office Copy) */
export function counterBillHtml(b: any, copy = false, label = "") {
  const hd = b.header || {
    company_name: "Vasavi Trading Company",
    company_address: "Vijayawada, Andhra Pradesh",
    company_gstin: "37ABCDE1234F1Z5",
    tax_setup_approved: true
  };
  const gst = hd.tax_setup_approved
    ? (b.gst_paise ? `<div class="r-row"><span>GST included</span><span>${rupee(b.gst_paise)}</span></div>` : "")
    : `<div class="r-note">Tax details pending accountant approval</div>`;
  const lines = b.lines || [];
  const payments = b.payments || [];

  return `
    <div class="receipt">
      <div class="r-c">
        <b style="font-size:15px">${h(hd.company_name)}</b><br>
        <span style="font-size:11px">COFFEE ROASTERS & MERCHANTS</span>
        ${hd.company_address ? `<br>${h(hd.company_address)}` : ""}
        ${hd.company_gstin ? `<br>GSTIN ${h(hd.company_gstin)}` : ""}
      </div>
      ${label ? `<div class="r-copy">${label}</div>` : ""}
      ${copy ? `<div class="r-c"><b>DUPLICATE COPY</b></div>` : ""}
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>Bill #${h(b.bill_no)}</span><span>${new Date(b.created_at || Date.now()).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "short" })}</span></div>
      <div>Cashier: ${h(b.cashier || "Admin")}${b.customer_name ? `<br>Customer: ${h(b.customer_name)}` : ""}</div>
      <div>${SEP_LINE}</div>
      ${lines.map((l: any) => `
        <div><b>${h(l.item_name)}</b></div>
        <div class="r-row">
          <span>${num(l.weight_kg, 3)} kg × ${rupee(l.rate_paise)}</span>
          <span><b>${rupee(l.amount_paise)}</b></span>
        </div>
      `).join("")}
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>Subtotal</span><span>${rupee(b.subtotal_paise || b.total_paise)}</span></div>
      ${b.discount_paise ? `<div class="r-row"><span>Discount</span><span>-${rupee(b.discount_paise)}</span></div>` : ""}
      <div class="r-row r-big"><span>TOTAL</span><span>${rupee(b.total_paise)}</span></div>
      ${gst}
      ${payments.map((p: any) => `
        <div class="r-row"><span>${p.method}${p.reference ? " " + h(p.reference) : ""}</span><span>${rupee(p.amount_paise || b.total_paise)}</span></div>
      `).join("")}
      <div>${SEP_LINE}</div>
      <div class="r-c">
        <b>Thank you! Visit again.</b><br>
        <span style="font-size:9px; color:#555">ATPOS AT 402 Thermal Receipt</span>
      </div>
    </div>
  `;
}

/** Generate HTML for Stock Inward Goods Receipt Note / Unloading Slip */
export function inwardReceiptHtml(r: any) {
  const soundBags = Math.max(0, (r.bags || 0) - (r.damaged || 0) - (r.partial || 0));
  const perBag = r.nominal_kg && r.bags ? (r.nominal_kg / r.bags) : 60;
  const soundKg = soundBags * perBag;
  const damagedKg = (r.damaged || 0) * perBag;
  const partialKg = r.partial_kg || 0;
  const dateStr = new Date(r.created_at || Date.now()).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "short" });

  return `
    <div class="receipt">
      <div class="r-c">
        <b style="font-size:15px">VASAVI TRADING COMPANY</b><br>
        <span style="font-size:11px">COFFEE MERCHANTS & ROASTERS</span><br>
        Vijayawada, Andhra Pradesh<br>
        GSTIN: 37ABCDE1234F1Z5
      </div>
      <div class="r-copy">GOODS INWARD RECEIPT / UNLOADING SLIP</div>
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>Receipt #${h(r.code || "#" + r.id)}</span><span>${dateStr}</span></div>
      <div>Supplier: <b>${h(r.source_name || "Supplier")}</b> (${h(r.source || "Direct")})</div>
      <div>Coffee: <b>${h(r.product || "Coffee Beans")}</b></div>
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>Sound Bags:</span><span>${soundBags} bags (${num(soundKg, 1)} kg)</span></div>
      ${r.damaged ? `<div class="r-row"><span>Damaged (Hold):</span><span>${r.damaged} bags (${num(damagedKg, 1)} kg)</span></div>` : ""}
      ${r.partial ? `<div class="r-row"><span>Partial Bags:</span><span>${r.partial} bags (${num(partialKg, 1)} kg)</span></div>` : ""}
      <div>${SEP_LINE}</div>
      <div class="r-row r-big"><span>TOTAL BAGS:</span><span>${r.bags} BAGS</span></div>
      <div class="r-row r-big"><span>TOTAL NOMINAL:</span><span>${num(r.nominal_kg, 1)} KG</span></div>
      <div class="r-row"><span>Usable Stock:</span><span><b>${num(r.allocatable_kg, 1)} kg</b></span></div>
      ${r.quarantine_kg > 0 ? `<div class="r-row"><span>Quarantined:</span><span>${num(r.quarantine_kg, 1)} kg</span></div>` : ""}
      <div>${SEP_LINE}</div>
      <div>Received By: ${h(r.created_by || "Admin")}</div>
      <div style="margin-top: 14px" class="r-row"><span>Unloaded by:</span><span>Weighed by:</span></div>
      <div class="r-row"><span>_______________</span><span>_______________</span></div>
      <div style="margin-top: 10px" class="r-row"><span>Driver Sign:</span><span>Manager Sign:</span></div>
      <div class="r-row"><span>_______________</span><span>_______________</span></div>
      <div>${SEP_LINE}</div>
      <div class="r-c">
        ATPOS AT 402 Thermal Receipt<br>
        Stock Verified & Stored
      </div>
    </div>
  `;
}

/** Generate HTML for Roast Batch Ticket */
export function roastBatchHtml(b: any) {
  const dateStr = new Date(b.created_at || Date.now()).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "short" });
  return `
    <div class="receipt">
      <div class="r-c">
        <b style="font-size:15px">VASAVI TRADING COMPANY</b><br>
        <span style="font-size:11px">DAILY ROASTING BATCH TICKET</span>
      </div>
      <div class="r-copy">BATCH #${h(b.code || "#" + b.id)}</div>
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>Date:</span><span>${dateStr}</span></div>
      <div class="r-row"><span>Roaster:</span><span><b>${h(b.roaster || "Ramesh")}</b></span></div>
      <div class="r-row"><span>Profile:</span><span>${h(b.profile || "Medium Roast")}</span></div>
      <div>${SEP_LINE}</div>
      <div><b>Input Coffee Lots:</b></div>
      <div>${h(b.inputs || "Green coffee beans")}</div>
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>Green Input:</span><span><b>${num(b.input_kg, 3)} kg</b></span></div>
      <div class="r-row r-big"><span>ROASTED OUTPUT:</span><span>${num(b.output_kg, 3)} KG</span></div>
      <div class="r-row"><span>Weight Loss:</span><span>${num(b.loss_pct, 1)}% (${num(b.loss_kg, 3)} kg)</span></div>
      <div class="r-row"><span>Duration:</span><span>${b.duration_min || 12} minutes</span></div>
      <div>${SEP_LINE}</div>
      <div class="r-row"><span>QC Status:</span><span><b>${b.qc_status || "QC_HOLD"}</b></span></div>
      ${b.abnormal ? `<div class="r-row" style="color:red"><span>FLAG:</span><span>ABNORMAL BATCH</span></div>` : ""}
      <div style="margin-top: 16px" class="r-row"><span>Roaster Sign:</span><span>QC Sign:</span></div>
      <div class="r-row"><span>_______________</span><span>_______________</span></div>
      <div>${SEP_LINE}</div>
      <div class="r-c">Attach to Roasted Coffee Container</div>
    </div>
  `;
}

// --------------------------------------------------------------- Direct ESC/POS Driver
export class AtposEscPosDriver {
  private static btDevice: any = null;
  private static btCharacteristic: any = null;

  /** Encode string to Uint8Array (ASCII / Latin1) */
  private static encodeText(str: string): Uint8Array {
    const bytes = new Uint8Array(str.length);
    for (let i = 0; i < str.length; i++) {
      bytes[i] = str.charCodeAt(i) & 0xff;
    }
    return bytes;
  }

  /** Connect via Web Bluetooth to ATPOS AT 402 */
  public static async connectBluetooth(): Promise<boolean> {
    const nav = navigator as any;
    if (!nav.bluetooth) {
      alert("Web Bluetooth is not supported in this browser. Please use Google Chrome or Microsoft Edge, or print via system dialog.");
      return false;
    }
    try {
      const device = await nav.bluetooth.requestDevice({
        acceptAllDevices: true,
        optionalServices: [
          "000018f0-0000-1000-8000-00805f9b34fb",
          "e7810a71-73ae-499d-8c15-faa9aef0c3f2",
          "49535343-fe7d-4ae5-8fa9-9fafd205e455"
        ]
      });
      const server = await device.gatt?.connect();
      if (!server) throw new Error("GATT connection failed");

      // Find writable characteristic
      const services = await server.getPrimaryServices();
      for (const service of services) {
        const chars = await service.getCharacteristics();
        for (const c of chars) {
          if (c.properties.write || c.properties.writeWithoutResponse) {
            this.btDevice = device;
            this.btCharacteristic = c;
            return true;
          }
        }
      }
      throw new Error("No writable characteristic found on printer");
    } catch (err: any) {
      console.warn("Bluetooth connection cancelled or failed:", err);
      return false;
    }
  }

  /** Send raw ESC/POS commands to printer if connected */
  public static async sendRaw(commands: Uint8Array): Promise<boolean> {
    if (!this.btCharacteristic) return false;
    try {
      const chunkSize = 100;
      for (let i = 0; i < commands.length; i += chunkSize) {
        const chunk = commands.slice(i, i + chunkSize);
        if (this.btCharacteristic.writeValueWithoutResponse) {
          await this.btCharacteristic.writeValueWithoutResponse(chunk);
        } else {
          await this.btCharacteristic.writeValue(chunk);
        }
      }
      return true;
    } catch (err) {
      console.warn("Failed sending to Bluetooth printer:", err);
      return false;
    }
  }

  /** Convert Bill to ESC/POS bytes for ATPOS AT 402 */
  public static billToEscPos(b: any, label = "CUSTOMER COPY"): Uint8Array {
    const cmds: number[] = [];
    const addStr = (s: string) => {
      for (let i = 0; i < s.length; i++) cmds.push(s.charCodeAt(i) & 0xff);
    };

    // ESC @ (Initialize printer)
    cmds.push(0x1b, 0x40);

    // ESC a 1 (Center align)
    cmds.push(0x1b, 0x61, 0x01);
    // GS ! 0x11 (Double width + height for title)
    cmds.push(0x1d, 0x21, 0x11);
    addStr("VASAVI TRADING CO.\n");
    // GS ! 0x00 (Normal font)
    cmds.push(0x1d, 0x21, 0x00);
    addStr("Coffee Roasters & Merchants\n");
    addStr("Vijayawada, Andhra Pradesh\n");
    addStr("GSTIN: 37ABCDE1234F1Z5\n");
    if (label) addStr(`[ ${label} ]\n`);
    addStr(SEP_LINE + "\n");

    // ESC a 0 (Left align)
    cmds.push(0x1b, 0x61, 0x00);
    const dateStr = new Date(b.created_at || Date.now()).toLocaleString("en-IN", { dateStyle: "short", timeStyle: "short" });
    addStr(formatRow(`Bill #${b.bill_no}`, dateStr) + "\n");
    addStr(`Cashier: ${b.cashier || "Admin"}\n`);
    if (b.customer_name) addStr(`Customer: ${b.customer_name}\n`);
    addStr(SEP_LINE + "\n");

    // Items
    const lines = b.lines || [];
    for (const l of lines) {
      addStr(`${l.item_name}\n`);
      const qtyStr = `${num(l.weight_kg, 3)} kg x ${rupee(l.rate_paise)}`;
      const amtStr = rupee(l.amount_paise);
      addStr(formatRow("  " + qtyStr, amtStr) + "\n");
    }
    addStr(SEP_LINE + "\n");

    // Subtotal & Discount
    addStr(formatRow("Subtotal", rupee(b.subtotal_paise || b.total_paise)) + "\n");
    if (b.discount_paise) {
      addStr(formatRow("Discount", "-" + rupee(b.discount_paise)) + "\n");
    }

    // ESC E 1 (Bold on)
    cmds.push(0x1b, 0x45, 0x01);
    // GS ! 0x01 (Double height for Total)
    cmds.push(0x1d, 0x21, 0x01);
    addStr(formatRow("TOTAL", rupee(b.total_paise)) + "\n");
    cmds.push(0x1d, 0x21, 0x00);
    cmds.push(0x1b, 0x45, 0x00);

    // Payments
    const payments = b.payments || [];
    for (const p of payments) {
      addStr(formatRow(`Paid via ${p.method}`, rupee(p.amount_paise || b.total_paise)) + "\n");
    }

    // Footer
    addStr(SEP_LINE + "\n");
    cmds.push(0x1b, 0x61, 0x01); // Center
    addStr("Thank you! Visit again.\n");
    addStr("ATPOS AT 402 Thermal Receipt\n\n\n\n");

    // GS V 66 0 (Feed and cut)
    cmds.push(0x1d, 0x56, 0x42, 0x00);

    return new Uint8Array(cmds);
  }
}
