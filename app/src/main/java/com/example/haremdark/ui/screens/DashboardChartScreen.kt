package com.example.haremdark.ui.screens

import androidx.compose.runtime.Composable
import com.example.haremdark.domain.GameEngine

/**
 * Resource Management and Analytics Dashboard (Vico charts for Currency, Materials, and Influence).
 */
@Composable
fun DashboardChartScreen(
    engine: GameEngine,
    onBack: (() -> Unit)? = null
) {
    ResourceManagementDashboardScreen(
        engine = engine,
        onBack = onBack
    )
}
