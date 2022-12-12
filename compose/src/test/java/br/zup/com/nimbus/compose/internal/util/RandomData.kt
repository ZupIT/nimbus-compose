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

package br.zup.com.nimbus.compose.internal.util

import kotlin.random.Random

object RandomData {

    fun boolean(): Boolean = Random.nextBoolean()

    fun int(): Int = Random.nextInt(1, 10000)

    fun double(): Double = Random.nextDouble(1.0, 10000.0)

    fun float(): Float = Random.nextFloat()

    fun string(size: Int = 20): String {
        val alphabet: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(size) { alphabet.random() }.joinToString("")
    }

    fun email(): String = "${string(5)}@${string(3)}.com"

    fun httpUrl(): String = "http://${string(5)}.com"

    fun httpsUrl(): String = "https://${string(5)}.com"
    
    fun jsonExample(): String = """{
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
}"""
}