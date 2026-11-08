// Talking to the server (Spring Boot REST API) with JWT token authentication.

import axios from "axios";

export const apiClient = axios.create({
  baseURL: "/api/v1",
  headers: { "X-VTC": "1" },
  withCredentials: true,
});

let onSignedOut: (code: string) => void = () => {};
export function setSignedOutHandler(fn: (code: string) => void) { onSignedOut = fn; }

let currentToken: string | null = null;

export function getAuthToken(): string | null {
  return currentToken || sessionStorage.getItem("vtc_jwt_token");
}

export function setAuthToken(token: string) {
  currentToken = token;
  sessionStorage.setItem("vtc_jwt_token", token);
}

export function clearAuthToken() {
  currentToken = null;
  sessionStorage.removeItem("vtc_jwt_token");
  localStorage.removeItem("vtc_jwt_token");
}

export class ApiError extends Error {
  code: string;
  status: number;
  constructor(code: string, status: number) {
    super(code);
    this.code = code;
    this.status = status;
  }
}

export function newKey(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

export const DEFAULT_SETTINGS = {
  company_name: "Vasavi Trading Company",
  short_name: "VASAVI",
  tagline: "TRADING COMPANY",
  location_label: "PROD-01",
  accent: "roast",
  currency_symbol: "₹",
  languages_enabled: ["en", "te", "hi"],
  default_language: "en",
  idle_shared_min: 15,
  idle_personal_min: 480,
  quiet_start_hour: 21,
  invoice_prefix: "VTC-26",
};

export const ALL_MODULES = [
  "dashboard", "tasks", "inward", "inventory", "roasting", "blending", "qc",
  "outside", "packing", "counter", "orders", "dispatch", "comms",
  "maintenance", "team", "finance", "masters", "exceptions", "reports", "profile"
];

export const ALL_PERMISSIONS = [
  "dashboard.my_view", "dashboard.team_view", "tasks.view_own", "tasks.update_own", "tasks.view_all",
  "tasks.assign", "tasks.update_any", "tasks.approve", "goods.view", "goods.receive", "goods.reverse",
  "inventory.view", "inventory.transfer", "stock.cycle_count", "roast.view", "roast.execute",
  "outside.view", "outside.execute", "pack.view", "pack.execute", "counter.sell", "counter.cash",
  "counter.customers", "counter.insights", "outgoing.view", "outgoing.create", "maintenance.raise",
  "maintenance.manage", "team.view", "reports.operational", "recipe.view_public", "qc.view",
  "masters.view", "masters.create_draft", "masters.approve", "system.settings", "system.users",
  "system.roles", "comms.oversight", "team.profiles", "team.hierarchy", "inward.receive",
  "roast.record", "pack.record", "counter.cash_verify", "system.grants", "system.audit", "system.preview_as"
];

export async function api(path: string, method = "GET", body: any = null, idempotencyKey: string | null = null): Promise<any> {
  const headers: Record<string, string> = { "X-VTC": "1" };
  const token = getAuthToken();
  if (token) headers["Authorization"] = "Bearer " + token;
  const opts: RequestInit = { method, headers, credentials: "include" };
  if (body !== null) {
    headers["Content-Type"] = "application/json";
    opts.body = JSON.stringify(body);
  }
  if (idempotencyKey) headers["X-Idempotency-Key"] = idempotencyKey;

  try {
    const res = await fetch("/api/v1" + path, opts);
    if (res.ok) {
      const data = await res.json().catch(() => ({}));
      if (data && data.token) {
        setAuthToken(data.token);
      }
      if (path === "/logout") {
        clearAuthToken();
      }
      if (path === "/me" && data && data.user) {
        normalizeMeResponse(data);
      }
      return data;
    }

    // 401 or 403: authentication required / denied
    if (res.status === 401 || res.status === 403) {
      const data: any = await res.json().catch(() => ({}));
      clearAuthToken();
      if (!path.startsWith("/login")) onSignedOut(data.error || "LOGIN_REQUIRED");
      const defaultErr = path.startsWith("/login") ? "INVALID_CREDENTIALS" : "LOGIN_REQUIRED";
      throw new ApiError(data.error || defaultErr, res.status);
    }

    if (res.status === 404) {
      const data: any = await res.json().catch(() => ({}));
      throw new ApiError(data.error || "NOT_FOUND", 404);
    }

    if (res.status >= 500) {
      const data: any = await res.json().catch(() => ({}));
      throw new ApiError(data.error || "SERVER_ERROR", res.status);
    }

    const data: any = await res.json().catch(() => ({}));
    throw new ApiError(data.error || "REQUEST_FAILED", res.status);
  } catch (err: any) {
    if (err instanceof ApiError) {
      throw err;
    }
    throw new ApiError(err?.message || "NETWORK_ERROR", 0);
  }
}

function normalizeMeResponse(data: any) {
  if (!data || !data.user) return data;
  data.user.role_name = data.user.role_name || data.user.role || "Staff";
  data.user.role_id = data.user.role_id || data.user.id || 1;
  const isOwner = Boolean(data.user.is_owner || data.user.username === "owner");
  data.user.is_owner = isOwner;
  data.user.tier = data.user.tier || (isOwner ? "admin" : (data.user.role === "Admin" ? "admin" : (data.user.role === "Manager" ? "manager" : "staff")));
  data.user.login_type = data.user.login_type || (data.user.tier === "staff" ? "pin" : "password");
  data.modules = data.modules || ALL_MODULES;
  data.live_modules = data.live_modules || ALL_MODULES;
  data.perms = Array.isArray(data.perms) ? data.perms : ALL_PERMISSIONS;
  data.settings = {
    ...DEFAULT_SETTINGS,
    ...(data.settings || {})
  };
  data.prefs = {
    lang: "en",
    large_text: false,
    shortcuts: ["inward", "roasting", "inventory"],
    widgets: ["stock", "pending_invoices", "holds", "orders", "team", "roasts", "audit", "quick_actions"],
    welcome: `Good day, ${data.user.full_name?.split(" ")[0] || "User"}`,
    ...(data.prefs || {})
  };
  return data;
}
