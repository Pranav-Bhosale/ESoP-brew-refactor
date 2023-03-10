package com.esops.controller

import com.esops.entity.FormattedUser
import com.esops.entity.Order
import com.esops.model.*
import com.esops.service.OrderService
import com.esops.service.PlatformService
import com.esops.service.UserService
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import javax.validation.Valid

@Validated
@Controller("/user")
class UserController {

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var orderService: OrderService

    @Inject
    private lateinit var platformService: PlatformService

    @Post(uri = "/{userName}/wallet")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun walletMoney(
        @Body @Valid addWalletMoneyRequestBody: AddWalletMoneyRequestBody,
        userName: String
    ): HttpResponse<AddWalletMoneyResponseBody> =
        HttpResponse.ok(this.userService.addWalletMoney(userName, addWalletMoneyRequestBody))

    @Post(uri = "/{userName}/inventory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun inventory(
        @Body @Valid addInventoryRequestBody: AddInventoryRequestBody,
        userName: String
    ): HttpResponse<AddInventoryResponseBody> =
        HttpResponse.ok(this.userService.addInventory(userName, addInventoryRequestBody))

    @Post(uri = "/{userName}/order")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun order(@Body @Valid addOrderRequestBody: AddOrderRequestBody, userName: String): HttpResponse<Any> {
        val newOrder = orderService.placeOrder(userName, addOrderRequestBody)
        platformService.executeOrders()
        return HttpResponse.ok()
    }

    @Post(uri = "/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun register(@Body @Valid userRegistrationRequestBody: UserRegistrationRequestBody): HttpResponse<UserRegistrationResponseBody> =
        HttpResponse.created(this.userService.addUser(userRegistrationRequestBody))

    @Get(uri = "/{userName}/accountInformation")
    @Produces(MediaType.APPLICATION_JSON)
    fun accountInformation(userName: String): HttpResponse<FormattedUser> =
        HttpResponse.ok(this.userService.accountInformation(userName))

    @Get(uri = "/{userName}/order")
    @Produces(MediaType.APPLICATION_JSON)
    fun orderHistory(userName: String): HttpResponse<List<Order>> =
        HttpResponse.ok(this.orderService.orderHistory(userName))
}
