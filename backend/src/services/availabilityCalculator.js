/**
 * Disponibilidad (%) = ((Tiempo total del período - Tiempo de indisponibilidad) / Tiempo total del período) × 100
 * Los períodos sin telemetría (noTelemetrySeconds) NO se contabilizan como tiempo operativo.
 */
function calculateAvailability({ totalPeriodSeconds, downtimeSeconds, noTelemetrySeconds = 0 }) {
  const effectiveObservedSeconds = Math.max(0, totalPeriodSeconds - Math.max(0, noTelemetrySeconds));
  if (effectiveObservedSeconds <= 0) {
    return {
      availabilityPct: 0,
      observedSeconds: 0,
      downtimeSeconds: Math.max(0, downtimeSeconds),
      noTelemetrySeconds: Math.max(0, noTelemetrySeconds),
      hasTelemetry: false,
    };
  }
  const boundedDowntime = Math.min(effectiveObservedSeconds, Math.max(0, downtimeSeconds));
  const rawPct = ((effectiveObservedSeconds - boundedDowntime) / effectiveObservedSeconds) * 100;
  return {
    availabilityPct: Number(rawPct.toFixed(2)),
    observedSeconds: effectiveObservedSeconds,
    downtimeSeconds: boundedDowntime,
    noTelemetrySeconds: Math.max(0, noTelemetrySeconds),
    hasTelemetry: true,
  };
}

function calculateMttrSeconds(resolvedIncidents) {
  const valid = resolvedIncidents.filter((inc) => Number(inc.durationSeconds) > 0);
  if (valid.length === 0) return 0;
  const total = valid.reduce((acc, inc) => acc + Number(inc.durationSeconds), 0);
  return Math.round(total / valid.length);
}

function calculateMtbfSeconds(totalObservedSeconds, failureCount, totalDowntimeSeconds) {
  if (failureCount <= 0) return totalObservedSeconds;
  const uptimeSeconds = Math.max(0, totalObservedSeconds - totalDowntimeSeconds);
  return Math.round(uptimeSeconds / failureCount);
}

module.exports = {
  calculateAvailability,
  calculateMttrSeconds,
  calculateMtbfSeconds,
};
