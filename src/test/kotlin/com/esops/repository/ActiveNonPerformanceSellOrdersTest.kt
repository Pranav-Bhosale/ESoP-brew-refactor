package com.esops.repository

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class ActiveNonPerformanceSellOrdersTest{
    @Inject
    lateinit var activeNonPerformanceSellOrders: ActiveNonPerformanceSellOrders

    @AfterEach
    fun tearDown() {
        activeNonPerformanceSellOrders.clear()
    }

    @Test
    fun `should be able to add sell order`() {
        val order = Order("1", "jake", OrderType.SELL, BigInteger.ONE, BigInteger.ONE, EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(order)
        val bestSellOrder = activeNonPerformanceSellOrders.getBestSellOrder()

        assertNotNull(bestSellOrder)
        assertEquals(order, bestSellOrder)
    }

    @Test
    fun `should get sell order with lowest order price`() {
        val lowPriceOrder = Order("1", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("5"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(lowPriceOrder)
        val highPriceOrder =
            Order("2", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(highPriceOrder)

        val bestSellOrder = activeNonPerformanceSellOrders.getBestSellOrder()

        assertNotNull(bestSellOrder)
        assertEquals(lowPriceOrder, bestSellOrder)
    }

    @Test
    fun `should get sell order that came first if same price`() {
        val firstOrder = Order("1", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(firstOrder)
        val secondOrder = Order("2", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeNonPerformanceSellOrders.addOrder(secondOrder)

        val bestSellOrder = activeNonPerformanceSellOrders.getBestSellOrder()

        assertNotNull(bestSellOrder)
        assertEquals(firstOrder, bestSellOrder)
    }
}