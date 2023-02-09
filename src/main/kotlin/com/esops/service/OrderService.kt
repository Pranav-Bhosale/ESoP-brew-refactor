package com.esops.service

import com.esops.configuration.PlatformFeesConfiguration
import com.esops.entity.*
import com.esops.model.AddOrderRequestBody
import com.esops.repository.ActiveBuyOrders
import com.esops.repository.ActiveNonPerformanceSellOrders
import com.esops.repository.ActivePerformanceSellOrders
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigInteger

@Singleton
class OrderService {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var platformService: PlatformService

    @Inject
    lateinit var platformFeesConfiguration: PlatformFeesConfiguration

    @Inject
    private lateinit var buyOrderQueue: ActiveBuyOrders

    @Inject
    private lateinit var activeNonPerformanceSellOrders: ActiveNonPerformanceSellOrders

    @Inject
    private lateinit var activePerformanceSellOrders: ActivePerformanceSellOrders

    fun placeOrder(username: String, addOrderRequestBody: AddOrderRequestBody): Order {
        val orderType = OrderType.valueOf(addOrderRequestBody.type!!)
        val esopType = EsopType.valueOf(addOrderRequestBody.esopType!!)
        val orderQuantity = BigInteger(addOrderRequestBody.quantity!!)
        val orderPrice = BigInteger(addOrderRequestBody.price!!)
        val orderAmount = orderPrice * orderQuantity

        val user = this.userService.getUser(username)
        val newOrder = if (orderType == OrderType.BUY) placeBuyOrder(user, orderQuantity, orderPrice)
        else placeSellOrder(user, orderQuantity, orderPrice, esopType)

        user.addOrder(newOrder)
        platformService.addOrderToQueue(newOrder)

        return newOrder
    }

    private fun placeBuyOrder(user: User, orderQuantity: BigInteger, orderPrice: BigInteger): Order {
        val orderAmount = orderPrice * orderQuantity
        user.lockWalletMoney(orderAmount)
        return Order(user, OrderType.BUY, orderQuantity, orderPrice, EsopType.NON_PERFORMANCE)
    }

    private fun placeSellOrder(
        user: User,
        orderQuantity: BigInteger,
        orderPrice: BigInteger,
        esopType: EsopType
    ): Order {
        return if (esopType == EsopType.NON_PERFORMANCE) placeNonPerformanceSellOrder(user, orderQuantity, orderPrice)
        else placePerformanceSellOrder(user, orderQuantity, orderPrice)
    }

    private fun placePerformanceSellOrder(user: User, orderQuantity: BigInteger, orderPrice: BigInteger): Order {
        user.lockPerformanceEsops(orderQuantity)
        return Order(user, OrderType.SELL, orderQuantity, orderPrice, EsopType.PERFORMANCE)
    }

    private fun placeNonPerformanceSellOrder(user: User, orderQuantity: BigInteger, orderPrice: BigInteger): Order {
        user.lockNonPerformanceEsops(orderQuantity)
        return Order(user, OrderType.SELL, orderQuantity, orderPrice, EsopType.NON_PERFORMANCE)
    }


    private fun updateRemainingQuantityInOrderDuringMatching(
        sellOrder: Order,
        minQuantity: BigInteger,
        buyOrder: Order
    ) {
        sellOrder.remainingQuantity = sellOrder.remainingQuantity.subtract(minQuantity)
        buyOrder.remainingQuantity = buyOrder.remainingQuantity.subtract(minQuantity)
    }

    private fun updateFilledFieldDuringMatching(
        sellOrder: Order,
        buyOrder: Order,
        minQuantity: BigInteger,
        minPrice: BigInteger
    ) {
        sellOrder.filled.add(Filled(buyOrder.orderId, minQuantity, minPrice))
        buyOrder.filled.add(Filled(sellOrder.orderId, minQuantity, minPrice))
    }


    fun orderHistory(username: String): List<Order> {
        userService.testUser(username)
        return userService.getUser(username).getAllOrders()
    }

    fun executeOrder(buyOrder: Order, sellOrder: Order) {
        TODO("Not yet implemented")
    }
}
