/*
 * Copyright 2020, 2022 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
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

package br.zup.com.nimbus.compose

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okio.Buffer
import org.json.JSONObject

class SimpleJsonAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): JSONObject? {
        return reader.readObject()
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: JSONObject?) {
        val json = value.toString()
        val buffer = Buffer()
        buffer.write(json.toByteArray())
        writer.value(buffer)
    }
}