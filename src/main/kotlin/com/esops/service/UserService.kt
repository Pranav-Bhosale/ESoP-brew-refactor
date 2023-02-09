package com.esops.service

import com.esops.entity.EsopType
import com.esops.entity.FormattedUser
import com.esops.entity.User
import com.esops.exception.UserNotFoundException
import com.esops.exception.UserNotUniqueException
import com.esops.model.*
import jakarta.inject.Singleton
import java.math.BigInteger

@Singleton
class UserService {

    private var users = HashMap<String, User>()

    fun addUser(userRegistrationRequestBody: UserRegistrationRequestBody): UserRegistrationResponseBody {
        checkUniqueness(userRegistrationRequestBody)
        val firstName = userRegistrationRequestBody.firstName
        val lastName = userRegistrationRequestBody.lastName
        val userName = userRegistrationRequestBody.userName
        val phoneNumber = userRegistrationRequestBody.phoneNumber
        val email = userRegistrationRequestBody.email
        users[userName!!] = User(firstName!!, lastName!!, userName, email!!, phoneNumber!!)
        return UserRegistrationResponseBody(userRegistrationRequestBody)
    }

    fun clearUsers() {
        users = HashMap()
    }

    fun getUser(username: String): User {
        return users[username]!!
    }

    private fun checkUniqueness(userRegistrationRequestBody: UserRegistrationRequestBody) {
        val userName = userRegistrationRequestBody.userName
        val phoneNumber = userRegistrationRequestBody.phoneNumber
        val email = userRegistrationRequestBody.email
        val error = mutableListOf<String>()
        if (users.containsKey(userName)) {
            error.add("userName already exists")
        }
        for ((_, user) in users) {
            if (user.email == email) {
                error.add("email already exists")
            }
        }
        for ((_, user) in users) {
            if (user.phoneNumber == phoneNumber) {
                error.add("phoneNumber already exists")
            }
        }
        if (error.isNotEmpty()) throw UserNotUniqueException(error)
    }

    fun testUser(username: String) {
        if (users.containsKey(username)) {
            return
        }
        throw UserNotFoundException(listOf("user does not exists"))
    }

    fun accountInformation(username: String): FormattedUser {
        testUser(username)
        return users[username]!!.getFormatterUserData()
    }

    fun addInventory(username: String, addInventoryRequestBody: AddInventoryRequestBody): AddInventoryResponseBody {
        val user = getUser(username)
        val esopType = EsopType.valueOf(addInventoryRequestBody.type)
        val quantity = BigInteger(addInventoryRequestBody.quantity!!)
        if (esopType == EsopType.NON_PERFORMANCE)
            user.addNonPerformanceEsops(quantity)
        else
            user.addPerformanceEsops(quantity)
        return AddInventoryResponseBody("${addInventoryRequestBody.quantity} ${addInventoryRequestBody.type} ESOPs added to your account")
    }

    fun addWalletMoney(
        username: String,
        addWalletMoneyRequestBody: AddWalletMoneyRequestBody
    ): AddWalletMoneyResponseBody {
        val user = getUser(username)
        val amount = BigInteger(addWalletMoneyRequestBody.amount!!)
        user.addMoneyToWallet(amount)
        return AddWalletMoneyResponseBody("${addWalletMoneyRequestBody.amount} amount added to your account")
    }

}
