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


const val BASE_URL = "https://gist.githubusercontent.com/hernandazevedozup/" +
        "c525a75a7706afc7816cf12d9213eda3/raw/4744d06218851df10621cdae674a04682861d078"


const val SCREEN1_JSON = """{
  "_:component": "layout:container",
  "children": [
    {
      "_:component": "material:text",
      "properties": {
        "text": "Screen 1"
      }
    },
    {
      "_:component": "material:button",
      "properties": {
        "text": "Next",
        "onPress": [{
          "_:action": "push",
          "properties": {
            "url": "/screen2.json"
          }
        }]
      }
    }
  ]
}"""
