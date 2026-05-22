package com.example.data.repository

import com.example.data.model.OrderDao
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

class OrderRepository(private val orderDao: OrderDao) {
    val allOrdersWithItems: Flow<List<OrderWithItems>> = orderDao.getAllOrdersWithItems()

    suspend fun saveOrder(order: OrderEntity, items: List<OrderItemEntity>): Long {
        val orderId = orderDao.insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId.toInt()) }
        orderDao.insertOrderItems(itemsWithId)
        return orderId
    }

    suspend fun deleteOrderById(id: Int) {
        orderDao.deleteOrderById(id)
    }

    suspend fun clearAll() {
        orderDao.clearAllOrders()
    }
}
