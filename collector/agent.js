const { execFile } = require("node:child_process");
const dgram = require("node:dgram");
const fs = require("node:fs");
const path = require("node:path");

const CONFIG_PATH = process.env.COLLECTOR_CONFIG || path.join(__dirname, "config.json");
const COLLECTOR_API_TOKEN = process.env.COLLECTOR_API_TOKEN || "";

function pingHost(ip, timeoutSec = 3) {
  return new Promise((resolve) => {
    if (!ip || ip.startsWith("CONFIGURE_")) {
      return resolve({ reachable: false, latencyMs: 0, packetLossPct: 100, unconfigured: true });
    }
    const start = Date.now();
    execFile("ping", ["-c", "3", "-W", String(timeoutSec), ip], (error, stdout) => {
      const elapsed = Date.now() - start;
      if (error) {
        return resolve({ reachable: false, latencyMs: 0, packetLossPct: 100 });
      }
      const lossMatch = stdout.match(/(\d+(?:\.\d+)?)% packet loss/);
      const rttMatch = stdout.match(/min\/avg\/max\/[^=]+=\s*[\d.]+\/([\d.]+)\//);
      resolve({
        reachable: true,
        latencyMs: rttMatch ? Math.round(Number(rttMatch[1])) : elapsed,
        packetLossPct: lossMatch ? Number(lossMatch[1]) : 0,
      });
    });
  });
}

async function pollSnmpV3Device(device) {
  let snmp;
  try {
    snmp = require("net-snmp");
  } catch {
    return {
      deviceId: device.deviceId,
      deviceType: device.deviceType,
      hostname: device.hostname,
      reachable: false,
      note: "Instalar dependencia net-snmp para consultas SNMPv3 reales",
    };
  }

  const user = {
    name: process.env[device.snmpV3User?.replace("ENV:", "")] || "noc_snmpv3",
    level: snmp.SecurityLevel.authPriv,
    authProtocol: snmp.AuthProtocols.sha256,
    authKey: process.env[device.snmpAuthKeyEnv] || "",
    privProtocol: snmp.PrivProtocols.aes,
    privKey: process.env[device.snmpPrivKeyEnv] || "",
  };

  if (!user.authKey || !user.privKey || device.ipAddress.startsWith("CONFIGURE_")) {
    return { deviceId: device.deviceId, deviceType: device.deviceType, hostname: device.hostname, reachable: false };
  }

  const session = snmp.createV3Session(device.ipAddress, user);
  const oids = ["1.3.6.1.2.1.1.3.0"]; // sysUpTime
  return new Promise((resolve) => {
    session.get(oids, (err, varbinds) => {
      session.close();
      if (err) {
        return resolve({ deviceId: device.deviceId, deviceType: device.deviceType, hostname: device.hostname, reachable: false });
      }
      resolve({
        deviceId: device.deviceId,
        deviceType: device.deviceType,
        hostname: device.hostname,
        reachable: true,
        uptimeSeconds: Math.round(Number(varbinds?.[0]?.value || 0) / 100),
      });
    });
  });
}

async function postToBackend(backendUrl, endpoint, payload) {
  if (!backendUrl || !COLLECTOR_API_TOKEN) return;
  await fetch(`${backendUrl}${endpoint}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${COLLECTOR_API_TOKEN}`,
    },
    body: JSON.stringify(payload),
  });
}

function startSyslogListener(port, backendUrl, collectorId) {
  const server = dgram.createSocket("udp4");
  server.on("message", async (msg, rinfo) => {
    const raw = msg.toString("utf8").slice(0, 400);
    await postToBackend(backendUrl, "/api/monitoring/events", {
      collectorId,
      agencyId: "matriz",
      agencyName: "MATRIZ",
      deviceName: `Syslog (${rinfo.address})`,
      incidentType: "EVENTO_SYSLOG_FIREWALL",
      severity: raw.toLowerCase().includes("down") || raw.toLowerCase().includes("critical") ? "CRITICAL" : "WARNING",
      currentStatus: "EVENTO REGISTRADO",
      recommendation: "Revisar bitácora de firewall sin almacenar tráfico sensible.",
      description: raw,
      eventEpochMs: Date.now(),
    });
  });
  server.bind(port);
  return server;
}

async function runCollector() {
  if (!fs.existsSync(CONFIG_PATH)) {
    console.error("Falta config.json. Copie config.example.json y configure sus IPs y variables de entorno.");
    process.exit(1);
  }
  const config = JSON.parse(fs.readFileSync(CONFIG_PATH, "utf8"));
  startSyslogListener(config.syslogPort || 5514, config.backendUrl, config.collectorId);

  const cycle = async () => {
    await postToBackend(config.backendUrl, "/api/monitoring/heartbeat", {
      collectorId: config.collectorId,
      agenciesCount: config.agencies.length,
      version: "1.0.0",
    });

    for (const agency of config.agencies) {
      const linkMetric = await pingHost(agency.publicIp, config.pingTimeoutSec || 3);
      if (linkMetric.unconfigured) continue;

      const deviceMetrics = [];
      for (const dev of agency.devices || []) {
        if (dev.protocol === "SNMPV3") {
          deviceMetrics.push(await pollSnmpV3Device(dev));
        }
      }

      await postToBackend(config.backendUrl, "/api/monitoring/metrics", {
        agencyId: agency.agencyId,
        linkMetric: { ...linkMetric, intervalSec: config.intervalSec || 30 },
        deviceMetrics,
      });
    }
  };

  await cycle();
  setInterval(cycle, (config.intervalSec || 30) * 1000);
}

if (require.main === module) {
  runCollector();
}

module.exports = { pingHost, pollSnmpV3Device };
