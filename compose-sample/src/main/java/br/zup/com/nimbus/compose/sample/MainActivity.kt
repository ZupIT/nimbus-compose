package br.zup.com.nimbus.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import br.zup.com.nimbus.compose.NimbusProvider
import br.zup.com.nimbus.compose.components.components
import br.zup.com.nimbus.compose.sample.components.customComponents
import br.zup.com.nimbus.compose.serverdriven.NimbusService
import com.zup.beagle.android.ui.theme.BeagleTheme

class MainActivity : ComponentActivity() {
    private val config = NimbusService(baseUrl = "http://10.0.2.2:8080",
        components = components + customComponents
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeagleTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    NimbusProvider(json1, config)
                }
            }
        }
    }
}


