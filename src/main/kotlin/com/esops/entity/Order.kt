package com.esops.entity

import java.math.BigInteger

enum class OrderType {
    BUY, SELL
}

data class Order(
    val orderId: String,
    val username: String,
    val type: OrderType,
    val quantity: BigInteger,
    val price: BigInteger,
    val esopType: EsopType,
    val filled: MutableList<Filled> = mutableListOf(),
    var remainingQuantity: BigInteger = quantity,
    val createdAt: Long = System.currentTimeMillis()
)

data class Filled(
    val orderId: String,
    val quantity: BigInteger = BigInteger("0"),
    val price: BigInteger = BigInteger("0")
)
