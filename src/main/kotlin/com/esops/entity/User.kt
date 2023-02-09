package com.esops.entity

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList


enum class EsopType: Comparable<EsopType> {
    NON_PERFORMANCE, PERFORMANCE
}

data class UnvestedInventory(
    var addedAt: Long = System.currentTimeMillis(),
    var dividedInventory: MutableList<BigInteger>
)

data class UnvestedInventoryResponse(
    var time: String,
    var amount: BigInteger = BigInteger("0")
)
data class User(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val phoneNumber: String,
    var wallet: Wallet = Wallet(),
    val nonPerformanceInventory: Inventory = Inventory(EsopType.NON_PERFORMANCE),
    val performanceInventory: Inventory = Inventory(EsopType.PERFORMANCE),
    var unvestedInventoryList: MutableList<UnvestedInventory> = mutableListOf(),
    private val orders: ArrayList<Order> = ArrayList()
) {
    fun getAllOrders(): List<Order> {
        return orders
    }

    fun addOrder(order: Order) = orders.add(order)

    fun lockWalletMoney(amount: BigInteger) {
        wallet.lockMoney(amount)
    }

    fun addMoneyToWallet(amount: BigInteger) {
        wallet.add(amount)
    }

    fun removeLockedMoneyFromWallet(amount: BigInteger) {
        wallet.removeLockedMoney(amount)
    }

    fun lockPerformanceEsops(quantity: BigInteger) {
        performanceInventory.lockEsops(quantity)
    }

    fun lockNonPerformanceEsops(quantity: BigInteger) {
        nonPerformanceInventory.lockEsops(quantity)
    }

    fun addPerformanceEsops(quantity: BigInteger) {
        performanceInventory.add(quantity)
    }

    fun addNonPerformanceEsops(quantity: BigInteger) {
        nonPerformanceInventory.add(quantity)
    }

    fun removeLockedNonPerformanceEsops(quantity: BigInteger) {
        nonPerformanceInventory.removeLockedEsops(quantity)
    }

    fun removeLockedPerformanceEsops(quantity: BigInteger) {
        performanceInventory.removeLockedEsops(quantity)
    }
    fun getFormatterUserData(vestingDuration: Int): FormattedUser {
        return FormattedUser(
            firstName,
            lastName,
            userName,
            email,
            phoneNumber,
            wallet,
            listOf(nonPerformanceInventory, performanceInventory),
            getUnvestedInventoryListResponse(unvestedInventoryList, vestingDuration)
        )
    }


    private fun getUnvestedInventoryListResponse(unvestedInventoryList: MutableList<UnvestedInventory>, vestingDuration: Int): MutableList<UnvestedInventoryResponse> {
        val unvestedInventoryResponseList = mutableListOf<UnvestedInventoryResponse>()
        for( inventory in unvestedInventoryList) {
            for(day in 0 until inventory.dividedInventory.size){
                if(inventory.dividedInventory[day]!= BigInteger("0")) {
                    val element = UnvestedInventoryResponse(
                        addSecsToDate(Date(inventory.addedAt), (day + 1) * vestingDuration).toString(),
                        inventory.dividedInventory[day]
                    )
                    unvestedInventoryResponseList.add(element)
                }
            }
        }
        return unvestedInventoryResponseList
    }

    private fun addSecsToDate(date: Date?, secs: Int): Date? {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.SECOND, secs)
        return calendar.time
    }
}

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class FormattedUser(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val phoneNumber: String,
    val wallet: Wallet,
    val inventory: List<Inventory>,
    val unvestedInventoryList: List<UnvestedInventoryResponse>
)
