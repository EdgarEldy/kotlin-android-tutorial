package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.data.remote.api.CartApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/**
 * Integration tests for CartRepositoryImpl using MockWebServer.
 * Verifies the HTTP-to-domain mapping for all cart endpoints, as well as
 * failure propagation on HTTP error codes (401, 500). The cart has no local
 * cache, so every test exercises a direct network path.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
class CartRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var cartApiService: CartApiService
    private lateinit var repository: CartRepositoryImpl

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        cartApiService = retrofit.create(CartApiService::class.java)
        repository = CartRepositoryImpl(cartApiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // --- JSON helpers ---

    private fun cartResponseBody(
        cartId: Long = 1L,
        items: String = "[]",
        total: Double = 0.0
    ) = """
        {
            "success": true,
            "message": "OK",
            "data": {
                "id": $cartId,
                "items": $items,
                "total": $total
            },
            "timestamp": "2026-11-23T10:00:00"
        }
    """.trimIndent()

    private fun cartItemJson(
        id: Long = 1L,
        productId: Long = 10L,
        productName: String = "Widget",
        unitPrice: Double = 9.99,
        quantity: Int = 2,
        subtotal: Double = 19.98
    ) = """
        {
            "id": $id,
            "product_id": $productId,
            "product_name": "$productName",
            "unit_price": $unitPrice,
            "quantity": $quantity,
            "subtotal": $subtotal
        }
    """.trimIndent()

    // --- Tests ---

    @Test
    fun _01_ShouldReturnSuccessWithCart_WhenGetCartResponseIs200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    cartResponseBody(
                        cartId = 42L,
                        items = "[${cartItemJson(id = 1L, quantity = 3, subtotal = 29.97)}]",
                        total = 29.97
                    )
                )
        )

        val result = repository.getCart()

        assertTrue(result.isSuccess)
        val cart = result.getOrNull()!!
        assertEquals(42L, cart.id)
        assertEquals(1, cart.items.size)
        assertEquals(1L, cart.items[0].id)
        assertEquals(10L, cart.items[0].productId)
        assertEquals("Widget", cart.items[0].productName)
        assertEquals(3, cart.items[0].quantity)
        assertEquals(29.97, cart.total, 0.001)
    }

    @Test
    fun _02_ShouldReturnSuccessWithUpdatedCart_WhenAddItemResponseIs200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    cartResponseBody(
                        cartId = 1L,
                        items = "[${cartItemJson(id = 2L, productId = 20L, productName = "Gadget", quantity = 1, subtotal = 9.99)}]",
                        total = 9.99
                    )
                )
        )

        val result = repository.addItem(productId = 20L, quantity = 1)

        assertTrue(result.isSuccess)
        val cart = result.getOrNull()!!
        assertEquals(1, cart.items.size)
        assertEquals(20L, cart.items[0].productId)
        assertEquals("Gadget", cart.items[0].productName)
        assertEquals(1, cart.items[0].quantity)
    }

    @Test
    fun _03_ShouldReturnSuccessWithUpdatedCart_WhenUpdateItemResponseIs200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    cartResponseBody(
                        cartId = 1L,
                        items = "[${cartItemJson(id = 1L, quantity = 5, subtotal = 49.95)}]",
                        total = 49.95
                    )
                )
        )

        val result = repository.updateItem(itemId = 1L, quantity = 5)

        assertTrue(result.isSuccess)
        val cart = result.getOrNull()!!
        assertEquals(5, cart.items[0].quantity)
        assertEquals(49.95, cart.total, 0.001)
    }

    @Test
    fun _04_ShouldReturnSuccessWithEmptyCart_WhenRemoveItemResponseIs200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(cartResponseBody(cartId = 1L, items = "[]", total = 0.0))
        )

        val result = repository.removeItem(itemId = 1L)

        assertTrue(result.isSuccess)
        val cart = result.getOrNull()!!
        assertTrue(cart.items.isEmpty())
        assertEquals(0.0, cart.total, 0.001)
    }

    @Test
    fun _05_ShouldReturnFailure_WhenGetCartResponseIs401() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val result = repository.getCart()

        assertTrue(result.isFailure)
    }

    @Test
    fun _06_ShouldReturnFailure_WhenGetCartResponseIs500() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.getCart()

        assertTrue(result.isFailure)
    }

    @Test
    fun _07_ShouldReturnFailure_WhenAddItemResponseIs401() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val result = repository.addItem(productId = 1L, quantity = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun _08_ShouldReturnSuccessWithEmptyCart_WhenClearCartResponseIs200() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(cartResponseBody(cartId = 1L, items = "[]", total = 0.0))
        )

        val result = repository.clearCart()

        assertTrue(result.isSuccess)
        val cart = result.getOrNull()!!
        assertTrue(cart.items.isEmpty())
    }

    @Test
    fun _09_ShouldReturnSuccessWithIdZero_WhenCartIdIsNull() = runTest {
        // New accounts receive {"id":null,"items":[],"total":0} before any item is added.
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                        "success": true,
                        "message": "Cart retrieved successfully",
                        "data": {"id": null, "items": [], "total": 0},
                        "timestamp": "2026-11-23T10:00:00"
                    }
                    """.trimIndent()
                )
        )

        val result = repository.getCart()

        assertTrue(result.isSuccess)
        val cart = result.getOrNull()!!
        assertEquals(0L, cart.id)
        assertTrue(cart.items.isEmpty())
        assertEquals(0.0, cart.total, 0.001)
    }
}
