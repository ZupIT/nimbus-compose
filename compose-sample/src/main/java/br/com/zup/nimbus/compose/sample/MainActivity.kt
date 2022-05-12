package br.com.zup.nimbus.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import br.com.zup.nimbus.compose.sample.components.customComponents
import br.com.zup.nimbus.compose.sample.theme.AppTheme
import br.zup.com.nimbus.compose.Nimbus
import br.zup.com.nimbus.compose.NimbusConfig
import br.zup.com.nimbus.compose.NimbusNavigator
import br.zup.com.nimbus.compose.core.ui.components.components

class MainActivity : ComponentActivity() {
    private val nimbusConfig = NimbusConfig(
        baseUrl = BASE_URL,
        components = components + customComponents,
        loadingView = { CircularProgressIndicator() },
        logger = AppLogger()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Nimbus(nimbusConfig = nimbusConfig) {
                            Column {
                                NimbusNavigator(initialUrl = "/screen1.json")
                                NimbusNavigator(initialUrl = "/screen1.json")
                            }
                        }
                    }

                }
            }
        }
    }
}


