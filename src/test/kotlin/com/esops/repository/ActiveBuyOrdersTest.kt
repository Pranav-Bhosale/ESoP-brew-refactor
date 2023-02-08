package com.esops.repository

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.math.BigInteger

@MicronautTest
class ActiveBuyOrdersTest {
    @Inject
    lateinit var activeBuyOrders: ActiveBuyOrders

    @AfterEach
    fun tearDown() {
        activeBuyOrders.clear()
    }

    @Test
    fun `should be able to add buy order`() {
        val order = Order("1", "jake", OrderType.BUY, BigInteger.ONE, BigInteger.ONE, EsopType.NON_PERFORMANCE)
        activeBuyOrders.addOrder(order)
        val bestBuyOrder = activeBuyOrders.getBestBuyOrder()

        assertNotNull(bestBuyOrder)
        assertEquals(order, bestBuyOrder)
    }

    @Test
    fun `should get buy order with highest order price`() {
        val lowPriceOrder = Order("1", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("5"), EsopType.NON_PERFORMANCE)
        activeBuyOrders.addOrder(lowPriceOrder)
        val highPriceOrder =
            Order("2", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeBuyOrders.addOrder(highPriceOrder)

        val bestBuyOrder = activeBuyOrders.getBestBuyOrder()

        assertNotNull(bestBuyOrder)
        assertEquals(highPriceOrder, bestBuyOrder)
    }

    @Test
    fun `should get buy order that came first if same price`() {
        val firstOrder = Order("1", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("10"), EsopType.NON_PERFORMANCE)
        activeBuyOrders.addOrder(firstOrder)
        val secondOrder = Order("2", "jake", OrderType.BUY, BigInteger.ONE, BigInteger("5"), EsopType.NON_PERFORMANCE)
        activeBuyOrders.addOrder(secondOrder)

        val bestBuyOrder = activeBuyOrders.getBestBuyOrder()

        assertNotNull(bestBuyOrder)
        assertEquals(firstOrder, bestBuyOrder)
    }
}