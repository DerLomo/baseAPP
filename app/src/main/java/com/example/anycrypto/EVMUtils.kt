package com.tnd.anycrypto

import android.content.Context
import android.util.Log
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.SDKOptions
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.math.BigInteger

class EVMUtils(context: Context) {
    private val dappMetadata = DappMetadata("AnyCrypto", "https://www.anycrypto.com")
    val infuraAPIKey = BuildConfig.INFRA
    private val readonlyRPCMap = emptyMap<String, String>()
    private val API_URL = "https://api.1inch.dev/price/v1.1/1/"
    private val circleAPIKey = BuildConfig.CIRCLE_API_KEY

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

    fun sendUSDC(
        fromChainId: String,
        toChainId: String,
        amount: BigInteger,
        recipient: String,
        callback: (Result<String>) -> Unit
    ) {
        // 1. Initiate a transfer using Circle's CCTP endpoint
        val url = "$API_URL/transfers"
        val client = OkHttpClient()

        val requestBody = JSONObject().apply {
            put("sourceChainId", fromChainId)
            put("destinationChainId", toChainId)
            put("amount", amount.toString()) // Amount in smallest unit (wei)
            put("recipient", recipient)
            put("token", "USDC")
        }

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.get("application/json"), requestBody.toString()))
            .addHeader("Authorization", "Bearer $circleAPIKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("EVMUtils", "Error sending USDC: ${e.message}")
                e.printStackTrace()
                callback(Result.failure(Exception("Failed to send USDC: ${e.message}")))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("EVMUtils", "Error response from Circle CCTP: ${response.code}")
                        callback(Result.failure(Exception("Circle CCTP failed with code: ${response.code}")))
                        return
                    }

                    val responseBody = it.body?.string() ?: ""
                    Log.d("EVMUtils", "Circle CCTP response: $responseBody")

                    try {
                        val json = JSONObject(responseBody)
                        val transferId = json.optString("transferId", "")

                        if (transferId.isNotEmpty()) {
                            Log.d("EVMUtils", "USDC transfer initiated, transferId: $transferId")
                            callback(Result.success(transferId))
                        } else {
                            Log.w("EVMUtils", "Failed to get transferId")
                            callback(Result.failure(Exception("Invalid response, no transferId")))
                        }
                    } catch (e: Exception) {
                        Log.e("EVMUtils", "Error parsing Circle CCTP response: ${e.message}")
                        callback(Result.failure(Exception("JSON parsing error: ${e.message}")))
                    }
                }
            }
        })
    }

    fun getTokenPriceInDollars(token: TokenData.TokenItem, callback: (Double?) -> Unit) {
        val tokenAddress = token.mintAddress
        val apiKey = BuildConfig.API1INCH
        val url = "$API_URL$tokenAddress"

        Log.d("EVMUtils", "Fetching price for token: ${token.name}, address: $tokenAddress, chain: ${token.chain}")
        Log.d("EVMUtils", "API URL: $url")
        Log.d("EVMUtils", "Using API Key: $apiKey")

        // Check if the token is USDC and return 1.0 immediately
        if (token.name.equals("USDC", ignoreCase = true)) {
            Log.d("EVMUtils", "Token is USDC. Returning price 1.0")
            callback(1.0)
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("EVMUtils", "Error fetching token price: ${e.message}")
                e.printStackTrace()
                callback(null)  // Return null in case of error
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("EVMUtils", "Unsuccessful response: ${response.code}")
                        callback(null)
                        return
                    }

                    val responseBody = it.body?.string() ?: ""
                    Log.d("EVMUtils", "Response body: $responseBody")

                    try {
                        val json = JSONObject(responseBody)
                        val price = json.optDouble(token.mintAddress, -1.0)

                        if (price != -1.0) {
                            Log.d("EVMUtils", "Price found for token: $price")
                            callback(price)
                        } else {
                            Log.w("EVMUtils", "Price not found for token with address: $tokenAddress")
                            callback(null)
                        }
                    } catch (e: Exception) {
                        Log.e("EVMUtils", "Error parsing JSON response: ${e.message}")
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