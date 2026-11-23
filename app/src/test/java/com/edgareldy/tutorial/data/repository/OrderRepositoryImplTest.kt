package com.edgareldy.tutorial.data.repository

import com.edgareldy.tutorial.data.local.dao.OrderDao
import com.edgareldy.tutorial.data.local.entity.OrderEntity
import com.edgareldy.tutorial.data.remote.api.OrderApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
 * Integration tests for OrderRepositoryImpl using MockWebServer and MockK.
 * Exercises the HTTP-to-domain mapping for createOrder (201/401/500) and
 * verifies that getOrders emits cached entities from Room. The OrderDao
 * is mocked with MockK to avoid a full Room setup, consistent with the
 * CartRepositoryImplTest approach.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var orderApiService: OrderApiService
    private lateinit var mockOrderDao: OrderDao
    private lateinit var repository: OrderRepositoryImpl

    private val testDispatcher = UnconfinedTestDispatcher()

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

        orderApiService = retrofit.create(OrderApiService::class.java)
        mockOrderDao = mockk()
        repository = OrderRepositoryImpl(
            orderApiService = orderApiService,
            orderDao = mockOrderDao,
            scope = CoroutineScope(testDispatcher)
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // --- JSON helpers ---

    private fun orderResponseBody(
        id: Long = 1L,
        orderNumber: String = "ORD-001",
        status: String = "PENDING",
        total: Double = 49.99,
        itemsJson: String = "[]"
    ) = """
        {
            "success": true,
            "message": "OK",
            "data": {
                "id": $id,
                "order_number": "$orderNumber",
                "status": "$status",
                "total_amount": $total,
                "items": $itemsJson
            },
            "timestamp": "2026-11-23T10:00:00"
        }
    """.trimIndent()

    private fun ordersPageResponseBody(
        orders: String = """[{"id":1,"order_number":"ORD-001","status":"PENDING","total_amount":49.99,"items":[]}]"""
    ) = """
        {
            "success": true,
            "message": "OK",
            "data": {
                "content": $orders,
                "page": 0,
                "size": 50,
                "totalElements": 1,
                "totalPages": 1
            },
            "timestamp": "2026-11-23T10:00:00"
        }
    """.trimIndent()

    // --- Tests ---

    @Test
    fun _01_ShouldEmitCachedOrders_WhenDaoHasData() = runTest(testDispatcher) {
        // Given: DAO has one cached entity, network refresh returns 200
        val fakeEntity = OrderEntity(
            id = 1L,
            orderNumber = "ORD-001",
            status = "PENDING",
            totalAmount = 49.99
        )
        every { mockOrderDao.getOrders() } returns flowOf(listOf(fakeEntity))
        coEvery { mockOrderDao.deleteAll() } just Runs
        coEvery { mockOrderDao.upsertAll(any()) } just Runs
        coEvery { mockOrderDao.upsertItems(any()) } just Runs

        // Enqueue a response so the background refresh does not fail
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(ordersPageResponseBody())
        )

        // When: collect the first emission from the flow
        var emission: Result<List<com.edgareldy.tutorial.domain.model.Order>>? = null
        repository.getOrders().take(1).collect { emission = it }

        // Then: the cached entity is mapped to a domain object
        assertTrue(emission!!.isSuccess)
        val orders = emission!!.getOrNull()!!
        assertEquals(1, orders.size)
        assertEquals(1L, orders[0].id)
        assertEquals("ORD-001", orders[0].orderNumber)
    }

    @Test
    fun _02_ShouldReturnSuccess_WhenCreateOrderResponseIs201() = runTest(testDispatcher) {
        // Given: server returns 201 with an order body
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(orderResponseBody(id = 42L, orderNumber = "ORD-042", status = "CONFIRMED"))
        )
        coEvery { mockOrderDao.upsertAll(any()) } just Runs
        coEvery { mockOrderDao.deleteItemsByOrderId(any()) } just Runs
        coEvery { mockOrderDao.upsertItems(any()) } just Runs

        // When
        val result = repository.createOrder()

        // Then
        assertTrue(result.isSuccess)
        val order = result.getOrNull()!!
        assertEquals(42L, order.id)
        assertEquals("ORD-042", order.orderNumber)
    }

    @Test
    fun _03_ShouldReturnFailure_WhenCreateOrderResponseIs401() = runTest(testDispatcher) {
        // Given: server returns 401 Unauthorized
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // When
        val result = repository.createOrder()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun _04_ShouldReturnFailure_WhenCreateOrderResponseIs500() = runTest(testDispatcher) {
        // Given: server returns 500 Internal Server Error
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        // When
        val result = repository.createOrder()

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun _05_ShouldVerifyDaoUpsertCalled_WhenCreateOrderSucceeds() = runTest(testDispatcher) {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .addHeader("Content-Type", "application/json")
                .setBody(orderResponseBody(id = 7L))
        )
        coEvery { mockOrderDao.upsertAll(any()) } just Runs
        coEvery { mockOrderDao.upsertItems(any()) } just Runs

        // When
        repository.createOrder()

        // Then: order header is upserted into Room after a successful checkout
        coVerify(exactly = 1) { mockOrderDao.upsertAll(any()) }
    }
}
