package com.esops.repository

import com.esops.entity.Order
import jakarta.inject.Singleton
import java.util.*
import kotlin.Comparator

@Singleton
class ActiveNonPerformanceSellOrders {
    private var sellOrderQueue = PriorityQueue(SellOrderComparator)
    fun addOrder(order: Order) {
        sellOrderQueue.add(order)
    }

    fun getBestSellOrder(): Order? {
        return sellOrderQueue.poll()
    }

    fun clear(){
        sellOrderQueue.clear()
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