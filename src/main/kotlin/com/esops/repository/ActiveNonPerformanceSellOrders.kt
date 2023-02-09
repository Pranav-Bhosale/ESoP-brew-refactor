package com.esops.repository

import com.esops.entity.Order
import jakarta.inject.Singleton
import java.util.*

@Singleton
class ActiveNonPerformanceSellOrders {
    private var sellOrderQueue = PriorityQueue(SellOrderComparator)
    fun addOrder(order: Order) {
        sellOrderQueue.add(order)
    }

    fun getBestSellOrder(): Order? {
        return sellOrderQueue.peek()
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


object SellOrderComparator: Comparator<Order> {
    override fun compare(o1: Order, o2: Order): Int {
        val priceComparison = o1.price.compareTo(o2.price)
        val timeComparison = o1.createdAt.compareTo(o2.createdAt)
        if (priceComparison != 0) return priceComparison
        return timeComparison
    }
}