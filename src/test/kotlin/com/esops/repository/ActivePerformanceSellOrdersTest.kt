package com.esops.repository

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import com.esops.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.math.BigInteger

class ActivePerformanceSellOrdersTest {
    private val activePerformanceSellOrders = ActivePerformanceSellOrders()
    private val dummyUser = User("", "", "", "", "")
    @AfterEach
    fun tearDown() {
        activePerformanceSellOrders.clear()
    }

    @Test
    fun `should get null if none of the sell orders are viable`() {
        val highPriceOrder =
            Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(highPriceOrder)
        val lowPriceOrder = Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(lowPriceOrder)
        val buyOrder = Order(dummyUser, OrderType.BUY, BigInteger.ONE, BigInteger("2"), EsopType.NON_PERFORMANCE)

        val bestSellOrder = activePerformanceSellOrders.getBestSellOrder(buyOrder)
        Assertions.assertNull(bestSellOrder)
    }

    @Test
    fun `should get sell order that comes first even if later ones have better price`() {
        val firstOrder = Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(secondOrder)
        val buyOrder = Order(dummyUser, OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)

        val bestSellOrder = activePerformanceSellOrders.getBestSellOrder(buyOrder)

        Assertions.assertNotNull(bestSellOrder)
        Assertions.assertEquals(firstOrder, bestSellOrder)
    }

    @Test
    fun `should be able to remove an order`() {
        val firstOrder = Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(secondOrder)
        val thirdOrder = Order(dummyUser, OrderType.SELL, BigInteger.ONE, BigInteger("2"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(thirdOrder)
        val buyOrderThatMatchesWithEverything =
            Order(dummyUser, OrderType.BUY, BigInteger.ONE, BigInteger.TEN, EsopType.NON_PERFORMANCE)

        val orderToBeDeleted = secondOrder
        activePerformanceSellOrders.removeOrderIfExists(orderToBeDeleted)

        while (true) {
            val currentOrder = activePerformanceSellOrders.getBestSellOrder(buyOrderThatMatchesWithEverything) ?: break
            assertNotEquals(currentOrder, orderToBeDeleted)
            activePerformanceSellOrders.removeOrderIfExists(currentOrder)
        }
    }
}