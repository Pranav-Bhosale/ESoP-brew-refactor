package com.esops.entity

import com.esops.exception.InventoryException
import java.math.BigInteger

class InventoryNew(private val type: EsopType, private val maxValue: BigInteger = BigInteger("100000000000000000000")) {
    private var free = BigInteger.ZERO
    private var locked = BigInteger.ZERO
    fun add(amount: BigInteger) {
        if (amount <= BigInteger.ZERO)
            throw InventoryException(listOf("Amount to be added has to be positive."))
        val totalAmount = amount + free + locked
        if (totalAmount > maxValue)
            throw InventoryException(listOf("Invalid amount. Inventory maximum threshold will be exceeded."))

        free += amount
    }

    fun lockEsops(amount: BigInteger) {
        if (free < amount)
            throw InventoryException(listOf("Insufficient ESoPs."))

        free -= amount
        locked += amount
    }

    fun removeLockedEsops(amount: BigInteger) {
        if (locked - amount < BigInteger.ZERO)
            throw InventoryException(listOf("Invalid amount. Inventory amount has to be positive."))

        locked -= amount
    }

    fun getType(): EsopType {
        return type
    }
}