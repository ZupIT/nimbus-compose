package br.com.zup.nimbus.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import br.com.zup.nimbus.compose.sample.components.customComponents
import br.com.zup.nimbus.compose.sample.theme.AppTheme
import br.zup.com.nimbus.compose.NimbusProvider
import br.zup.com.nimbus.compose.components.components
import br.zup.com.nimbus.compose.serverdriven.Nimbus

class MainActivity : ComponentActivity() {
    private val config = Nimbus(
        baseUrl = BASE_URL,
        components = components + customComponents,
        loadingView = { Text("Custom Loading from app") },
        logger = AppLogger()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    NimbusProvider("/screen1.json", config)
                }
            }
        }
    }
}


