package br.zup.com.nimbus.compose

import com.zup.nimbus.core.network.ServerDrivenHttpMethod
import java.io.Serializable

data class ViewRequest222(/**
                    * The URL to send the request to. When it starts with "/", it's relative to the BaseUrl.
                    */
                   val url: String,
                   /**
                    * The request method. Default is "Get".
                    */
                   val method: ServerDrivenHttpMethod = ServerDrivenHttpMethod.Get,
                   /**
                    * The headers for the request.
                    */
                   val headers: Map<String, String>? = null,
                   /**
                    * The request body. Invalid for "Get" requests.
                    */
                   val body: String? = null,
                   /**
                    * UI tree to show if an error occurs and the view can't be fetched.
                    */
                   val fallback: String? = null) : Serializable