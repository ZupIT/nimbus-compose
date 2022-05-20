package br.com.zup.nimbus.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import br.com.zup.nimbus.compose.sample.components.CustomError
import br.com.zup.nimbus.compose.sample.components.customComponents
import br.com.zup.nimbus.compose.sample.theme.AppTheme
import br.zup.com.nimbus.compose.Nimbus
import br.zup.com.nimbus.compose.NimbusConfig
import br.zup.com.nimbus.compose.NimbusNavigator
import br.zup.com.nimbus.compose.core.ui.components.components
import com.zup.nimbus.core.network.ViewRequest

class MainActivity : ComponentActivity() {
    private val config = NimbusConfig(
        baseUrl = BASE_URL,
        components = components + customComponents,
        logger = AppLogger(),
        errorView = { throwable: Throwable, retry: () -> Unit ->
            CustomError(throwable = throwable, retry = retry)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        Nimbus(config = config) {
                            Column {
                                NimbusNavigator(viewRequest = ViewRequest("/present.json"))
                                NimbusNavigator(viewRequest = ViewRequest("/screen1.json"))
                                NimbusNavigator(json = SCREEN1_JSON)
                            }
                        }
                    }

                }
            }
        }
    }
}


