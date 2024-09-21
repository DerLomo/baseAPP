package com.tnd.anycrypto
class XMRUtils {
    companion object {
        private val rpcService = RpcService()

        fun getXMRPriceInUSD(callback: (Double?) -> Unit) {
            rpcService.getMoneroPrice(callback)
        }

    }
}