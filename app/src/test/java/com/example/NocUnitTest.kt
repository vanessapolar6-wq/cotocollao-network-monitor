package com.example

import com.example.data.demo.DemoDataEngine
import com.example.data.model.Incident
import com.example.data.model.IncidentStatus
import com.example.data.model.UserRole
import com.example.ui.state.DemoSandboxController
import com.example.util.AvailabilityCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NocUnitTest {

    @Test
    fun `availability formula excludes no-telemetry periods`() {
        val result = AvailabilityCalculator.calculate(
            totalPeriodSeconds = 3600L,
            downtimeSeconds = 180L,
            noTelemetrySeconds = 1800L
        )
        // Observed = 1800s; ((1800 - 180) / 1800) * 100 = 90.0%
        assertTrue(result.hasTelemetry)
        assertEquals(90.0, result.availabilityPct, 0.01)

        val emptyTelemetry = AvailabilityCalculator.calculate(
            totalPeriodSeconds = 3600L,
            downtimeSeconds = 0L,
            noTelemetrySeconds = 3600L
        )
        assertFalse(emptyTelemetry.hasTelemetry)
        assertEquals(0.0, emptyTelemetry.availabilityPct, 0.01)
    }

    @Test
    fun `seven institutional agencies are present in both real and demo sets`() {
        val expected = listOf("MATRIZ", "CALDERÓN", "SANGOLQUÍ", "SUR", "POMASQUI", "SAN ANTONIO", "CENTRO")
        val realNames = DemoDataEngine.buildEmptyRealAgencies("uid_1").map { it.name }
        val demoNames = DemoDataEngine.buildInitialAgencies().map { it.name }
        assertEquals(expected, realNames)
        assertEquals(expected, demoNames)
    }

    @Test
    fun `ip masking enforces RBAC visibility rules`() {
        assertEquals("190.152.10.10", AvailabilityCalculator.maskIpIfUnauthorized("190.152.10.10", UserRole.ADMIN.canViewIp))
        assertEquals("190.152.10.10", AvailabilityCalculator.maskIpIfUnauthorized("190.152.10.10", UserRole.TECHNICIAN.canViewIp))
        assertTrue(AvailabilityCalculator.maskIpIfUnauthorized("190.152.10.10", UserRole.MANAGER.canViewIp).contains("Restringida"))
    }

    @Test
    fun `demo controller simulates drop and recovery lifecycle`() {
        val controller = DemoSandboxController()
        val dropAlert = controller.dropLink("matriz")
        assertNotNull(dropAlert)
        assertEquals("RED", controller.agencies.first { it.id == "matriz" }.linkStatus)

        val recAlert = controller.recoverLink("matriz")
        assertNotNull(recAlert)
        assertEquals("GREEN", controller.agencies.first { it.id == "matriz" }.linkStatus)
    }

    @Test
    fun `mttr averages resolved and closed incident durations`() {
        val incidents = listOf(
            Incident(status = IncidentStatus.RESOLVED.name, durationSeconds = 120),
            Incident(status = IncidentStatus.CLOSED.name, durationSeconds = 240),
            Incident(status = IncidentStatus.NEW.name, durationSeconds = 999)
        )
        assertEquals(180L, AvailabilityCalculator.calculateMttrSeconds(incidents))
    }
}
