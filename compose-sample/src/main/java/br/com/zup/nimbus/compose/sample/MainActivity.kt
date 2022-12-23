/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import br.com.zup.nimbus.compose.Nimbus
import br.com.zup.nimbus.compose.NimbusMode
import br.com.zup.nimbus.compose.ProvideNimbus
import br.com.zup.nimbus.compose.NimbusNavigator
import br.com.zup.nimbus.core.network.ViewRequest

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
                            NimbusNavigator(json = FILTER_TEST)
                        }
                    }
                }
            }
        }
    }
}
