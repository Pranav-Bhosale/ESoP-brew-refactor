package com.esops.service

import com.esops.entity.Platform
import com.esops.repository.ActiveBuyOrders
import com.esops.repository.ActiveNonPerformanceSellOrders
import com.esops.repository.ActivePerformanceSellOrders
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigInteger

@Inject
lateinit var activeBuyOrders: ActiveBuyOrders

@Inject
lateinit var activePerformanceSellOrders: ActivePerformanceSellOrders

@Inject
lateinit var activeNonPerformanceSellOrders: ActiveNonPerformanceSellOrders

@Inject
lateinit var orderService: OrderService

@Singleton
class PlatformService {
    private val platform = Platform()
    fun getCollectedPlatformFee(): Map<String, BigInteger> {
        return mapOf("feesCollected" to platform.getTotalFessCollected())
    }

    fun addPlatformFees(fee: BigInteger) {
        platform.add(fee)
    }

    fun executeOrders() {
        val buyOrder = activeBuyOrders.getBestBuyOrder() ?: return
        val sellOrder =
            activePerformanceSellOrders.getBestSellOrder(buyOrder) ?: activeNonPerformanceSellOrders.getBestSellOrder()
            ?: return
        orderService.executeOrder(buyOrder, sellOrder)
    }
}
