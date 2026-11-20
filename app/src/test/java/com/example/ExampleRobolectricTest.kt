package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.ui.navigation.NocWorkspaceShell
import com.example.ui.theme.CotocollaoMonitorTheme
import com.example.ui.viewmodel.DemoViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `app name matches Cotocollao Monitor`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Cotocollao Monitor", appName)
    }

    @Test
    fun `demo workspace displays dashboard and navigates to 7 agencies`() {
        val vm = DemoViewModel()
        composeRule.setContent {
            CotocollaoMonitorTheme {
                val state by vm.uiState.collectAsState()
                NocWorkspaceShell(
                    state = state,
                    actions = vm,
                    isUnauthDemoSandbox = true,
                    onSignOutOrExitDemo = {}
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("COTOCOLLAO NETWORK MONITOR").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_agencias").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Agencias Monitoreadas (7)").assertIsDisplayed()
        composeRule.onNodeWithTag("agency_card_matriz").assertIsDisplayed()
        composeRule.onNodeWithTag("agencies_list").performScrollToNode(hasTestTag("agency_card_centro"))
        composeRule.onNodeWithTag("agency_card_centro").assertIsDisplayed()

        composeRule.onNodeWithTag("tab_incidentes").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Gestión de Incidentes").assertIsDisplayed()
        composeRule.onNodeWithText("Todas las agencias").assertIsDisplayed()
        composeRule.onNodeWithText("CENTRO").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("incidents_list").performScrollToNode(hasText("INC-003 • CENTRO"))
        composeRule.onNodeWithText("INC-003 • CENTRO").assertIsDisplayed()
    }
}
