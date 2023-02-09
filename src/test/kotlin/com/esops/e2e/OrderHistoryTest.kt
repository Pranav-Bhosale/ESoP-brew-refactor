package com.esops.e2e

import com.esops.repository.ActiveBuyOrders
import com.esops.repository.ActiveNonPerformanceSellOrders
import com.esops.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class OrderHistoryTest {
    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var buyOrders: ActiveBuyOrders

    @Inject
    lateinit var nonPerformanceSellOrders: ActiveNonPerformanceSellOrders

    @BeforeEach
    fun `clear user`() {
        userService.clearUsers()
    }

    @BeforeEach
    fun `clear order`() {
        buyOrders.clear()
        nonPerformanceSellOrders.clear()
    }

    @Test
    fun `Should throw error if user does not exist`(specification: RequestSpecification) {
        specification
            .given()
            .`when`()
            .contentType(ContentType.JSON)
            .pathParam("userName", "yash")
            .get("/user/{userName}/order")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
            .body("error", hasItem("user does not exists"))
    }
}