package com.esops.repository

import com.esops.entity.Order
import jakarta.inject.Singleton
import java.util.*

@Singleton
class ActiveBuyOrders {
    private var buyOrderQueue = PriorityQueue(BuyOrderComparator)
    fun addOrder(order: Order) {
        buyOrderQueue.add(order)
    }

    fun getBestBuyOrder(): Order? {
        return buyOrderQueue.peek()
    }

    fun clear() {
        buyOrderQueue.clear()
    }

    fun removeOrderIfExists(order: Order) {
        val buyOrders = buyOrderQueue.iterator()
        while (buyOrders.hasNext()) {
            val currentBuyOrder = buyOrders.next()
            if (order.orderId == currentBuyOrder.orderId) {
                buyOrders.remove()
                return
            }
        }
    }
}


object BuyOrderComparator : Comparator<Order> {
    override fun compare(o1: Order, o2: Order): Int {
        val priceComparison = o2.price.compareTo(o1.price)
        val timeComparison = o1.createdAt.compareTo(o2.createdAt)
        if (priceComparison != 0) return priceComparison
        return timeComparison
    }
}