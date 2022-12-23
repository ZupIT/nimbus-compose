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

val json1 = """
    {
      "id": "1",
      "component": "layout:container",
      "children": [
        {
          "id": "2",
          "component": "material:text",
          "properties": {
            "text": "Nimbus App @{counter}"
          }
        },
        {
          "id": "3",
          "component": "material:text",
          "properties": {
            "text": "Hi There"
          }
        },
        {
          "id": "4",
          "component": "custom:personCard",
          "properties": {
            "person": {
              "name": "Fulano da Silva",
              "age": 28,
              "company": "ZUP",
              "document": "014.778.547-56"
            },
            "address": {
              "street": "Rua dos bobos",
              "number": 0,
              "zip": "47478-745"
            }
          }
        },
        {
          "id": "5",
          "component": "material:button",
          "properties": {
            "text": "Increment counter",
            "onPress": "[[ACTION:INC_COUNTER]]"
          }
        }
      ]
    }
""".trimIndent()

const val UPDATE_TEST = """{
  "_:component": "layout:container",
  "children": [
    {
      "_:component": "layout:container",
      "properties": {
        "backgroundColor": "@{global.backgroundColor}",
        "padding": 10
      },
      "children": [
        {
          "_:component": "layout:container",
          "children": [
            {
              "_:component": "material:text",
              "properties": {
                "text": "Row 1"
              }
            }
          ]
        },
        {
          "_:component": "material:button",
          "properties": {
            "text": "Change BG to red",
            "onPress": [
              {
                "_:action": "setState",
                "properties": {
                  "path": "global.backgroundColor",
                  "value": "#FF0000"
                }
              }
            ]
          }
        }
      ]
    },
    {
      "_:component": "layout:container",
      "properties": {
        "padding": 10
      },
      "children": [
        {
          "_:component": "material:text",
          "properties": {
            "text": "Row 2"
          }
        }
      ]
    },
    {
      "_:component": "layout:container",
      "properties": {
        "padding": 10
      },
      "children": [
        {
          "_:component": "material:text",
          "properties": {
            "text": "Row 3"
          }
        }
      ]
    }
  ]
}
"""

const val FILTER_TEST = """{
  "_:component":"layout:column",
  "id":"container",
  "state":{
    "searchTerm": "",
    "notes":{
      "1671408000000": [
        {
          "id": 11,
          "title": "Buy cereal for the kids",
          "description": "5 boxes",
          "date": 1671408000000,
          "isDone": false
        },
        {
          "id": 10,
          "title": "Hairdresser appointment",
          "description": "2PM, on 8th avenue",
          "date": 1671408000000,
          "isDone": false
        },
        {
          "id": 9,
          "title": "Research dates and prices for family trip",
          "description": "Beach cities",
          "date": 1671408000000,
          "isDone": false
        },
        {
          "id": 8,
          "title": "Pick up clothes at the shop",
          "description": "They're opened until 6PM",
          "date": 1671408000000,
          "isDone": true
        },
        {
          "id": 7,
          "title": "Pay credit card bill",
          "description": "",
          "date": 1671408000000,
          "isDone": false
        },
        {
          "id": 6,
          "title": "Pick up kids at school",
          "description": "",
          "date": 1671408000000,
          "isDone": true
        }
      ],
      "1671148800000": [
        {
          "id": 5,
          "title": "Julia's birthday",
          "description": "Must buy a small gift",
          "date": 1671148800000,
          "isDone": false
        },
        {
          "id": 4,
          "title": "Remember to eat more healthily",
          "description": "Must lose some pounds",
          "date": 1671148800000,
          "isDone": false
        },
        {
          "id": 3,
          "title": "Cancel streaming subscription",
          "description": "Netflix and Disney+",
          "date": 1671148800000,
          "isDone": true
        }
      ],
      "1670976000000": [
        {
          "id": 2,
          "title": "Prepare special breakfast",
          "description": "Anniversary",
          "date": 1670976000000,
          "isDone": true
        },
        {
          "id": 1,
          "title": "Finish marketing campaign",
          "description": "Also prepare presentation",
          "date": 1670976000000,
          "isDone": true
        },
        {
          "id": 0,
          "title": "Need to cook dinner today",
          "description": "Meat balls spaghetti",
          "date": 1670889600000,
          "isDone": false
        },
        {
          "id": 12,
          "title": "Buy new edition of Forbes Magazine",
          "description": "When at the mall",
          "date": 1670889600000,
          "isDone": true
        }
      ]
    },
    "filtered": {}
  },
  "children":[
    {
      "_:component":"material:button",
      "id":"start",
      "properties":{
        "text":"Start",
        "onPress":[
          {
            "_:action":"setState",
            "properties":{
              "path":"filtered",
              "value":"@{notes}"
            }
          }
        ]
      }
    },
    {
      "_:component":"material:textInput",
      "id":"filter",
      "properties":{
        "label": "search",
        "value": "@{searchTerm}",
        "onChange":[
          {
            "_:action":"setState",
            "properties":{
              "path":"searchTerm",
              "value":"@{onChange}"
            }
          },
          {
            "_:action":"setState",
            "properties":{
              "path":"filtered",
              "value":"@{filterNotes(notes, onChange)}"
            }
          }
        ]
      }
    },
    {
      "_:component":"forEach",
      "properties":{
        "key":"key",
        "items":"@{entries(filtered)}"
      },
      "children":[
        {
          "_:component":"layout:column",
          "id": "section",
          "children": [
            {
              "_:component":"layout:text",
              "properties":{
                "text":"@{item.key}"
              }
            },
            {
              "_:component":"forEach",
              "properties":{
                "key":"id",
                "items":"@{item.value}"
              },
              "children":[
                {
                  "_:component":"layout:text",
                  "properties":{
                    "text":"@{item.id}: @{item.title}: @{item.description}"
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}"""
