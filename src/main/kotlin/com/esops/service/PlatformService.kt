package com.esops.service

import com.esops.entity.EsopType
import com.esops.entity.Order
import com.esops.entity.OrderType
import com.esops.entity.Platform
import com.esops.repository.ActiveBuyOrders
import com.esops.repository.ActiveNonPerformanceSellOrders
import com.esops.repository.ActivePerformanceSellOrders
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.aspectj.weaver.ast.Or
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

    fun addOrderToQueue(order: Order){
        when(order.type){
            OrderType.BUY -> addBuyOrder(order)
            OrderType.SELL -> addSellOrder(order)
        }
    }

    private fun addSellOrder(order: Order) {
        when(order.esopType){
            EsopType.PERFORMANCE -> addPerformanceSellOrder(order)
            EsopType.NON_PERFORMANCE -> addNonPerformanceSellOrder(order)
        }
    }

    private fun addPerformanceSellOrder(order: Order) = activePerformanceSellOrders.addOrder(order)


    private fun addNonPerformanceSellOrder(order: Order) = activeNonPerformanceSellOrders.addOrder(order)

    private fun addBuyOrder(order: Order) = activeBuyOrders.addOrder(order)
}
