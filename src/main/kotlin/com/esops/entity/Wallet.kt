package com.esops.entity

import com.esops.exception.WalletException
import java.math.BigInteger

class WalletNew(private var free: BigInteger = BigInteger.ZERO, private var locked: BigInteger = BigInteger.ZERO) {
    private val maxValue: BigInteger = BigInteger("100000000000000000000")

    fun add(amount: BigInteger) {
        if (amount <= BigInteger.ZERO)
            throw WalletException(listOf("Amount to be added has to be positive."))
        val totalAmount = amount + free + locked
        if (totalAmount > maxValue)
            throw WalletException(listOf("Invalid amount. Wallet maximum threshold will be exceeded."))

        free += amount
    }

    fun lockMoney(amount: BigInteger) {
        if (free < amount)
            throw WalletException(listOf("Insufficient funds."))

        free -= amount
        locked += amount
    }

    fun removeLockedMoney(amount: BigInteger) {
        if (locked - amount < BigInteger.ZERO)
            throw WalletException(listOf("Invalid amount. Wallet amount has to be positive."))

        locked -= amount
    }
}
