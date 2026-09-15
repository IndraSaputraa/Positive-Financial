package com.positivefinancial.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Displays a raw digit string (the actual stored/edited value, e.g. "2000000")
 * grouped with "." every three digits ("2.000.000") — the Indonesian convention
 * already used everywhere else in the app for Rupiah amounts. Only the on-screen
 * text changes; the field's real value stays plain digits, so no ViewModel
 * changes are needed to use this.
 */
class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val transformed = buildString {
            val reversedDigits = original.reversed()
            for (i in reversedDigits.indices) {
                if (i != 0 && i % 3 == 0) append('.')
                append(reversedDigits[i])
            }
        }.reversed()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val targetDigitCount = offset.coerceIn(0, original.length)
                var digitCount = 0
                var index = 0
                while (digitCount < targetDigitCount && index < transformed.length) {
                    if (transformed[index] != '.') digitCount++
                    index++
                }
                return index
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, transformed.length)
                var digitCount = 0
                for (i in 0 until clamped) {
                    if (transformed[i] != '.') digitCount++
                }
                return digitCount.coerceIn(0, original.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}
