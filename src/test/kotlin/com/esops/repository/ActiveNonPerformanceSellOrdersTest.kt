package com.esops.repository

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ActiveNonPerformanceSellOrdersTest {
    private val activeNonPerformanceSellOrders = ActiveNonPerformanceSellOrders()

    @AfterEach
    fun tearDown() {
        activeNonPerformanceSellOrders.clear()
    }

    @Test
    fun `should be able to add sell order`() {
        val order = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger.ONE, EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(order)
        val bestSellOrder = activeNonPerformanceSellOrders.getBestSellOrder()

        assertNotNull(bestSellOrder)
        assertEquals(order, bestSellOrder)
    }

    @Test
    fun `should get sell order with lowest order price`() {
        val lowPriceOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(lowPriceOrder)
        val highPriceOrder =
            Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(highPriceOrder)

        val bestSellOrder = activeNonPerformanceSellOrders.getBestSellOrder()

        assertNotNull(bestSellOrder)
        assertEquals(lowPriceOrder, bestSellOrder)
    }

    @Test
    fun `should get sell order that came first if same price`() {
        val firstOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(secondOrder)

        val bestSellOrder = activeNonPerformanceSellOrders.getBestSellOrder()

        assertNotNull(bestSellOrder)
        assertEquals(firstOrder, bestSellOrder)
    }

    @Test
    fun `should be able to remove an order`() {
        val firstOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(secondOrder)
        val thirdOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("2"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(thirdOrder)

        val orderToBeDeleted = secondOrder
        activeNonPerformanceSellOrders.removeOrderIfExists(orderToBeDeleted)

        while (true) {
            val currentOrder = activeNonPerformanceSellOrders.getBestSellOrder() ?: break
            Assertions.assertNotEquals(currentOrder, orderToBeDeleted)
            activeNonPerformanceSellOrders.removeOrderIfExists(currentOrder)
        }
    }
}