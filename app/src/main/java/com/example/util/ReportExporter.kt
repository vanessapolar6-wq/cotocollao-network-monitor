package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Agency
import com.example.data.model.Incident
import java.io.File
import java.io.FileOutputStream

object ReportExporter {

    fun exportCsvForExcel(
        context: Context,
        agencies: List<Agency>,
        incidents: List<Incident>,
        isDemo: Boolean
    ): Result<File> = runCatching {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "Cotocollao_NOC_Reporte_${System.currentTimeMillis()}.csv")

        val csv = buildString {
            append('\uFEFF') // UTF-8 BOM para Excel en español
            appendLine("COTOCOLLAO NETWORK MONITOR - REPORTE DE INFRAESTRUCTURA")
            appendLine("Fecha generacion,${AvailabilityCalculator.formatTimestamp(System.currentTimeMillis())}")
            appendLine()
            appendLine("AGENCIA,ZONA,PROVEEDOR_ISP,ESTADO,ENLACE,SWITCH_L3,FIREWALL,LATENCIA_MS,PERDIDA_PAQ_PCT,DISPONIBILIDAD_PCT,INDISPONIBILIDAD_SEG,SIN_TELEMETRIA_SEG")
            agencies.forEach { a ->
                appendLine(
                    listOf(
                        a.name,
                        a.cityZone,
                        a.ispProvider.ifBlank { "Sin configurar" },
                        a.overallLight.labelEs,
                        a.linkLight.labelEs,
                        a.switchLight.labelEs,
                        a.firewallLight.labelEs,
                        a.latencyMs.toString(),
                        a.packetLossPct.toString(),
                        a.availabilityPct.toString(),
                        a.downtimeSeconds.toString(),
                        a.noTelemetrySeconds.toString()
                    ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                )
            }
            appendLine()
            appendLine("CODIGO,AGENCIA,EQUIPO,TIPO_FALLA,CRITICIDAD,ESTADO,DURACION_SEG,RESPONSABLE,DIAGNOSTICO,SOLUCION")
            incidents.forEach { inc ->
                appendLine(
                    listOf(
                        inc.code,
                        inc.agencyName,
                        inc.deviceName,
                        inc.failureType,
                        inc.severityEnum.labelEs,
                        inc.statusEnum.labelEs,
                        inc.durationSeconds.toString(),
                        inc.assignedTo,
                        inc.diagnosis,
                        inc.solution
                    ).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
                )
            }
        }
        file.writeText(csv, Charsets.UTF_8)
        file
    }

    fun exportPdfReport(
        context: Context,
        agencies: List<Agency>,
        incidents: List<Incident>,
        mttrSeconds: Long,
        mtbfSeconds: Long,
        isDemo: Boolean
    ): Result<File> = runCatching {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "Cotocollao_NOC_Reporte_${System.currentTimeMillis()}.pdf")

        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val orangePaint = Paint().apply { color = Color.rgb(234, 88, 12) }
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.rgb(255, 237, 213)
            textSize = 10f
        }
        val headingPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 13f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 10f
        }

        // Header banner
        canvas.drawRect(0f, 0f, 595f, 84f, orangePaint)
        canvas.drawText("COTOCOLLAO NETWORK MONITOR", 28f, 36f, titlePaint)
        canvas.drawText(
            "Reporte Estadístico de Disponibilidad e Incidentes - ${AvailabilityCalculator.formatTimestamp(System.currentTimeMillis())}",
            28f,
            58f,
            subtitlePaint
        )

        var y = 114f
        canvas.drawText("Resumen Ejecutivo (MTTR: ${AvailabilityCalculator.formatDuration(mttrSeconds)} | MTBF: ${AvailabilityCalculator.formatDuration(mtbfSeconds)})", 28f, y, headingPaint)
        y += 22f
        canvas.drawText("Fórmula: ((Tiempo total - Indisponibilidad) / Tiempo total) × 100 (excluyendo períodos sin telemetría)", 28f, y, bodyPaint)
        y += 26f

        canvas.drawText("Disponibilidad por Agencia:", 28f, y, headingPaint)
        y += 18f
        agencies.forEach { a ->
            val availText = if (a.configured) "${a.availabilityPct}%" else "Sin telemetría"
            canvas.drawText(
                "• ${a.name.padEnd(14)} | Estado: ${a.overallLight.labelEs.padEnd(14)} | Latencia: ${a.latencyMs}ms | Disp: $availText",
                32f,
                y,
                bodyPaint
            )
            y += 16f
        }

        y += 16f
        canvas.drawText("Incidentes Recientes (${incidents.size}):", 28f, y, headingPaint)
        y += 18f
        incidents.take(14).forEach { inc ->
            canvas.drawText(
                "• ${inc.code} [${inc.agencyName}] ${inc.failureType} - ${inc.statusEnum.labelEs} (${AvailabilityCalculator.formatDuration(inc.durationSeconds)})",
                32f,
                y,
                bodyPaint
            )
            y += 15f
        }

        pdf.finishPage(page)
        FileOutputStream(file).use { out -> pdf.writeTo(out) }
        pdf.close()
        file
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte NOC"))
    }
}
