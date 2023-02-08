package com.esops.repository

import com.esops.entity.BuyOrderComparator
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
        return buyOrderQueue.poll()
    }
}
