package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.AgencyAvailabilityChartCard
import com.example.ui.components.FaultDistributionDonutCard
import com.example.ui.state.NocWorkspaceState
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.util.AvailabilityCalculator
import com.example.util.ReportExporter

@Composable
fun ReportsScreen(
    state: NocWorkspaceState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Mensual") }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    val mttrSec = remember(state.incidents) { AvailabilityCalculator.calculateMttrSeconds(state.incidents) }
    val mtbfSec = remember(state.agencies, state.incidents) {
        AvailabilityCalculator.calculateMtbfSeconds(state.agencies, state.incidents.size)
    }

    val incidentsByIsp = remember(state.agencies, state.incidents) {
        val map = mutableMapOf<String, Int>()
        state.incidents.forEach { inc ->
            val isp = state.agencies.firstOrNull { it.id == inc.agencyId }?.ispProvider?.ifBlank { "Sin ISP" } ?: "Sin ISP"
            map[isp] = (map[isp] ?: 0) + 1
        }
        map.entries.sortedByDescending { it.value }
    }

    val recurrenceByAgency = remember(state.incidents) {
        state.incidents.groupingBy { it.agencyName }.eachCount().entries.sortedByDescending { it.value }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("reports_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reportes Estadísticos NOC", style = MaterialTheme.typography.titleLarge)
                    Text("SLA, MTTR, MTBF, proveedores y exportación", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Diario", "Semanal", "Mensual").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period, maxLines = 1) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        ReportExporter.exportPdfReport(
                            context, state.agencies, state.incidents, mttrSec, mtbfSec, state.isDemoMode
                        ).onSuccess { file ->
                            exportStatus = "PDF generado: ${file.name}"
                            ReportExporter.shareFile(context, file, "application/pdf")
                        }.onFailure {
                            exportStatus = "Error exportando PDF: ${it.localizedMessage}"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_pdf_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CotocollaoOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Exportar PDF")
                }

                OutlinedButton(
                    onClick = {
                        ReportExporter.exportCsvForExcel(
                            context, state.agencies, state.incidents, state.isDemoMode
                        ).onSuccess { file ->
                            exportStatus = "Excel (CSV UTF-8) generado: ${file.name}"
                            ReportExporter.shareFile(context, file, "text/csv")
                        }.onFailure {
                            exportStatus = "Error exportando Excel: ${it.localizedMessage}"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_excel_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.TableChart, contentDescription = null, tint = CotocollaoOrange, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Exportar Excel")
                }
            }
            if (exportStatus != null) {
                Text(
                    text = exportStatus ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = CotocollaoOrange,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Métricas de Confiabilidad ($selectedPeriod)", style = MaterialTheme.typography.titleMedium)
                    InfoRow("Disponibilidad Consolidada", "${state.dashboard.overallAvailabilityPct}%")
                    InfoRow("MTTR (Tiempo medio recuperación)", AvailabilityCalculator.formatDuration(mttrSec))
                    InfoRow("MTBF (Tiempo medio entre fallas)", AvailabilityCalculator.formatDuration(mtbfSec))
                    InfoRow("Total incidentes registrados", "${state.incidents.size}")
                    Text(
                        text = "Fórmula: ((Tiempo total - Indisponibilidad) / Tiempo total) × 100. Los períodos sin telemetría no cuentan como tiempo operativo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { AgencyAvailabilityChartCard(state.agencies, state.isDemoMode) }
        item { FaultDistributionDonutCard(state.chartSeries.faultDistribution, state.isDemoMode) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Agencias con Mayor Recurrencia de Fallas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (recurrenceByAgency.isEmpty()) {
                        Text("Sin incidentes registrados.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        recurrenceByAgency.forEach { (ag, count) ->
                            InfoRow(ag, "$count incidente(s)")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Incidentes por Proveedor de Internet (ISP)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (incidentsByIsp.isEmpty()) {
                        Text("Sin incidentes por ISP.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        incidentsByIsp.forEach { (isp, count) ->
                            InfoRow(isp, "$count incidente(s)")
                        }
                    }
                }
            }
        }
    }
}
