package com.esops.repository

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class ActivePerformanceSellOrdersTest{
    @Inject
    lateinit var activePerformanceSellOrders: ActivePerformanceSellOrders

    @AfterEach
    fun tearDown() {
        activePerformanceSellOrders.clear()
    }

    @Test
    fun `should get null if none of the sell orders are viable`() {
        val highPriceOrder =
            Order("1", "jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(highPriceOrder)
        val lowPriceOrder = Order("2", "jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(lowPriceOrder)
        val buyOrder = Order("3", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("2"), EsopType.NON_PERFORMANCE)

        val bestSellOrder = activePerformanceSellOrders.getBestSellOrder(buyOrder)
        Assertions.assertNull(bestSellOrder)
    }

    @Test
    fun `should get sell order that comes first even if later ones have better price`() {
        val firstOrder = Order("1", "jake", OrderType.SELL, BigInteger.ONE, BigInteger("10"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order("2", "jake", OrderType.SELL, BigInteger.ONE, BigInteger("5"), EsopType.PERFORMANCE)
        activePerformanceSellOrders.addOrder(secondOrder)
        val buyOrder = Order("3", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)

        val bestSellOrder = activePerformanceSellOrders.getBestSellOrder(buyOrder)

        Assertions.assertNotNull(bestSellOrder)
        Assertions.assertEquals(firstOrder, bestSellOrder)
    }
}