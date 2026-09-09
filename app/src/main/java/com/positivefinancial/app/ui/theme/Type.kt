package com.positivefinancial.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PositiveFinancialTypography = Typography().let { base ->
    Typography(
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge,
        bodyMedium = base.bodyMedium,
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold)
    )
}

val MoneyDisplayStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp
)
