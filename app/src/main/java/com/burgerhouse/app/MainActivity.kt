package com.burgerhouse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import com.burgerhouse.app.data.repository.seedDatabaseIfEmpty
import com.burgerhouse.app.ui.navigation.AppNavigation
import com.burgerhouse.app.ui.theme.BurgerHouseTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Activity única do app.
 * Responsabilidades:
 *  1. Aplicar o seed do banco na primeira execução;
 *  2. Aplicar o tema (claro/escuro conforme preferência do usuário);
 *  3. Hospedar a navegação do app.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as BurgerHouseApp

        // Seed do banco em background (produtos e cupons na 1ª execução)
        lifecycleScope.launch {
            seedDatabaseIfEmpty(app.repository)
        }

        setContent {
            val darkMode by app.preferences.darkMode.collectAsState(
                initial = isSystemInDarkTheme()
            )

            BurgerHouseTheme(darkTheme = darkMode) {
                AppNavigation()
            }
        }
    }
}
