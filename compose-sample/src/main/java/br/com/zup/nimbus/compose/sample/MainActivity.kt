package br.com.zup.nimbus.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import br.com.zup.nimbus.compose.sample.components.CustomError
import br.com.zup.nimbus.compose.sample.components.layoutLib
import br.com.zup.nimbus.compose.sample.theme.AppTheme
import br.zup.com.nimbus.compose.Nimbus
import br.zup.com.nimbus.compose.NimbusMode
import br.zup.com.nimbus.compose.ProvideNimbus
import br.zup.com.nimbus.compose.NimbusNavigator
import com.zup.nimbus.core.network.ViewRequest

const val JSON = """{
  "_:component": "layout:container",
  "children": [
    {
      "_:component": "material:text",
      "properties": {
        "text": "Valor: @{global}"
      }
    },
    {
      "_:component": "material:textInput",
      "properties": {
        "label": "Global",
        "value": "@{global}",
        "onChange": [{
          "_:action": "setState",
          "properties": {
            "path": "global",
            "value": "@{onChange}"
          }
        }]
      }
    }
  ]
}"""

const val ADDRESS = """{
  "_:component": "layout:container",
  "children": [
    {
      "_:component": "layout:container",
      "children": [
        {
          "_:component": "material:textInput",
          "properties": {
            "label": "Zip",
            "value": "@{global.address.zip}",
            "onChange": [
              {
                "_:action": "setState",
                "properties": {
                  "path": "global.address.zip",
                  "value": "@{onChange}"
                }
              }
            ],
            "onBlur": [
              {
                "_:action": "sendRequest",
                "properties": {
                  "onSuccess": [
                    {
                      "_:action": "setState",
                      "properties": {
                        "path": "global.address.city",
                        "value": "@{onSuccess.data.localidade}"
                      }
                    },
                    {
                      "_:action": "setState",
                      "properties": {
                        "path": "global.address.neighborhood",
                        "value": "@{onSuccess.data.bairro}"
                      }
                    },
                    {
                      "_:action": "setState",
                      "properties": {
                        "path": "global.address.state",
                        "value": "@{onSuccess.data.uf}"
                      }
                    },
                    {
                      "_:action": "setState",
                      "properties": {
                        "path": "global.address.street",
                        "value": "@{onSuccess.data.logradouro}"
                      }
                    }
                  ],
                  "url": "https://viacep.com.br/ws/@{onBlur}/json"
                }
              }
            ],
            "onFocus": null
          }
        },
        {
          "_:component": "material:textInput",
          "properties": {
            "label": "Street",
            "value": "@{global.address.street}",
            "onChange": [
              {
                "_:action": "setState",
                "properties": {
                  "path": "global.address.street",
                  "value": "@{onChange}"
                }
              }
            ],
            "onBlur": null,
            "onFocus": null
          }
        },
        {
          "_:component": "material:textInput",
          "properties": {
            "label": "Number",
            "value": "@{global.address.number}",
            "onChange": [
              {
                "_:action": "setState",
                "properties": {
                  "path": "global.address.number",
                  "value": "@{onChange}"
                }
              }
            ],
            "onBlur": null,
            "onFocus": null
          }
        },
        {
          "_:component": "material:textInput",
          "properties": {
            "label": "City",
            "value": "@{global.address.city}",
            "onChange": [
              {
                "_:action": "setState",
                "properties": {
                  "path": "global.address.city",
                  "value": "@{onChange}"
                }
              }
            ],
            "onBlur": null,
            "onFocus": null
          }
        },
        {
          "_:component": "material:textInput",
          "properties": {
            "label": "State",
            "value": "@{global.address.state}",
            "onChange": [
              {
                "_:action": "setState",
                "properties": {
                  "path": "global.address.state",
                  "value": "@{onChange}"
                }
              }
            ],
            "onBlur": null,
            "onFocus": null
          }
        },
        {
          "_:component": "material:button",
          "properties": {
            "text": "Cancel",
            "enabled": true,
            "onPress": [
              {
                "_:action": "pop",
                "properties": {
                  
                }
              }
            ]
          }
        },
        {
          "_:component": "material:button",
          "properties": {
            "text": "Next",
            "enabled": true,
            "onPress": [
              {
                "_:action": "push",
                "properties": {
                  "navigationState": {
                    "path": "address",
                    "value": "@{global.address}"
                  },
                  "url": "/nimbus/payment"
                }
              }
            ]
          }
        }
      ],
      "properties": {
        "stretch": false,
        "crossAxisAlignment": "start",
        "mainAxisAlignment": "start"
      }
    }
  ],
  "properties": {
    "stretch": false,
    "crossAxisAlignment": "start",
    "mainAxisAlignment": "start"
  }
}"""

class MainActivity : ComponentActivity() {
    private val nimbus = Nimbus(
        baseUrl = BASE_URL,
        components = listOf(layoutLib),
        logger = AppLogger(),
        errorView = { throwable: Throwable, retry: () -> Unit ->
            CustomError(throwable = throwable, retry = retry)
        },
        mode = if (BuildConfig.DEBUG) NimbusMode.Development else NimbusMode.Release,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //nimbus.globalState.set(ArrayList<Any>())
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column {
                        TextFieldTest()
                        ProvideNimbus(nimbus) {
                            Column {
                                /*NimbusNavigator(viewRequest = ViewRequest("/present.json"))
                                NimbusNavigator(viewRequest = ViewRequest("/screen1.json"))
                                NimbusNavigator(json = SCREEN1_JSON)*/
                                NimbusNavigator(json = JSON)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextFieldTest() {
    val (value, setValue) = remember { mutableStateOf("") }
    Column {
        Text("Native value:")
        Text(value)
        TextField(
            value = value ?: "",
            onValueChange = {
                setValue(it)
            },
            label = { Text("Nativo") },
            modifier = Modifier.onFocusChanged {
                if (it.isFocused) print("Gained focus")
                else print("Lost focus")
            }
        )
    }
}


