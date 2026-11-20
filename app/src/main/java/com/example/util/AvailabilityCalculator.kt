package com.example.util

import com.example.data.model.Agency
import com.example.data.model.Incident
import com.example.data.model.IncidentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object AvailabilityCalculator {

    data class AvailabilityResult(
        val availabilityPct: Double,
        val observedSeconds: Long,
        val downtimeSeconds: Long,
        val noTelemetrySeconds: Long,
        val hasTelemetry: Boolean
    )

    /**
     * Fórmula oficial:
     * Disponibilidad (%) = ((Tiempo total del período - Tiempo de indisponibilidad) / Tiempo total del período) × 100
     * Los períodos sin telemetría (noTelemetrySeconds) NO se contabilizan como períodos operativos.
     */
    fun calculate(
        totalPeriodSeconds: Long,
        downtimeSeconds: Long,
        noTelemetrySeconds: Long = 0L
    ): AvailabilityResult {
        val cleanNoTelemetry = noTelemetrySeconds.coerceAtLeast(0L)
        val observed = (totalPeriodSeconds - cleanNoTelemetry).coerceAtLeast(0L)
        if (observed <= 0L) {
            return AvailabilityResult(
                availabilityPct = 0.0,
                observedSeconds = 0L,
                downtimeSeconds = downtimeSeconds.coerceAtLeast(0L),
                noTelemetrySeconds = cleanNoTelemetry,
                hasTelemetry = false
            )
        }
        val boundedDowntime = downtimeSeconds.coerceIn(0L, observed)
        val raw = ((observed - boundedDowntime).toDouble() / observed.toDouble()) * 100.0
        val rounded = (raw * 100.0).roundToInt() / 100.0
        return AvailabilityResult(
            availabilityPct = rounded,
            observedSeconds = observed,
            downtimeSeconds = boundedDowntime,
            noTelemetrySeconds = cleanNoTelemetry,
            hasTelemetry = true
        )
    }

    fun calculateMttrSeconds(incidents: List<Incident>): Long {
        val resolved = incidents.filter {
            (it.statusEnum == IncidentStatus.RESOLVED || it.statusEnum == IncidentStatus.CLOSED) &&
                it.durationSeconds > 0L
        }
        if (resolved.isEmpty()) return 0L
        return resolved.sumOf { it.durationSeconds } / resolved.size
    }

    fun calculateMtbfSeconds(agencies: List<Agency>, totalIncidents: Int): Long {
        if (totalIncidents <= 0) return 0L
        val totalDowntime = agencies.sumOf { it.downtimeSeconds }
        val configuredCount = agencies.count { it.configured }.coerceAtLeast(1)
        val nominalObserved = configuredCount * 30L * 86400L
        val uptime = (nominalObserved - totalDowntime).coerceAtLeast(0L)
        return uptime / totalIncidents
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0L) return "0s"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (days == 0L && (secs > 0 || isEmpty())) append("${secs}s")
        }.trim()
    }

    fun formatTimestamp(epochMs: Long): String {
        if (epochMs <= 0L) return "Sin registro"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(epochMs))
    }

    fun maskIpIfUnauthorized(ip: String, canViewIp: Boolean): String {
        if (ip.isBlank()) return "Sin configurar"
        return if (canViewIp) ip else "•••.•••.•••.••• (Restringida)"
    }
}
