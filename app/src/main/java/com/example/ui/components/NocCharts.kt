package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.model.Agency
import com.example.ui.theme.CotocollaoOrange
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

@Composable
fun AgencyAvailabilityChartCard(
    agencies: List<Agency>,
    isDemo: Boolean
) {
    ChartCardContainer(
        title = "Disponibilidad de Internet por Agencia (%)",
        subtitle = "Cálculo operativo por enlace de agencia"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            agencies.forEach { agency ->
                val pct = if (agency.configured) agency.availabilityPct else 0.0
                val barColor = agency.linkLight.foregroundColor()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agency.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(96.dp),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(OutlineSubtle.copy(alpha = 0.45f))
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val fraction = (pct / 100.0).toFloat().coerceIn(0f, 1f)
                            if (fraction > 0f) {
                                drawRoundRect(
                                    color = barColor,
                                    size = Size(size.width * fraction, size.height),
                                    cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (agency.configured) "${pct}%" else "S/D",
                        style = MaterialTheme.typography.labelSmall,
                        color = barColor,
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun IncidentEvolutionChartCard(
    points: List<Pair<String, Int>>,
    isDemo: Boolean
) {
    ChartCardContainer(
        title = "Evolución de Incidentes (Últimos 30 días)",
        subtitle = "Serie temporal de eventos registrados"
    ) {
        if (points.isEmpty()) {
            Text(
                text = "Sin historial de incidentes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val maxVal = (points.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val stepX = if (points.size > 1) size.width / (points.size - 1) else size.width / 2f
                val coords = points.mapIndexed { idx, (_, count) ->
                    val x = if (points.size > 1) idx * stepX else size.width / 2f
                    val y = size.height - ((count.toFloat() / maxVal) * (size.height * 0.78f)) - 12f
                    Offset(x, y)
                }

                if (coords.size >= 2) {
                    val areaPath = Path().apply {
                        moveTo(coords.first().x, size.height)
                        coords.forEach { lineTo(it.x, it.y) }
                        lineTo(coords.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            listOf(CotocollaoOrange.copy(alpha = 0.28f), Color.Transparent)
                        )
                    )

                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(
                        path = linePath,
                        color = CotocollaoOrange,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                coords.forEach { pt ->
                    drawCircle(color = PureWhite, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = CotocollaoOrange, radius = 3.5.dp.toPx(), center = pt)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { (label, count) ->
                    Text(
                        text = "$label ($count)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyAvailabilityBarChartCard(
    points: List<Pair<String, Double>>,
    isDemo: Boolean
) {
    ChartCardContainer(
        title = "Disponibilidad Mensual de Enlaces (%)",
        subtitle = "Promedio consolidado mensual"
    ) {
        if (points.isEmpty()) {
            Text(
                text = "Sin mediciones mensuales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val slotWidth = size.width / points.size
                val barWidth = (slotWidth * 0.52f).coerceAtMost(28.dp.toPx())
                points.forEachIndexed { idx, (_, pct) ->
                    val frac = ((pct - 80.0) / 20.0).toFloat().coerceIn(0.08f, 1f)
                    val barHeight = size.height * frac
                    val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                    val color = when {
                        pct >= 99.0 -> StatusGreen
                        pct >= 95.0 -> StatusYellow
                        pct > 0.0 -> StatusRed
                        else -> StatusGray
                    }
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                points.forEach { (label, pct) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                        Text("${pct}%", style = MaterialTheme.typography.labelSmall, color = CotocollaoOrange)
                    }
                }
            }
        }
    }
}

@Composable
fun LatencyComparisonChartCard(
    agencies: List<Agency>,
    isDemo: Boolean
) {
    ChartCardContainer(
        title = "Comparación de Latencia entre Agencias (ms)",
        subtitle = "Última medición ICMP registrada"
    ) {
        val maxLat = (agencies.maxOfOrNull { it.latencyMs } ?: 100L).coerceAtLeast(100L)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            agencies.forEach { agency ->
                val color = when {
                    !agency.configured || agency.linkStatus == "GRAY" -> StatusGray
                    agency.linkStatus == "RED" -> StatusRed
                    agency.latencyMs > 120 -> StatusYellow
                    else -> StatusGreen
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agency.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(96.dp),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(OutlineSubtle.copy(alpha = 0.45f))
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val frac = (agency.latencyMs.toFloat() / maxLat.toFloat()).coerceIn(0f, 1f)
                            if (frac > 0f) {
                                drawRoundRect(
                                    color = color,
                                    size = Size(size.width * frac, size.height),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (agency.linkStatus == "RED") "CAÍDO" else "${agency.latencyMs} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        modifier = Modifier.width(54.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FaultDistributionDonutCard(
    slices: List<Pair<String, Int>>,
    isDemo: Boolean
) {
    ChartCardContainer(
        title = "Distribución de Fallas por Tipo de Dispositivo",
        subtitle = "Clasificación de incidentes por capa"
    ) {
        val total = slices.sumOf { it.second }
        val colors = listOf(CotocollaoOrange, StatusRed, StatusYellow, StatusGreen)
        if (total == 0) {
            Text(
                text = "Sin fallas registradas en el período actual.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Canvas(modifier = Modifier.size(104.dp)) {
                    var startAngle = -90f
                    val strokeWidth = 18.dp.toPx()
                    slices.forEachIndexed { idx, (_, value) ->
                        val sweep = (value.toFloat() / total.toFloat()) * 360f
                        drawArc(
                            color = colors[idx % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweep
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEachIndexed { idx, (label, count) ->
                        val pct = ((count.toFloat() / total.toFloat()) * 100).toInt()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[idx % colors.size])
                            )
                            Text(
                                text = "$label: $count ($pct%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCardContainer(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.dp, OutlineSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
