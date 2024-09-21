package com.tnd.anycrypto

import android.content.Context
import android.util.Log
import io.metamask.androidsdk.Ethereum
import io.metamask.androidsdk.DappMetadata
import io.metamask.androidsdk.SDKOptions
import io.metamask.androidsdk.EthereumRequest

class EVMUtils(context: Context) {
    private val dappMetadata = DappMetadata("AnyCrypto", "https://www.anycrypto.com")
    private val infuraAPIKey = "3a2cee4ee80f4387a1f2e10b0b0caaf5" // Replace with your actual Infura API key
    private val readonlyRPCMap = emptyMap<String, String>() // Empty map if you don't need specific RPCs

    val ethereum = Ethereum(context, dappMetadata, SDKOptions(infuraAPIKey, readonlyRPCMap))

    fun connectMetaMask(callback: (Result<MetaMaskConnection>) -> Unit) {
        ethereum.connect { result ->
            when (result) {
                is io.metamask.androidsdk.Result.Success.Items -> {
                    Log.d("EVMUtils", "Connected successfully, requesting accounts and chain")
                    Log.d("PUTA","${result}")
                    Log.d("PUTA II" ,"${ethereum.chainId}")

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

    data class MetaMaskConnection(
        val address: String,
        val chainId: String
    )
}