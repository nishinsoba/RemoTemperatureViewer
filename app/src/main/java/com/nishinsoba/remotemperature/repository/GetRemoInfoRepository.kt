package com.nishinsoba.remotemperature.repository

import com.nishinsoba.remotemperature.dataclass.GetRemoInfoResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.util.concurrent.TimeUnit


class GetRemoInfoRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun get(fromStr: String, toStr: String, token:String): Pair<Int,GetRemoInfoResponse?>
    {
        val url = "https://xoelshjsze.execute-api.ap-northeast-1.amazonaws.com/default/GetRemoInfo?from=$fromStr&to=$toStr"
        var code: Int = 999
        var getRemoInfoResponse: GetRemoInfoResponse? = null
        try {
            val request = Request.Builder()
                .url(url)
                .header("x-api-key",token)
                .build()

            val response = client.newCall(request).execute()
            if (response.code == 401 || response.code == 403){
                return Pair(response.code, null)
            }else if (response.code != 200){
                return Pair(response.code, null)
            }

            val adapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(GetRemoInfoResponse::class.java)
            getRemoInfoResponse = adapter.fromJson(response.body!!.string())
            code = response.code
        }catch (e: IllegalArgumentException){
            //headerに不正な文字が入った時
            e.printStackTrace()
            return Pair(401,null)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return Pair(code,getRemoInfoResponse)
    }
}