package com.lucafilippi.rotaeuropa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucafilippi.rotaeuropa.data.RotaEuropaRepository
import com.lucafilippi.rotaeuropa.data.UserPreferencesRepository
import com.lucafilippi.rotaeuropa.data.local.RotaEuropaDatabase
import com.lucafilippi.rotaeuropa.presentation.RotaEuropaViewModel
import com.lucafilippi.rotaeuropa.presentation.RotaEuropaViewModelFactory
import com.lucafilippi.rotaeuropa.presentation.RotaEuropaApp
import com.lucafilippi.rotaeuropa.ui.theme.ROTAEUROPATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = RotaEuropaDatabase.get(applicationContext)
        val factory = RotaEuropaViewModelFactory(
            RotaEuropaRepository(database),
            UserPreferencesRepository(applicationContext),
        )
        val viewModel = ViewModelProvider(this, factory)[RotaEuropaViewModel::class.java]
        setContent {
            val state = viewModel.uiState.collectAsStateWithLifecycle().value
            ROTAEUROPATheme(darkTheme = state.darkTheme) {
                RotaEuropaApp(state = state, viewModel = viewModel)
            }
        }
    }
}
