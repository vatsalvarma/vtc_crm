# Vasavi Trading Company — Coffee CRM Workspace

A high-performance modern React 19 + TypeScript + Vite + Tailwind CSS portal for **Vasavi Trading Company (ESTD. 1965)**, South Indian Commercial Roastery, Blending Mill & POS.

---

## ☕ Key Functional Modules

1. **Executive Dashboard (`overview`)**
   - Live revenue metrics & CountUp animations
   - Hourly Throughput area charts (Drum Roast kg vs. Counter POS kg)
   - Commercial Blend Ratio distribution (80:20, 70:30, Pure Arabica, Commercial Strong)
   - Quick navigation shortcuts

2. **Roastery & Drum Batches (`production`)**
   - Real-time Drum Roaster telemetry & stage monitoring (Exothermic First Crack)
   - Live drum temperature & roast time elapsed
   - Calculated roast loss percentage tracker with tolerance bands
   - Commercial Roasting Batch log table

3. **Warehouse Bay Grid (`warehouse`)**
   - 4 Bay central storage grid (Bay A: Raw Green, Bay B: Chicory & Pre-Mix, Bay C: Roasted Blends, Bay D: Retail Packs)
   - Real-time capacity utilization, lot codes, bag counts, moisture & QC statuses

4. **Counter POS & Billing (`counter`)**
   - **Sell Mode**: Fast catalog selection, B2B GST / Retail toggle, quick-add weights (0.5kg, 1kg, 5kg), dynamic line item calculations
   - **Edit for Blend**: Commercial blend ratio specifications, live Coffee:Chicory custom slider & ratio calculator
   - **Today's Bills**: Real-time sales journal with search, reprint on POS-80C, WhatsApp invoice sharing
   - **Customers Directory**: Regular customer profiles, quick-pick favorite blends, phone registry
   - **Day Close Reconciliation**: Physical cash denomination counter (₹500, ₹200, ₹100, ₹50, ₹20, ₹10, coins), expected drawer cash calculation, shift variance audit, 80mm Z-Report

5. **Thermal 80mm ESC/POS Receipt Printing**
   - Dual copy toggle (Customer & Office copies)
   - 80mm / 58mm paper width selection
   - Dynamic BharatQR / UPI QR code generation (`upi://pay?pa=vasavitrading@okhdfcbank`)
   - Direct raw ESC/POS spooler integration & cash drawer pulse triggers

6. **Wholesale Dispatches (`dispatches`)**
   - Outward estate, hotel, and branch distribution queue
   - Vehicle numbers, driver details, payment status, and dispatch status tags

7. **Synthesized Web Audio SFX Engine**
   - Zero-asset Web Audio API synthesizers for tactile clicks, tab filter sweeps, cash register chimes, and threshold alerts

---

## 🛠 Tech Stack

- **Framework**: React 19 + Vite 8
- **Language**: TypeScript
- **Styling**: Tailwind CSS 3.4 + Glassmorphism luxury coffee palette
- **Animations**: Framer Motion & Motion
- **Charts**: Recharts
- **Icons**: Lucide React
- **State & Query**: Zustand & TanStack React Query
- **Notifications**: React Hot Toast

---

## 🚀 Running the Project

```bash
# 1. Install dependencies (if not already installed)
npm install

# 2. Start Vite Dev Server
npm run dev

# 3. Build for Production
npm run build

# 4. Preview Production Build
npm run preview
```
