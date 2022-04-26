package br.zup.com.nimbus.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.zup.beagle.android.ui.theme.BeagleTheme
import com.zup.nimbus.core.ServerDrivenConfig

class MainActivity : ComponentActivity() {
    private val config = ServerDrivenConfig(baseUrl = "http://10.0.2.2:8080", platform = "android")
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


