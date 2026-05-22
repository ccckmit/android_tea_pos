package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.OrderRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PosViewModel(private val repository: OrderRepository) : ViewModel() {

    // Current Tab: 0 for POS (點餐), 1 for History (銷貨歷史), 2 for Analytics (營收圖表)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Current menu category filtering
    private val _selectedCategory = MutableStateFlow("經典奶茶")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Active shopping cart
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // The item being customized right now (shown in BottomSheet/Dialog)
    private val _customizingItem = MutableStateFlow<CartItem?>(null)
    val customizingItem: StateFlow<CartItem?> = _customizingItem.asStateFlow()

    // Active checkout outcome for receipt display
    private val _lastCheckoutOrder = MutableStateFlow<OrderWithItems?>(null)
    val lastCheckoutOrder: StateFlow<OrderWithItems?> = _lastCheckoutOrder.asStateFlow()

    // View state for showing the receipt dialog
    private val _showCheckoutReceipt = MutableStateFlow(false)
    val showCheckoutReceipt: StateFlow<Boolean> = _showCheckoutReceipt.asStateFlow()

    // Cash received input box text
    val checkOutCashAmount = MutableStateFlow("")

    // Selected payment method
    private val _paymentMethod = MutableStateFlow("現金")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    // Menu search bar text
    private val _menuSearchQuery = MutableStateFlow("")
    val menuSearchQuery: StateFlow<String> = _menuSearchQuery.asStateFlow()

    // History and receipt filter query
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    // Flow of historical orders from Room DB
    val ordersWithItems: StateFlow<List<OrderWithItems>> = repository.allOrdersWithItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun changeTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _menuSearchQuery.value = query
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun startCustomizing(drink: Drink) {
        _customizingItem.value = CartItem(
            drink = drink,
            size = "大杯",
            sweetness = "半糖",
            ice = "微冰",
            toppings = emptyList(),
            quantity = 1
        )
    }

    fun cancelCustomization() {
        _customizingItem.value = null
    }

    // Functional update helper to modify individual properties in ongoing customization state
    fun updateCustomizingItem(update: (CartItem) -> CartItem) {
        _customizingItem.value = _customizingItem.value?.let { update(it) }
    }

    // Multi-select/toggle topping helper
    fun toggleTopping(topping: String) {
        updateCustomizingItem { item ->
            val list = item.toppings.toMutableList()
            if (list.contains(topping)) {
                list.remove(topping)
            } else {
                list.add(topping)
            }
            item.copy(toppings = list)
        }
    }

    // Add final item options to cart
    fun commitToCart() {
        _customizingItem.value?.let { customItem ->
            val existingIndex = _cart.value.indexOfFirst {
                it.drink.id == customItem.drink.id &&
                it.size == customItem.size &&
                it.sweetness == customItem.sweetness &&
                it.ice == customItem.ice &&
                it.toppings.sorted() == customItem.toppings.sorted()
            }

            if (existingIndex != -1) {
                // Update quantity if perfect match
                _cart.value = _cart.value.mapIndexed { idx, item ->
                    if (idx == existingIndex) {
                        item.copy(quantity = item.quantity + customItem.quantity)
                    } else item
                }
            } else {
                // Append new customize parameters configuration
                _cart.value = _cart.value + customItem
            }
            _customizingItem.value = null
        }
    }

    fun increaseCartItemQuantity(itemId: String) {
        _cart.value = _cart.value.map {
            if (it.id == itemId) it.copy(quantity = it.quantity + 1) else it
        }
    }

    fun decreaseCartItemQuantity(itemId: String) {
        _cart.value = _cart.value.mapNotNull {
            if (it.id == itemId) {
                if (it.quantity > 1) it.copy(quantity = it.quantity - 1) else null
            } else it
        }
    }

    fun removeCartItem(itemId: String) {
        _cart.value = _cart.value.filter { it.id != itemId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }

    // Save transaction state as complete in SQL local schema
    fun performCheckout() {
        val cartItems = _cart.value
        if (cartItems.isEmpty()) return

        val total = cartItems.sumOf { it.totalPrice }
        val method = _paymentMethod.value
        val cashInput = checkOutCashAmount.value.toDoubleOrNull() ?: total

        val received = if (method == "現金") cashInput.coerceAtLeast(total) else total
        val change = if (method == "現金") (received - total).coerceAtLeast(0.0) else 0.0

        val order = OrderEntity(
            totalPrice = total,
            paymentMethod = method,
            receivedAmount = received,
            changeAmount = change
        )

        val itemsToSave = cartItems.map { c ->
            OrderItemEntity(
                orderId = 0, // Setup by insertion inside repository
                drinkName = c.drink.name,
                category = c.drink.category,
                size = c.size,
                sweetness = c.sweetness,
                ice = c.ice,
                toppings = if (c.toppings.isEmpty()) "無加料" else c.toppings.joinToString(", "),
                unitPrice = c.singlePrice,
                quantity = c.quantity
            )
        }

        viewModelScope.launch {
            val orderId = repository.saveOrder(order, itemsToSave)
            val savedOrder = order.copy(id = orderId.toInt())
            _lastCheckoutOrder.value = OrderWithItems(order = savedOrder, items = itemsToSave)
            _showCheckoutReceipt.value = true

            // Reset checkout state
            _cart.value = emptyList()
            checkOutCashAmount.value = ""
        }
    }

    fun closeReceiptDialog() {
        _showCheckoutReceipt.value = false
        _lastCheckoutOrder.value = null
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteOrderById(orderId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Helper to generate some sample historical data if the user wants starting figures
    fun injectSampleHistory() {
        viewModelScope.launch {
            repository.clearAll()
            val sampleOrders = listOf(
                Pair(
                    OrderEntity(totalPrice = 160.0, paymentMethod = "現金", receivedAmount = 200.0, changeAmount = 40.0, timestamp = System.currentTimeMillis() - 86400000 * 2),
                    listOf(
                        OrderItemEntity(orderId = 0, drinkName = "珍珠奶茶", category = "經典奶茶", size = "大杯", sweetness = "半糖", ice = "微冰", toppings = "珍珠", unitPrice = 60.0, quantity = 2),
                        OrderItemEntity(orderId = 0, drinkName = "翡翠茉莉綠茶", category = "特調原茶", size = "中杯", sweetness = "無糖", ice = "去冰", toppings = "無加料", unitPrice = 35.0, quantity = 1),
                        OrderItemEntity(orderId = 0, drinkName = "經典雙響炮", category = "經典奶茶", size = "中杯", sweetness = "半糖", ice = "少冰", toppings = "珍珠, 椰果", unitPrice = 65.0, quantity = 1)
                    )
                ),
                Pair(
                    OrderEntity(totalPrice = 125.0, paymentMethod = "Line Pay", receivedAmount = 125.0, changeAmount = 0.0, timestamp = System.currentTimeMillis() - 86400000),
                    listOf(
                        OrderItemEntity(orderId = 0, drinkName = "冰釀鮮橙綠", category = "鮮果茶", size = "大杯", sweetness = "微糖", ice = "微冰", toppings = "蘆薈", unitPrice = 75.0, quantity = 1),
                        OrderItemEntity(orderId = 0, drinkName = "多多綠茶", category = "特調原茶", size = "大杯", sweetness = "半糖", ice = "去冰", toppings = "無加料", unitPrice = 50.0, quantity = 1)
                    )
                ),
                Pair(
                    OrderEntity(totalPrice = 280.0, paymentMethod = "信用卡", receivedAmount = 280.0, changeAmount = 0.0, timestamp = System.currentTimeMillis() - 3600000 * 3),
                    listOf(
                        OrderItemEntity(orderId = 0, drinkName = "經典芋頭鮮奶", category = "鮮奶茶/拿鐵", size = "大杯", sweetness = "微糖", ice = "溫熱", toppings = "西米露", unitPrice = 80.0, quantity = 2),
                        OrderItemEntity(orderId = 0, drinkName = "鐵觀音拿鐵", category = "鮮奶茶/拿鐵", size = "大杯", sweetness = "半糖", ice = "微冰", toppings = "珍珠", unitPrice = 75.0, quantity = 1),
                        OrderItemEntity(orderId = 0, drinkName = "藍莓奶蓋茶", category = "特調原茶", size = "大杯", sweetness = "少糖", ice = "正常冰", toppings = "無加料", unitPrice = 45.0, quantity = 1)
                    )
                ),
                Pair(
                    OrderEntity(totalPrice = 110.0, paymentMethod = "街口支付", receivedAmount = 110.0, changeAmount = 0.0, timestamp = System.currentTimeMillis() - 1800000),
                    listOf(
                        OrderItemEntity(orderId = 0, drinkName = "珍珠奶茶", category = "經典奶茶", size = "大杯", sweetness = "半糖", ice = "微冰", toppings = "珍珠", unitPrice = 60.0, quantity = 1),
                        OrderItemEntity(orderId = 0, drinkName = "布丁凍奶茶", category = "經典奶茶", size = "中杯", sweetness = "半糖", ice = "微冰", toppings = "布丁", unitPrice = 50.0, quantity = 1)
                    )
                )
            )

            for (sample in sampleOrders) {
                repository.saveOrder(sample.first, sample.second)
            }
        }
    }

    class Factory(private val repository: OrderRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PosViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
