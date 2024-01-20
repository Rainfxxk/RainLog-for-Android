package com.rain.rainlog.http

import okhttp3.CacheControl
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpClient {
    companion object {
        public fun send(url:String, formBody: FormBody, callback: Callback) {
            var okHttpClient = OkHttpClient()
            var request: Request? = null
            if (HttpConfig.SESSIONID != null) {
                request = Request.Builder()
                    .url(HttpConfig.BASEURL + url)
                    .post(formBody)
                    .addHeader("cookie", HttpConfig.SESSIONID!!)
                    .build()
            }
            else {
                request = Request.Builder()
                    .url(HttpConfig.BASEURL + url)
                    .post(formBody)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
            }
            var call = okHttpClient.newCall(request)
            call.enqueue(callback)
        }

        public fun send(url:String, callback: Callback) {
            var okHttpClient = OkHttpClient()
            var request: Request? = null
            if (HttpConfig.SESSIONID != null) {
                request = Request.Builder()
                    .url(HttpConfig.BASEURL + url)
                    .addHeader("cookie", HttpConfig.SESSIONID!!)
                    .build()
            }
            else {
                request = Request.Builder()
                    .url(HttpConfig.BASEURL + url)
                    .cacheControl(CacheControl.FORCE_NETWORK)
                    .build()
            }
            var call = okHttpClient.newCall(request)
            call.enqueue(callback)
        }
    }
}