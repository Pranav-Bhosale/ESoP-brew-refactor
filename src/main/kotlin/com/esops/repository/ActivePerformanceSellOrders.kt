package com.esops.repository

import com.esops.entity.Order
import jakarta.inject.Singleton
import java.util.*

@Singleton
class ActivePerformanceSellOrders {
    private var sellOrderQueue = LinkedList<Order>()
    fun addOrder(order: Order) {
        sellOrderQueue.add(order)
    }

    fun getBestSellOrder(buyOrder: Order): Order? {
        for (sellOrder in sellOrderQueue) {
            if (buyOrder.price >= sellOrder.price) return sellOrder
        }
        return null
    }

    fun clear() {
        sellOrderQueue.clear()
    }

    fun removeOrderIfExists(order: Order) {
        val sellOrders = sellOrderQueue.iterator()
        while (sellOrders.hasNext()) {
            val currentSellOrder = sellOrders.next()
            if (order.orderId == currentSellOrder.orderId) {
                sellOrders.remove()
                return
            }
        }
    }
}