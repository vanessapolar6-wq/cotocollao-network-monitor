const http = require("node:http");
const crypto = require("node:crypto");
const { MonitoringEngine } = require("./services/monitoringEngine");

function constantTimeCompare(a, b) {
  if (!a || !b) return false;
  const bufA = Buffer.from(String(a));
  const bufB = Buffer.from(String(b));
  if (bufA.length !== bufB.length) return false;
  return crypto.timingSafeEqual(bufA, bufB);
}

function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    let raw = "";
    req.on("data", (chunk) => {
      raw += chunk;
      if (raw.length > 1024 * 1024) {
        reject(new Error("Payload too large"));
      }
    });
    req.on("end", () => {
      if (!raw) return resolve({});
      try {
        resolve(JSON.parse(raw));
      } catch {
        reject(new Error("Invalid JSON payload"));
      }
    });
    req.on("error", reject);
  });
}

function sendJson(res, statusCode, data) {
  const body = JSON.stringify(data);
  res.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "X-Content-Type-Options": "nosniff",
    "Cache-Control": "no-store",
  });
  res.end(body);
}

function createApp({
  engine = new MonitoringEngine(),
  collectorToken = process.env.COLLECTOR_API_TOKEN || "",
  verifyClientToken = null,
} = {}) {
  async function isAuthorizedCollector(req) {
    const authHeader = req.headers["authorization"] || "";
    const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
    if (!collectorToken) return false;
    return constantTimeCompare(token, collectorToken);
  }

  async function isAuthorizedClient(req) {
    const authHeader = req.headers["authorization"] || "";
    const token = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
    if (!token) return false;
    if (verifyClientToken) {
      return verifyClientToken(token);
    }
    if (collectorToken && constantTimeCompare(token, collectorToken)) {
      return true;
    }
    return false;
  }

  const server = http.createServer(async (req, res) => {
    try {
      const url = new URL(req.url || "/", "http://localhost");
      const { pathname } = url;

      if (req.method === "GET" && pathname === "/health") {
        return sendJson(res, 200, { status: "ok", service: "cotocollao-noc-backend" });
      }

      if (req.method === "POST" && pathname === "/api/monitoring/heartbeat") {
        if (!(await isAuthorizedCollector(req))) {
          return sendJson(res, 401, { error: "Unauthorized collector credentials" });
        }
        const body = await readJsonBody(req);
        const hb = engine.recordHeartbeat(body);
        return sendJson(res, 200, { ok: true, heartbeat: hb });
      }

      if (req.method === "POST" && pathname === "/api/monitoring/metrics") {
        if (!(await isAuthorizedCollector(req))) {
          return sendJson(res, 401, { error: "Unauthorized collector credentials" });
        }
        const body = await readJsonBody(req);
        const result = await engine.ingestMetrics(body);
        return sendJson(res, result.ok ? 200 : 400, result);
      }

      if (req.method === "POST" && pathname === "/api/monitoring/events") {
        if (!(await isAuthorizedCollector(req))) {
          return sendJson(res, 401, { error: "Unauthorized collector credentials" });
        }
        const body = await readJsonBody(req);
        const result = await engine.ingestEvent(body);
        return sendJson(res, 202, result);
      }

      if (pathname.startsWith("/api/")) {
        if (!(await isAuthorizedClient(req))) {
          return sendJson(res, 401, { error: "Authentication required (Bearer Firebase ID Token)" });
        }
      }

      if (req.method === "GET" && pathname === "/api/dashboard") {
        return sendJson(res, 200, engine.getDashboardSummary());
      }
      if (req.method === "GET" && pathname === "/api/agencies") {
        return sendJson(res, 200, { agencies: Array.from(engine.agencies.values()) });
      }
      if (req.method === "GET" && pathname === "/api/devices") {
        return sendJson(res, 200, { devices: Array.from(engine.devices.values()) });
      }
      if (req.method === "GET" && pathname === "/api/incidents") {
        return sendJson(res, 200, { incidents: engine.incidents });
      }
      if (req.method === "GET" && pathname === "/api/reports/availability") {
        return sendJson(res, 200, engine.getAvailabilityReport());
      }

      return sendJson(res, 404, { error: "Endpoint no encontrado" });
    } catch (err) {
      return sendJson(res, 400, { error: err.message || "Bad Request" });
    }
  });

  return { server, engine };
}

module.exports = { createApp };
