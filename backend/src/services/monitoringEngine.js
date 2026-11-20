const { calculateAvailability, calculateMttrSeconds, calculateMtbfSeconds } = require("./availabilityCalculator");
const { NotificationService } = require("./notificationService");

const INITIAL_AGENCIES = [
  { id: "matriz", code: "AG-01", name: "MATRIZ", cityZone: "Quito Norte - Cotocollao" },
  { id: "calderon", code: "AG-02", name: "CALDERÓN", cityZone: "Quito Norte - Calderón" },
  { id: "sangolqui", code: "AG-03", name: "SANGOLQUÍ", cityZone: "Valle de los Chillos" },
  { id: "sur", code: "AG-04", name: "SUR", cityZone: "Quito Sur - El Recreo" },
  { id: "pomasqui", code: "AG-05", name: "POMASQUI", cityZone: "Quito Norte - Pomasqui" },
  { id: "san_antonio", code: "AG-06", name: "SAN ANTONIO", cityZone: "Mitad del Mundo" },
  { id: "centro", code: "AG-07", name: "CENTRO", cityZone: "Quito Centro Histórico" },
];

class MonitoringEngine {
  constructor({ notificationService = new NotificationService() } = {}) {
    this.notificationService = notificationService;
    this.thresholds = {
      pingIntervalSec: 30,
      pingTimeoutSec: 3,
      failuresForDown: 3,
      successesForRecovery: 2,
      cpuThresholdPct: 85,
      ramThresholdPct: 85,
      tempThresholdC: 70,
      packetLossThresholdPct: 5,
      latencyThresholdMs: 120,
      dedupWindowMinutes: 10,
    };

    this.agencies = new Map();
    for (const base of INITIAL_AGENCIES) {
      this.agencies.set(base.id, {
        ...base,
        publicIp: "",
        gatewayIp: "",
        switchIp: "",
        firewallIp: "",
        ispProvider: "",
        contractedBandwidthMbps: 0,
        status: "GRAY",
        linkStatus: "GRAY",
        switchStatus: "GRAY",
        firewallStatus: "GRAY",
        latencyMs: 0,
        packetLossPct: 0,
        availabilityPct: 0,
        observedSeconds: 0,
        downtimeSeconds: 0,
        noTelemetrySeconds: 0,
        consecutiveFailures: 0,
        consecutiveSuccesses: 0,
        lastCheckEpochMs: 0,
        lastConnectionEpochMs: 0,
        configured: false,
      });
    }

    this.devices = new Map();
    this.incidents = [];
    this.collectorHeartbeats = new Map();
    this.fcmTokens = new Set();
  }

  recordHeartbeat(payload) {
    const collectorId = String(payload.collectorId || "collector-default");
    const entry = {
      collectorId,
      version: String(payload.version || "1.0.0"),
      host: String(payload.host || "unknown"),
      agenciesCount: Number(payload.agenciesCount || 0),
      lastHeartbeatEpochMs: Date.now(),
      status: "ONLINE",
    };
    this.collectorHeartbeats.set(collectorId, entry);
    return entry;
  }

  async ingestMetrics(payload) {
    const { agencyId, linkMetric, deviceMetrics = [] } = payload;
    const agency = this.agencies.get(agencyId);
    if (!agency) {
      return { ok: false, error: `Agencia desconocida: ${agencyId}` };
    }

    const now = Date.now();
    const alertsTriggered = [];

    if (linkMetric) {
      const intervalSec = Number(linkMetric.intervalSec || this.thresholds.pingIntervalSec);
      agency.observedSeconds += intervalSec;
      agency.lastCheckEpochMs = now;
      agency.configured = true;

      if (linkMetric.reachable) {
        agency.consecutiveFailures = 0;
        agency.consecutiveSuccesses += 1;
        agency.latencyMs = Number(linkMetric.latencyMs || 0);
        agency.packetLossPct = Number(linkMetric.packetLossPct || 0);
        agency.lastConnectionEpochMs = now;

        const wasDown = agency.linkStatus === "RED";
        if (wasDown && agency.consecutiveSuccesses >= this.thresholds.successesForRecovery) {
          agency.linkStatus = "GREEN";
          const alert = await this.notificationService.processEvent(
            {
              agencyId: agency.id,
              agencyName: agency.name,
              deviceName: "Enlace WAN / Router Principal",
              incidentType: "RECUPERACIÓN DE ENLACE DE INTERNET",
              severity: "RECOVERY",
              currentStatus: "OPERATIVO",
              recommendation: "Monitoreo continuo normalizado.",
              durationSeconds: agency.downtimeSeconds,
              eventEpochMs: now,
            },
            Array.from(this.fcmTokens)
          );
          alertsTriggered.push(alert);
        } else if (!wasDown) {
          const degraded =
            agency.latencyMs > this.thresholds.latencyThresholdMs ||
            agency.packetLossPct > this.thresholds.packetLossThresholdPct;
          agency.linkStatus = degraded ? "YELLOW" : "GREEN";
        }
      } else {
        agency.consecutiveSuccesses = 0;
        agency.consecutiveFailures += 1;
        agency.downtimeSeconds += intervalSec;
        agency.packetLossPct = 100;

        if (agency.consecutiveFailures >= this.thresholds.failuresForDown) {
          agency.linkStatus = "RED";
          const alert = await this.notificationService.processEvent(
            {
              agencyId: agency.id,
              agencyName: agency.name,
              deviceName: "Router principal",
              authorizedIp: agency.publicIp,
              incidentType: "ENLACE DE INTERNET CAÍDO",
              severity: "CRITICAL",
              currentStatus: "SIN CONECTIVIDAD",
              recommendation: "Verificar proveedor de internet, gateway y conectividad WAN.",
              eventEpochMs: now,
            },
            Array.from(this.fcmTokens)
          );
          alertsTriggered.push(alert);
        }
      }
    }

    for (const dm of deviceMetrics) {
      const existing = this.devices.get(dm.deviceId) || {
        id: dm.deviceId,
        agencyId,
        agencyName: agency.name,
        deviceType: dm.deviceType || "SWITCH_L3",
        hostname: dm.hostname || dm.deviceId,
        ipAddress: dm.ipAddress || "",
        brand: dm.brand || "",
        model: dm.model || "",
        serialNumber: dm.serialNumber || "",
        firmwareVersion: dm.firmwareVersion || "",
        protocol: dm.protocol || "SNMPV3",
      };

      const status = this.evaluateDeviceStatus(dm);
      const updated = {
        ...existing,
        ...dm,
        status,
        lastCheckEpochMs: now,
      };
      this.devices.set(dm.deviceId, updated);

      if (dm.deviceType === "SWITCH_L3") agency.switchStatus = status;
      if (dm.deviceType === "FIREWALL") agency.firewallStatus = status;
    }

    const avail = calculateAvailability({
      totalPeriodSeconds: agency.observedSeconds + agency.noTelemetrySeconds,
      downtimeSeconds: agency.downtimeSeconds,
      noTelemetrySeconds: agency.noTelemetrySeconds,
    });
    agency.availabilityPct = avail.availabilityPct;
    agency.status = this.combineTrafficLights([agency.linkStatus, agency.switchStatus, agency.firewallStatus]);

    return { ok: true, agency, alertsTriggered };
  }

  evaluateDeviceStatus(dm) {
    if (dm.reachable === false) return "RED";
    if (
      Number(dm.cpuPct || 0) > this.thresholds.cpuThresholdPct ||
      Number(dm.ramPct || 0) > this.thresholds.ramThresholdPct ||
      Number(dm.tempCelsius || 0) > this.thresholds.tempThresholdC
    ) {
      return "YELLOW";
    }
    return "GREEN";
  }

  combineTrafficLights(statuses) {
    if (statuses.includes("RED")) return "RED";
    if (statuses.includes("YELLOW")) return "YELLOW";
    if (statuses.includes("GREEN")) return "GREEN";
    return "GRAY";
  }

  async ingestEvent(event) {
    const res = await this.notificationService.processEvent(event, Array.from(this.fcmTokens));
    if (event.createIncident) {
      this.incidents.unshift({
        id: `inc_${Date.now()}`,
        code: `INC-${new Date().getFullYear()}-${String(this.incidents.length + 1).padStart(3, "0")}`,
        agencyId: event.agencyId,
        agencyName: event.agencyName,
        deviceName: event.deviceName,
        deviceType: event.deviceType || "INTERNET_LINK",
        failureType: event.incidentType,
        severity: event.severity || "CRITICAL",
        status: "NEW",
        startedAtEpochMs: event.eventEpochMs || Date.now(),
        recoveredAtEpochMs: 0,
        durationSeconds: 0,
        assignedTo: "Guardia NOC",
      });
    }
    return res;
  }

  getDashboardSummary() {
    const agencies = Array.from(this.agencies.values());
    const devices = Array.from(this.devices.values());
    const switches = devices.filter((d) => d.deviceType === "SWITCH_L3");
    const firewalls = devices.filter((d) => d.deviceType === "FIREWALL");
    const configuredAgencies = agencies.filter((a) => a.configured);
    const overallAvailabilityPct =
      configuredAgencies.length > 0
        ? Number(
            (
              configuredAgencies.reduce((sum, a) => sum + Number(a.availabilityPct || 0), 0) /
              configuredAgencies.length
            ).toFixed(2)
          )
        : 0;

    return {
      totalAgencies: agencies.length,
      operationalAgencies: agencies.filter((a) => a.status === "GREEN").length,
      agenciesWithIncidents: agencies.filter((a) => a.status === "RED" || a.status === "YELLOW").length,
      activeInternetLinks: agencies.filter((a) => a.linkStatus === "GREEN" || a.linkStatus === "YELLOW").length,
      downInternetLinks: agencies.filter((a) => a.linkStatus === "RED").length,
      operationalSwitches: switches.filter((s) => s.status === "GREEN").length,
      failedSwitches: switches.filter((s) => s.status === "RED" || s.status === "YELLOW").length,
      operationalFirewalls: firewalls.filter((f) => f.status === "GREEN").length,
      failedFirewalls: firewalls.filter((f) => f.status === "RED" || f.status === "YELLOW").length,
      pendingCriticalIncidents: this.incidents.filter(
        (i) => i.severity === "CRITICAL" && i.status !== "RESOLVED" && i.status !== "CLOSED"
      ).length,
      overallAvailabilityPct,
      telemetryConnected: this.collectorHeartbeats.size > 0,
      collectors: Array.from(this.collectorHeartbeats.values()),
    };
  }

  getAvailabilityReport() {
    const agencies = Array.from(this.agencies.values()).map((a) => ({
      agencyId: a.id,
      agencyName: a.name,
      ispProvider: a.ispProvider || "Sin configurar",
      availabilityPct: a.availabilityPct,
      observedSeconds: a.observedSeconds,
      downtimeSeconds: a.downtimeSeconds,
      noTelemetrySeconds: a.noTelemetrySeconds,
      status: a.status,
    }));

    const resolved = this.incidents.filter((i) => i.status === "RESOLVED" || i.status === "CLOSED");
    const totalObserved = agencies.reduce((s, a) => s + a.observedSeconds, 0);
    const totalDowntime = agencies.reduce((s, a) => s + a.downtimeSeconds, 0);

    return {
      generatedAtEpochMs: Date.now(),
      formula: "((Tiempo total del período - Tiempo de indisponibilidad) / Tiempo total del período) × 100",
      mttrSeconds: calculateMttrSeconds(resolved),
      mtbfSeconds: calculateMtbfSeconds(totalObserved, this.incidents.length, totalDowntime),
      agencies,
    };
  }
}

module.exports = { MonitoringEngine, INITIAL_AGENCIES };
