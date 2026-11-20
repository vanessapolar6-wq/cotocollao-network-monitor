package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.AlertEvent
import com.example.data.model.AlertSeverity

object LocalNotificationHelper {
    private const val CHANNEL_ID = "cotocollao_noc_alerts"
    private const val CHANNEL_NAME = "Alertas Críticas NOC"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de caídas de enlace, switches, firewalls y recuperaciones"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showAlertNotification(context: Context, alert: AlertEvent, isDemo: Boolean = false) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context)

        val header = if (alert.severityEnum == AlertSeverity.RECOVERY) {
            "RECUPERACIÓN – COTOCOLLAO NETWORK MONITOR"
        } else {
            "ALERTA ${alert.severityEnum.labelEs} – COTOCOLLAO NETWORK MONITOR"
        }

        val body = buildString {
            appendLine("Agencia: ${alert.agencyName}")
            appendLine("Evento: ${alert.incidentType}")
            appendLine("Equipo afectado: ${alert.deviceName}")
            appendLine("Hora: ${AvailabilityCalculator.formatTimestamp(alert.eventEpochMs)}")
            appendLine("Estado: ${alert.currentStatus}")
            if (alert.durationSeconds > 0) {
                appendLine("Duración: ${AvailabilityCalculator.formatDuration(alert.durationSeconds)}")
            }
            append("Acción requerida: ${alert.recommendation}")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(header)
            .setContentText("${alert.agencyName}: ${alert.incidentType} (${alert.currentStatus})")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (alert.id.hashCode() and 0x7FFFFFFF).coerceAtLeast(1),
                notification
            )
        } catch (_: SecurityException) {
            // Permission revoked between check and notify
        }
    }
}
