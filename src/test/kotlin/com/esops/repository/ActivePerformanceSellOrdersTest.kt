package com.esops.repository

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ActivePerformanceSellOrdersTest {
    private val activePerformanceSellOrders = ActivePerformanceSellOrders()

    @AfterEach
    fun tearDown() {
        activePerformanceSellOrders.clear()
    }

    @Test
    fun `should get null if none of the sell orders are viable`() {
        val highPriceOrder =
            Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(highPriceOrder)
        val lowPriceOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(lowPriceOrder)
        val buyOrder = Order("jake", OrderType.BUY, BigInteger.ONE, BigInteger("2"), EsopType.NON_PERFORMANCE)

        val bestSellOrder = activePerformanceSellOrders.getBestSellOrder(buyOrder)
        Assertions.assertNull(bestSellOrder)
    }

    @Test
    fun `should get sell order that comes first even if later ones have better price`() {
        val firstOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(secondOrder)
        val buyOrder = Order("jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)

        val bestSellOrder = activePerformanceSellOrders.getBestSellOrder(buyOrder)

        Assertions.assertNotNull(bestSellOrder)
        Assertions.assertEquals(firstOrder, bestSellOrder)
    }

    @Test
    fun `should be able to remove an order`() {
        val firstOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(secondOrder)
        val thirdOrder = Order("jake", OrderType.SELL, BigInteger.ONE, BigInteger("2"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(thirdOrder)
        val buyOrderThatMatchesWithEverything =
            Order("jake", OrderType.BUY, BigInteger.ONE, BigInteger.TEN, EsopType.NON_PERFORMANCE)

        val orderToBeDeleted = secondOrder
        activePerformanceSellOrders.removeOrderIfExists(orderToBeDeleted)

        while (true) {
            val currentOrder = activePerformanceSellOrders.getBestSellOrder(buyOrderThatMatchesWithEverything) ?: break
            assertNotEquals(currentOrder, orderToBeDeleted)
            activePerformanceSellOrders.removeOrderIfExists(currentOrder)
        }
    }
}