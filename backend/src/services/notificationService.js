const DEFAULT_DEDUP_WINDOW_MS = 10 * 60 * 1000;

class NotificationService {
  constructor({ fcmSender = null, dedupWindowMs = DEFAULT_DEDUP_WINDOW_MS } = {}) {
    this.fcmSender = fcmSender;
    this.dedupWindowMs = dedupWindowMs;
    this.activeAlertMap = new Map();
    this.sentHistory = [];
  }

  buildDeduplicationKey(event) {
    return `${event.agencyId || "unknown"}::${event.deviceName || "unknown"}::${event.incidentType || "EVENT"}`;
  }

  formatNotificationPayload(event) {
    const isRecovery = event.severity === "RECOVERY";
    const title = isRecovery
      ? `RECUPERACIÓN DE SERVICIO – COTOCOLLAO NETWORK MONITOR`
      : `ALERTA ${event.severity || "CRÍTICA"} – COTOCOLLAO NETWORK MONITOR`;

    const lines = [
      `Agencia: ${event.agencyName}`,
      `Evento: ${event.incidentType}`,
      `Equipo afectado: ${event.deviceName}`,
      `Hora del incidente: ${new Date(event.eventEpochMs || Date.now()).toISOString().substr(11, 8)}`,
      `Estado: ${event.currentStatus}`,
      event.durationSeconds > 0 ? `Duración interrupción: ${event.durationSeconds}s` : null,
      `Acción requerida: ${event.recommendation}`,
    ].filter(Boolean);

    return {
      title,
      body: lines.join("\n"),
      data: {
        agencyId: String(event.agencyId || ""),
        agencyName: String(event.agencyName || ""),
        deviceName: String(event.deviceName || ""),
        incidentType: String(event.incidentType || ""),
        severity: String(event.severity || "WARNING"),
        currentStatus: String(event.currentStatus || ""),
        eventEpochMs: String(event.eventEpochMs || Date.now()),
        // IP solo incluida para canales/clientes autorizados; nunca expuesta en payloads públicos sin RBAC
        authorizedIp: event.authorizedIp ? String(event.authorizedIp) : "",
      },
    };
  }

  async processEvent(event, deviceTokens = []) {
    const now = Date.now();
    const key = this.buildDeduplicationKey(event);
    const existing = this.activeAlertMap.get(key);

    if (event.severity === "RECOVERY") {
      this.activeAlertMap.delete(key);
      const payload = this.formatNotificationPayload(event);
      const dispatch = await this.dispatchToFcm(payload, deviceTokens);
      this.sentHistory.unshift({ ...event, payload, deduplicated: false, sentAt: now });
      return { sent: true, deduplicated: false, escalated: false, payload, dispatch };
    }

    if (existing && now - existing.lastSentAt < this.dedupWindowMs) {
      existing.occurrences += 1;
      existing.lastSeenAt = now;
      const shouldEscalate = existing.occurrences >= 3 && !existing.escalated;
      if (shouldEscalate) {
        existing.escalated = true;
        const escalatedEvent = {
          ...event,
          severity: "CRITICAL",
          currentStatus: `ESCALADO (${existing.occurrences} repeticiones)`,
        };
        const payload = this.formatNotificationPayload(escalatedEvent);
        const dispatch = await this.dispatchToFcm(payload, deviceTokens);
        this.sentHistory.unshift({ ...escalatedEvent, payload, deduplicated: false, escalated: true, sentAt: now });
        return { sent: true, deduplicated: false, escalated: true, occurrences: existing.occurrences, payload, dispatch };
      }
      return { sent: false, deduplicated: true, escalated: false, occurrences: existing.occurrences };
    }

    this.activeAlertMap.set(key, {
      firstSeenAt: now,
      lastSentAt: now,
      lastSeenAt: now,
      occurrences: 1,
      escalated: false,
    });

    const payload = this.formatNotificationPayload(event);
    const dispatch = await this.dispatchToFcm(payload, deviceTokens);
    this.sentHistory.unshift({ ...event, payload, deduplicated: false, escalated: false, sentAt: now });
    return { sent: true, deduplicated: false, escalated: false, occurrences: 1, payload, dispatch };
  }

  async dispatchToFcm(payload, tokens) {
    if (!this.fcmSender || !Array.isArray(tokens) || tokens.length === 0) {
      return { deliveredCount: 0, mode: "dry-run-no-tokens" };
    }
    return this.fcmSender.sendEachForMulticast({
      tokens,
      notification: {
        title: payload.title,
        body: payload.body,
      },
      data: payload.data,
      android: {
        priority: "high",
      },
    });
  }
}

module.exports = {
  NotificationService,
  DEFAULT_DEDUP_WINDOW_MS,
};
