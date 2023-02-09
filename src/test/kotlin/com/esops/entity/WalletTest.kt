package com.esops.entity

import com.esops.exception.WalletException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger

class WalletTest {
    @Test
    fun `should be able to add money`() {
        val wallet = Wallet()
        val amount = BigInteger.TEN

        assertDoesNotThrow { wallet.add(amount) }
    }

    @Test
    fun `should not be able to add negative amount`() {
        val wallet = Wallet()
        val amount = BigInteger("-10")

        val exception = assertThrows(WalletException::class.java) { wallet.add(amount) }
        assertEquals("Amount to be added has to be positive.", exception.errorList[0])
    }

    @Test
    fun `should not be able to add more than the max limit`() {
        val wallet = Wallet()
        val amount = BigInteger("100000000000000000001")

        val exception = assertThrows(WalletException::class.java) { wallet.add(amount) }
        assertEquals("Invalid amount. Wallet maximum threshold will be exceeded.", exception.errorList[0])
    }

    @Test
    fun `should not be able to lock more money than wallet has free`() {
        val wallet = Wallet()
        val amountToAdd = BigInteger.TWO
        val amountToLock = BigInteger.TEN
        wallet.add(amountToAdd)

        val exception = assertThrows(WalletException::class.java) { wallet.lockMoney(amountToLock) }
        assertEquals("Insufficient funds.", exception.errorList[0])
    }

    @Test
    fun `should not be able to remove more money than wallet has locked`() {
        val wallet = Wallet()
        val amountToLock = BigInteger.TWO
        val amountToRemove = BigInteger.TEN
        wallet.add(amountToLock)
        wallet.lockMoney(amountToLock)

        val exception = assertThrows(WalletException::class.java) { wallet.removeLockedMoney(amountToRemove) }
        assertEquals("Invalid amount. Wallet amount has to be positive.", exception.errorList[0])
    }

    @Test
    fun `should be able to remove money`() {
        val wallet = Wallet()
        val amount = BigInteger.ONE

        wallet.add(amount)
        wallet.lockMoney(amount)

        assertDoesNotThrow { wallet.removeLockedMoney(amount) }
    }
}