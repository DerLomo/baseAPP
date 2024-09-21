package com.tnd.anycrypto

object TokenData {

    val tokenList = listOf(
        TokenItem(1,"USDC", R.drawable.usdc_poly, 6, "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359","0x89"),
        TokenItem(2,"USDC", R.drawable.usdc_eth, 6, "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359","0x1"),
        TokenItem(3,"Polygon", R.drawable.polygon, 6, "0x3c499c542cEF5E3811e1192ce70d8cC03d5c3359","0x89"),
        )

    data class TokenItem(
        val id: Int,
        val name: String,
        val imageResId: Int,
        val decimals: Int,
        val mintAddress: String,
        val chain: String
    )
}
