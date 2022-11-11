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
