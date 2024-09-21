package com.tnd.anycrypto

import android.content.Context
import android.util.Log
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.SDKOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class EVMUtils(context: Context) {
    private val dappMetadata = DappMetadata("AnyCrypto", "https://www.anycrypto.com")
    private val infuraAPIKey = "3a2cee4ee80f4387a1f2e10b0b0caaf5"
    private val readonlyRPCMap = emptyMap<String, String>()
    private val API_URL = "https://api.1inch.dev/price/v1.1/1/"

    val ethereum = Ethereum(context, dappMetadata, SDKOptions(infuraAPIKey, readonlyRPCMap))

    fun connectMetaMask(callback: (Result<MetaMaskConnection>) -> Unit) {
        ethereum.connect { result ->
            when (result) {
                is io.metamask.androidsdk.Result.Success.Items -> {
                    Log.d("EVMUtils", "Connected successfully, requesting accounts and chain")
                    Log.d("PUTA", "${result}")
                    Log.d("PUTA II", "${ethereum.chainId}")

                    // Extract the address from the result
                    val address = result.value.firstOrNull() ?: ""

                    // Return the connection details
                    callback(Result.success(MetaMaskConnection(address, ethereum.chainId)))
                }

                is io.metamask.androidsdk.Result.Error -> {
                    Log.e("EVMUtils", "Connection error: ${result.error.message}")
                    callback(Result.failure(Exception("Connection failed: ${result.error.message}")))
                }

                else -> {
                    Log.e("EVMUtils", "Unexpected result during connection")
                    callback(Result.failure(Exception("Unexpected result during connection")))
                }
            }
        }
    }
    fun getTokenPriceInDollars(token: TokenData.TokenItem, callback: (Double?) -> Unit) {
            val tokenAddress = token.mintAddress
            val url = "$API_URL$tokenAddress"
            val apiKey = BuildConfig.API1INCH


            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    callback(null)  // Return null in case of error
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            callback(null)
                            return
                        }

                        val responseBody = it.body?.string() ?: ""
                        val json = JSONObject(responseBody)
                        val price = json.optDouble(token.mintAddress, -1.0)

                        if (price != -1.0) {
                            callback(price)
                        } else {
                            callback(null)
                        }
                    }
                }
            })
    }


    data class MetaMaskConnection(
        val address: String,
        val chainId: String
    )
}