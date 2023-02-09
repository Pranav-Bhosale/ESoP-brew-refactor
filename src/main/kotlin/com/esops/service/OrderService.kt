package com.esops.service

import com.esops.entity.*
import com.esops.model.AddOrderRequestBody
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigInteger

@Singleton
class OrderService {

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var platformService: PlatformService

    fun placeOrder(username: String, addOrderRequestBody: AddOrderRequestBody): Order {
        val orderType = OrderType.valueOf(addOrderRequestBody.type!!)
        val esopType = EsopType.valueOf(addOrderRequestBody.esopType!!)
        val orderQuantity = BigInteger(addOrderRequestBody.quantity!!)
        val orderPrice = BigInteger(addOrderRequestBody.price!!)

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
        buyOrder: Order,
        sellOrder: Order
    ) {
        val orderQuantity = getOrderQuantity(buyOrder, sellOrder)
        sellOrder.remainingQuantity = sellOrder.remainingQuantity.subtract(orderQuantity)
        buyOrder.remainingQuantity = buyOrder.remainingQuantity.subtract(orderQuantity)
    }

    private fun updateFilledFieldDuringMatching(
        sellOrder: Order,
        buyOrder: Order
    ) {
        val orderQuantity = getOrderQuantity(buyOrder, sellOrder)
        val orderPrice = sellOrder.price
        sellOrder.filled.add(Filled(buyOrder.orderId, orderQuantity, orderPrice))
        buyOrder.filled.add(Filled(sellOrder.orderId, orderQuantity, orderPrice))
    }


    fun orderHistory(username: String): List<Order> {
        userService.testUser(username)
        return userService.getUser(username).getAllOrders()
    }

    fun executeOrder(buyOrder: Order, sellOrder: Order) {
        if (buyOrder.price < sellOrder.price) return

        updateWallets(buyOrder, sellOrder)
        updateInventories(buyOrder, sellOrder)
        updateFilledFieldDuringMatching(sellOrder, buyOrder)
        updateRemainingQuantityInOrderDuringMatching(buyOrder, sellOrder)
    }

    private fun updateInventories(buyOrder: Order, sellOrder: Order) {
        val buyer = buyOrder.createdBy
        val seller = sellOrder.createdBy
        val orderQuantity = getOrderQuantity(buyOrder, sellOrder)
        buyer.addNonPerformanceEsops(orderQuantity)
        if (sellOrder.esopType == EsopType.NON_PERFORMANCE)
            seller.removeLockedNonPerformanceEsops(orderQuantity)
        else
            seller.removeLockedPerformanceEsops(orderQuantity)
    }

    private fun updateWallets(
        buyOrder: Order,
        sellOrder: Order
    ) {
        val buyerPrice = buyOrder.price
        val sellerPrice = sellOrder.price
        val buyer = buyOrder.createdBy
        val seller = sellOrder.createdBy
        val orderQuantity = getOrderQuantity(buyOrder, sellOrder)
        val commissionFee = sellOrder.esopType.commissionFeePercentage

        updateBuyerWallet(buyer, orderQuantity, buyerPrice, sellerPrice)
        updateSellerWallet(seller, orderQuantity, sellerPrice, commissionFee)
    }

    private fun updateSellerWallet(
        seller: User,
        orderQuantity: BigInteger,
        sellerPrice: BigInteger,
        commissionFee: Int
    ) {
        seller.addMoneyToWallet(orderQuantity * sellerPrice * BigInteger.valueOf(100L - commissionFee))
    }

    private fun updateBuyerWallet(
        buyer: User,
        orderQuantity: BigInteger,
        buyerPrice: BigInteger,
        sellerPrice: BigInteger
    ) {
        buyer.removeLockedMoneyFromWallet(orderQuantity * buyerPrice)
        val priceDifferenceAmount = orderQuantity * (buyerPrice - sellerPrice)
        buyer.addMoneyToWallet(priceDifferenceAmount)
    }

    private fun getOrderQuantity(buyOrder: Order, sellOrder: Order): BigInteger {
        if (buyOrder.quantity > sellOrder.quantity) return sellOrder.quantity
        return buyOrder.quantity
    }
}