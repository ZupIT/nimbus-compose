package br.com.zup.nimbus.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import br.com.zup.nimbus.compose.sample.components.CustomError
import br.com.zup.nimbus.compose.sample.components.customLib
import br.com.zup.nimbus.compose.sample.components.layoutLib
import br.com.zup.nimbus.compose.sample.components.materialLib
import br.com.zup.nimbus.compose.sample.theme.AppTheme
import br.zup.com.nimbus.compose.Nimbus
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.ProvideNimbus
import br.zup.com.nimbus.compose.NimbusNavigator
import com.zup.nimbus.core.network.ViewRequest

class MainActivity : ComponentActivity() {
    private val nimbus = Nimbus(
        baseUrl = BASE_URL,
        ui = listOf(layoutLib, customLib, materialLib),
        errorView = { throwable: Throwable, retry: () -> Unit ->
            CustomError(throwable = throwable, retry = retry)
        },
        mode = if (BuildConfig.DEBUG) NimbusMode.Development else NimbusMode.Release,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        ProvideNimbus(nimbus) {
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
