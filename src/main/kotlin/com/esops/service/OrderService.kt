package com.esops.service

import com.esops.configuration.PlatformFeesConfiguration
import com.esops.entity.*
import com.esops.model.AddOrderRequestBody
import com.esops.repository.ActiveBuyOrders
import com.esops.repository.ActiveNonPerformanceSellOrders
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
    private lateinit var activeNonPerformanceSellOrders : ActiveNonPerformanceSellOrders

    private var orderIDCounter: Long = 0

    fun placeOrder(username: String, addOrderRequestBody: AddOrderRequestBody): Order {
        orderIDCounter++
        val user = this.userService.getUser(username)
        return when (OrderType.valueOf(addOrderRequestBody.type!!)) {
            OrderType.BUY -> placeBuyOrder(addOrderRequestBody, user)
            OrderType.SELL -> placeSellOrder(addOrderRequestBody, user)
        }
    }

    private fun placeBuyOrder(
        addOrderRequestBody: AddOrderRequestBody,
        user: User
    ): Order {
        val username = user.userName
        val price = BigInteger(addOrderRequestBody.price!!)
        val quantity = BigInteger(addOrderRequestBody.quantity!!)
        val orderValue = price.multiply(quantity)
        val order = Order(
            orderIDCounter.toString(),
            username,
            OrderType.BUY,
            quantity,
            price,
            EsopType.NON_PERFORMANCE,
            remainingQuantity = quantity
        )
        userService.getUser(username).addNewOrder(order)
        buyOrderQueue.addOrder(order)
        user.moveWalletMoneyFromFreeToLockedState(orderValue)
        executeBuyOrder(order)
        return order
    }

    private fun executeBuyOrder(sellOrder: Order) {
        val sellOrderUser = userService.getUser(sellOrder.username)

        while(sellOrder.remainingQuantity > BigInteger.ZERO) {
            val bestBuyOrder = buyOrderQueue.getBestBuyOrder() ?: return
            val buyOrderUser = userService.getUser(bestBuyOrder.username)
            applyOrderMatchingAlgorithm(bestBuyOrder, sellOrder, buyOrderUser, sellOrderUser)
        }
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

    private fun placeSellOrder(
        addOrderRequestBody: AddOrderRequestBody,
        user: User
    ): Order {
        val username = user.userName
        val esopType = EsopType.valueOf(addOrderRequestBody.esopType!!)
        val price = BigInteger(addOrderRequestBody.price!!)
        val quantity = BigInteger(addOrderRequestBody.quantity!!)
        val order = Order(
            orderIDCounter.toString(),
            username,
            OrderType.SELL,
            quantity,
            price,
            esopType,
            remainingQuantity = quantity
        )
        userService.getUser(username).addNewOrder(order)
        activeNonPerformanceSellOrders.addOrder(order)
        user.moveInventoryFromFreeToLockedState(esopType, quantity)
        executeSellOrder(order)
        return order
    }

    private fun executeSellOrder(sellOrder: Order) {
        val sellOrderUser = userService.getUser(sellOrder.username)

        while(sellOrder.remainingQuantity > BigInteger.ZERO) {
            val bestBuyOrder = buyOrderQueue.getBestBuyOrder() ?: return
            val buyOrderUser = userService.getUser(bestBuyOrder.username)
            applyOrderMatchingAlgorithm(bestBuyOrder, sellOrder, buyOrderUser, sellOrderUser)
        }
    }


    private fun applyOrderMatchingAlgorithm(
        buyOrder: Order,
        sellOrder: Order,
        buyOrderUser: User,
        sellOrderUser: User
    ) {
        if (buyOrder.price >= sellOrder.price) {
            val minPrice = sellOrder.price
            val minQuantity = if (buyOrder.remainingQuantity < sellOrder.remainingQuantity) buyOrder.remainingQuantity else sellOrder.remainingQuantity
            if(minQuantity <= BigInteger.ZERO) return
            updateFilledFieldDuringMatching(sellOrder, buyOrder, minQuantity, minPrice)
            updateRemainingQuantityInOrderDuringMatching(sellOrder, minQuantity, buyOrder)
            val buyOrderValue = buyOrder.price.multiply(minQuantity)
            val sellOrderValue = sellOrder.price.multiply(minQuantity)
            buyOrderUser.wallet.free = buyOrderUser.wallet.free.add(buyOrderValue.subtract(sellOrderValue))
            buyOrderUser.wallet.locked = buyOrderUser.wallet.locked.subtract(buyOrderValue)

            buyOrderUser.normal.free = buyOrderUser.normal.free.add(minQuantity)
            when (sellOrder.esopType) {
                EsopType.PERFORMANCE -> {
                    sellOrderUser.performance.locked = sellOrderUser.performance.locked.subtract(minQuantity)
                    val platformFees: BigInteger =
                        sellOrderValue.multiply(
                            BigInteger(
                                (platformFeesConfiguration.performance * 100).toInt().toString()
                            )
                        )
                            .divide(BigInteger("10000"))
                    sellOrderUser.wallet.free = sellOrderUser.wallet.free.add(sellOrderValue).subtract(platformFees)
                    platformService.addPlatformFees(platformFees)
                }

                EsopType.NON_PERFORMANCE -> {
                    sellOrderUser.normal.locked =
                        sellOrderUser.normal.locked.subtract(minQuantity)
                    val platformFees: BigInteger =
                        sellOrderValue.multiply(BigInteger((platformFeesConfiguration.normal * 100).toInt().toString()))
                            .divide(BigInteger("10000"))
                    sellOrderUser.wallet.free = sellOrderUser.wallet.free.add(sellOrderValue).subtract(platformFees)
                    platformService.addPlatformFees(platformFees)
                }
            }
        }
    }

    fun orderHistory(username: String): List<Order> {
        userService.testUser(username)
        return userService.getUser(username).getAllOrders()
    }

    fun clearOrders() {
        orderIDCounter = 0
        while (true)
            buyOrderQueue.getBestBuyOrder() ?: break
        sellOrderQueue.clear()
    }
}
