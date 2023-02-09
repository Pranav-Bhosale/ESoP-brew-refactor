package com.esops.entity

import com.esops.exception.InventoryException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger

class InventoryNewTest {
    @Test
    fun `should be able to add ESoPs`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)
        val amount = BigInteger.TEN

        assertDoesNotThrow { inventory.add(amount) }
    }

    @Test
    fun `should not be able to add negative amount`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)
        val amount = BigInteger("-10")

        val exception = assertThrows(InventoryException::class.java) { inventory.add(amount) }
        assertEquals("Amount to be added has to be positive.", exception.errorList[0])
    }

    @Test
    fun `should not be able to add more than the max limit`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)
        val amount = BigInteger("100000000000000000001")

        val exception = assertThrows(InventoryException::class.java) { inventory.add(amount) }
        assertEquals("Invalid amount. Inventory maximum threshold will be exceeded.", exception.errorList[0])
    }

    @Test
    fun `should not be able to lock more ESoPs than inventory has free`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)
        val amountToAdd = BigInteger.TWO
        val amountToLock = BigInteger.TEN
        inventory.add(amountToAdd)

        val exception = assertThrows(InventoryException::class.java) { inventory.lockEsops(amountToLock) }
        assertEquals("Insufficient ESoPs.", exception.errorList[0])
    }

    @Test
    fun `should not be able to remove more ESoPs than inventory has locked`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)
        val amountToLock = BigInteger.TWO
        val amountToRemove = BigInteger.TEN
        inventory.add(amountToLock)
        inventory.lockEsops(amountToLock)

        val exception = assertThrows(InventoryException::class.java) { inventory.removeLockedEsops(amountToRemove) }
        assertEquals("Invalid amount. Inventory amount has to be positive.", exception.errorList[0])
    }

    @Test
    fun `should be able to remove ESoPs`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)
        val amount = BigInteger.ONE

        inventory.add(amount)
        inventory.lockEsops(amount)

        assertDoesNotThrow { inventory.removeLockedEsops(amount) }
    }

    @Test
    fun `should be able to get type of inventory`() {
        val inventory = InventoryNew(EsopType.NON_PERFORMANCE)

        assertEquals(EsopType.NON_PERFORMANCE, inventory.getType())
    }
}