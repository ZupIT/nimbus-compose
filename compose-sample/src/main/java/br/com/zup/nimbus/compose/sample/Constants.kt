package br.com.zup.nimbus.compose.sample


const val BASE_URL = "https://gist.githubusercontent.com/Tiagoperes/" +
        "74808ebd7ad7f0645491fc60436223a6/raw/85702801aa8b5c6d62d8d500ef9a262b767ec94c"


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

const val COUNTER_JSON = """{
  "_:component": "layout:container",
  "state": {
    "id": "counter",
    "value": 0
  },
  "children": [
    {
      "_:component": "layout:container",
      "children": [
        {
          "_:component": "material:text",
          "properties": {
            "text": "Counter: @{counter}"
          }
        },
        {
          "_:component": "material:button",
          "properties": {
            "text": "Next",
            "onPress": [{
              "_:action": "setState",
              "properties": {
                "path": "counter",
                "value": "@{sum(counter, 1)}"
              }
            }]
          }
        }
      ]
    }
  ]
}"""
