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
