# [**Nimbus Compose**](https://github.com/ZupIT/nimbus-docs/) &middot; [![GitHub license](https://img.shields.io/badge/license-Apache%202.0-blue)](https://github.com/ZupIT/nimbus-compose/blob/main/LICENSE.txt) [![maven version](https://img.shields.io/maven-central/v/br.com.zup.nimbus/nimbus-compose)](https://search.maven.org/artifact/br.com.zup.nimbus/nimbus-compose) [![CI/CD Status](https://github.com/ZupIT/nimbus-compose/actions/workflows/validation.yml/badge.svg?branch=main)](https://github.com/ZupIT/nimbus-compose/actions/workflows/validation.yml) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/ZupIT/nimbus-compose/blob/main/CONTRIBUTING.md) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ZupIT_nimbus_compose&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ZupIT_nimbus_compose)

Nimbus is a Server Driven UI library written for Jetpack Compose and SwiftUI.

This repository holds the Compose implementation.

# Nimbus for Compose: Getting started
## Pre-requisites

- [Latest Android Studio (recommended)](https://developer.android.com/studio)
- [Jdk Minimum 1.8](https://www.oracle.com/java/technologies/downloads/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Android API minSdk = 21
- Kotlin 1.6.x

# Installing
You can download the Nimbus library from mavenCentral with the below configuration on your build.gradle

```
allprojects {
    repositories {
        mavenCentral()
    }
}

```

``` 
  // Nimbus compose base library
  implementation "br.com.zup.nimbus:nimbus-compose:${nimbusComposeVersion}". 
  // Recommended if you dont want to implement the layout components yourself
  implementation "br.com.zup.nimbus:nimbus-layout-compose:${nimbusComposeLayoutVersion}" 
```

# Rendering your first server-driven screen

## Rendering with a endpoint url


```kotlin
class MainActivity : ComponentActivity() {
    private val config = NimbusConfig(
        baseUrl = "http://myapi.com",
        components = layoutComponents,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Nimbus(config = config) {
                        NimbusNavigator(ViewRequest("/homepage"))
                    }
                }
            }
        }
    }
}
```
### Create on your endpoint http://myapi.com/homepage the json below
```
{
  "_:component": "layout:row",
  "children": [
    {
      "_:component": "layout:row",
      "children": [{
        "_:component": "layout:text",
        "properties": {
          "text": "r"
        }
      }],
      "properties": {
        "flex":2,
        "backgroundColor": "#FF0000"
      }
    },
    {
      "_:component": "layout:row",
      "children": [{
        "_:component": "layout:text",
        "properties": {
          "text": "g"
        }
      }],
      "properties": {
        "flex":1,
        "backgroundColor": "#00FF00"
      }
    },
    {
      "_:component": "layout:row",
      "children": [{
        "_:component": "layout:text",
        "properties": {
          "text": "b"
        }
      }],
      "properties": {
        "flex":1,
        "backgroundColor": "#0000FF"
      }
    }]
}
```

* Note you can also load this json whithin your app using the code snippet below
```
NimbusNavigator(json = YOUR_JSON)
```

### The result on your app's screen
<img src="https://github.com/ZupIT/nimbus-layout-compose/blob/main/layout/screenshots/debug/br.com.zup.nimbus.compose.layout.LayoutFlexTest_test_layout_1.png" width="228"/>

## **Documentation**

You can find Nimbus's documentation on our [**website**](https://github.com/ZupIT/nimbus-docs/).

[nimbus-docs]: https://github.com/ZupIT/nimbus-docs/

## **Running the sample project**

1. Cloning the repo
2. open the nimbus-compose folder using Android Studio
3. Select the compose-sample module run on emulator or device.

## **License**

[**Apache License 2.0**](https://github.com/ZupIT/nimbus-compose/blob/main/LICENSE.txt).