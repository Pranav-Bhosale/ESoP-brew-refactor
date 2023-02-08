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
        val sellOrderIterator = sellOrderQueue.iterator()
        while (sellOrderIterator.hasNext()){
            val currentSellOrder = sellOrderIterator.next()
            if(currentSellOrder.price <= buyOrder.price){
                sellOrderIterator.remove()
                return currentSellOrder
            }
        }
        return null
    }

    fun clear(){
        sellOrderQueue.clear()
    }
}