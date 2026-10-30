package com.lucafilippi.rotaeuropa

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppAccessibilitySmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun primaryDestinationsExposeReadableLabelsAndCoreSections() {
        compose.onNodeWithText("Início").assertIsDisplayed()
        compose.onNodeWithText("Finanças").performClick()
        compose.onNodeWithText("Cenários financeiros").assertIsDisplayed()
        compose.onNodeWithText("Mais").performClick()
        compose.onNodeWithText("Lembretes locais").assertIsDisplayed()
    }
}
