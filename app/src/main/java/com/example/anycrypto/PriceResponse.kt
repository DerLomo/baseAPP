package com.tnd.anycrypto

data class PriceResponse(
    val data: Map<String, TokenInfo>
)

data class TokenInfo(
    val id: String,
    val mintSymbol: String,
    val vsToken: String,
    val vsTokenSymbol: String,
    val price: Double
)

