package com.esops.entity

import java.math.BigInteger

enum class OrderType {
    BUY, SELL
}

data class Order(
    val username: String,
    val type: OrderType,
    val quantity: BigInteger,
    val price: BigInteger,
    val esopType: EsopType,
    val filled: MutableList<Filled> = mutableListOf(),
    var remainingQuantity: BigInteger = quantity,
    val createdAt: Long = System.currentTimeMillis()
) {
    val orderId = OrderIdGenerator.generateOrderId()
}

data class Filled(
    val orderId: BigInteger,
    val quantity: BigInteger = BigInteger("0"),
    val price: BigInteger = BigInteger("0")
)

object OrderIdGenerator {
    private var orderId = BigInteger.ZERO
    fun generateOrderId(): BigInteger {
        orderId += BigInteger.ONE
        return orderId
    }
}