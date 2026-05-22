@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.PosViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val customizingItem by viewModel.customizingItem.collectAsStateWithLifecycle()
    val lastCheckoutOrder by viewModel.lastCheckoutOrder.collectAsStateWithLifecycle()
    val showCheckoutReceipt by viewModel.showCheckoutReceipt.collectAsStateWithLifecycle()
    val orders by viewModel.ordersWithItems.collectAsStateWithLifecycle()

    var showActiveCartSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.changeTab(0) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "點餐") },
                    label = { Text("收銀點餐", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.changeTab(1) },
                    icon = { Icon(Icons.Default.History, contentDescription = "歷史") },
                    label = { Text("銷售歷史", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.changeTab(2) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "統計") },
                    label = { Text("營業數據", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Main Panels
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith
                        fadeOut(animationSpec = spring())
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> CashierPanel(viewModel, onExpandCart = { showActiveCartSheet = true })
                        1 -> HistoryPanel(orders, viewModel)
                        2 -> AnalyticsPanel(orders, viewModel)
                    }
                }

                // Mini Floating Cart Bar (Only show in Tab 0 when cart is not empty and sheet is closed)
                if (currentTab == 0 && cart.isNotEmpty() && !showActiveCartSheet) {
                    val totalQty = cart.sumOf { it.quantity }
                    val totalPrice = cart.sumOf { it.totalPrice }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Surface(
                            onClick = { showActiveCartSheet = true },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shadowElevation = 12.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = totalQty.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "前往結帳大廳",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "NT$ ${totalPrice.toInt()}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "結帳",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Customization Dialog
    if (customizingItem != null) {
        CustomizationDialog(
            item = customizingItem!!,
            onDismiss = { viewModel.cancelCustomization() },
            onDone = { viewModel.commitToCart() },
            viewModel = viewModel
        )
    }

    // Interactive Grocery Cart Sheet (As Dialog / Full layout for phone ergonomics)
    if (showActiveCartSheet) {
        CartCheckoutDialog(
            cart = cart,
            onDismiss = { showActiveCartSheet = false },
            viewModel = viewModel
        )
    }

    // Post-checkout change visual summary receipt popup
    if (showCheckoutReceipt && lastCheckoutOrder != null) {
        ReceiptPreviewDialog(
            order = lastCheckoutOrder!!,
            onDismiss = { viewModel.closeReceiptDialog() }
        )
    }
}

// ============================
// PANEL 1: CASHIER & SELECTIONS
// ============================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CashierPanel(
    viewModel: PosViewModel,
    onExpandCart: () -> Unit
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.menuSearchQuery.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()

    val filteredDrinks = remember(selectedCategory, searchQuery) {
        Menu.drinks.filter { drink ->
            drink.category == selectedCategory &&
            (searchQuery.isBlank() || drink.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🧋",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = "珍珠奶茶專賣",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "手機版收銀系統",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (cart.isNotEmpty()) {
                FilledIconButton(
                    onClick = onExpandCart,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(cart.sumOf { it.quantity }.toString())
                            }
                        }
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "我的購物車")
                    }
                }
            }
        }

        // Search textfield
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("搜尋飲品項目 (例如：珍珠)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "ClearIcon")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        // Menu category horizontal slide
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Menu.categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(cat) },
                    label = { Text(cat, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Grid Menu
        if (filteredDrinks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍹", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("找不到相關飲品", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDrinks, key = { it.id }) { drink ->
                    Surface(
                        onClick = { viewModel.startCustomizing(drink) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        tonalElevation = 1.dp,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(drink.iconEmoji, fontSize = 20.sp)
                                }
                                Text(
                                    text = "NT$ ${drink.basePrice.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = drink.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = drink.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                minLines = 2,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================
// COMPONENT: DRINK CUSTOMIZATION DIALOG
// ============================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizationDialog(
    item: CartItem,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    viewModel: PosViewModel
) {
    val sugars = listOf("正常糖(100%)", "少糖(70%)", "半糖(50%)", "微糖(30%)", "無糖(0%)")
    val ices = listOf("正常冰", "少冰", "微冰", "去冰", "溫熱")
    val allToppings = listOf("珍珠", "椰果", "布丁", "仙草凍", "蘆薈", "紅豆")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.drink.iconEmoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.drink.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = item.drink.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "關閉")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                // Scrollable Customization Elements
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .maxHeight(380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 1. Size Choice
                        Column {
                            Text("容量選擇", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("中杯", "大杯").forEach { s ->
                                    val isSelected = item.size == s
                                    val priceAdd = if (s == "大杯") "+$10" else "+$0"
                                    Surface(
                                        onClick = { viewModel.updateCustomizingItem { it.copy(size = s) } },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(s, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(priceAdd, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Sweetness Choice
                        Column {
                            Text("甜度糖量", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                maxItemsInEachRow = 3,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sugars.forEach { s ->
                                    val isSelected = item.sweetness == s
                                    Surface(
                                        onClick = { viewModel.updateCustomizingItem { it.copy(sweetness = s) } },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(
                                            text = s,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Ice Choice
                        Column {
                            Text("冰量調整", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                maxItemsInEachRow = 3,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ices.forEach { ic ->
                                    val isSelected = item.ice == ic
                                    Surface(
                                        onClick = { viewModel.updateCustomizingItem { it.copy(ice = ic) } },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(
                                            text = ic,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Add Toppings list
                        Column {
                            Text("加料加價區 (可多選)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                maxItemsInEachRow = 3,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allToppings.forEach { topping ->
                                    val isSelected = item.toppings.contains(topping)
                                    val cost = if (topping == "布丁") "+$15" else "+$10"
                                    Surface(
                                        onClick = { viewModel.toggleTopping(topping) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("$topping $cost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Quantity selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("購買杯數", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (item.quantity > 1) {
                                            viewModel.updateCustomizingItem { it.copy(quantity = it.quantity - 1) }
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "減少", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.updateCustomizingItem { it.copy(quantity = it.quantity + 1) } },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "增加", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                // Bottom Sum & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("客製後單杯: NT$ ${item.singlePrice.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "總計 NT$ ${item.totalPrice.toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onDone,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Done")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("加入收銀 cart", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Modifier layout max-height constraint helper
private fun Modifier.maxHeight(max: androidx.compose.ui.unit.Dp) = this.heightIn(max = max)

// ============================
// COMPONENT: ACTIVE CART & CHECKOUT DRAWER SHEET
// ============================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartCheckoutDialog(
    cart: List<CartItem>,
    onDismiss: () -> Unit,
    viewModel: PosViewModel
) {
    val totalQty = cart.sumOf { it.quantity }
    val totalPrice = cart.sumOf { it.totalPrice }
    val activeMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val cashInputText by viewModel.checkOutCashAmount.collectAsStateWithLifecycle()

    val paymentMethods = listOf("現金", "信用卡", "Line Pay", "街口支付", "Apple Pay")

    // Parse cash change details
    val cashReceived = cashInputText.toDoubleOrNull() ?: totalPrice
    val changeAmount = if (activeMethod == "現金") {
        (cashReceived - totalPrice).coerceAtLeast(0.0)
    } else 0.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                // Toolbar Header
                TopAppBar(
                    title = {
                        Text(
                            "結帳櫃檯大廳 (${totalQty} 杯飲品)",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (cart.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.clearCart()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("排空購物車", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )

                if (cart.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛒", fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("購物車空空如也", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("請返回點單大廳，點選想要客製的飲品", fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                                Text("立即去點餐", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Up: Cart Items scrollable list (Half proportion weight)
                            Column(
                                modifier = Modifier
                                    .weight(0.45f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("購買品項明細", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(cart, key = { it.id }) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(0.6f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(item.drink.iconEmoji, fontSize = 24.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        item.drink.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        "${item.size} / ${item.sweetness} / ${item.ice}" +
                                                        (if (item.toppings.isNotEmpty()) " (${item.toppings.joinToString(",")})" else ""),
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.weight(0.4f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                // Quantity selector inline
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.surface,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = { viewModel.decreaseCartItemQuantity(item.id) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    }
                                                    Text(
                                                        item.quantity.toString(),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp)
                                                    )
                                                    IconButton(
                                                        onClick = { viewModel.increaseCartItemQuantity(item.id) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    "NT$ ${item.totalPrice.toInt()}",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.widthIn(min = 54.dp),
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Down: Checkout Options Panel (Half proportion weight)
                            Column(
                                modifier = Modifier
                                    .weight(0.55f)
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                    )
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Subtotal indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("訂單應收總計", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                                    Text(
                                        "NT$ ${totalPrice.toInt()}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 26.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Payment methods horizontal selector
                                Text("選擇付款方式", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(paymentMethods) { m ->
                                        val isSel = activeMethod == m
                                        Surface(
                                            onClick = { viewModel.setPaymentMethod(m) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            contentColor = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = when (m) {
                                                        "現金" -> Icons.Default.Payments
                                                        "信用卡" -> Icons.Default.CreditCard
                                                        else -> Icons.Default.QrCodeScanner
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(m, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // If CASH is chosen: display change helper calculator
                                AnimatedVisibility(visible = activeMethod == "現金") {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Cash received input display
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = cashInputText,
                                                onValueChange = { s ->
                                                    // filter to digits only
                                                    if (s.isEmpty() || s.all { it.isDigit() }) {
                                                        viewModel.checkOutCashAmount.value = s
                                                    }
                                                },
                                                label = { Text("實收現金金額 (NT$)") },
                                                placeholder = { Text(totalPrice.toInt().toString()) },
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                                                ),
                                                singleLine = true
                                            )

                                            // Change returned display
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                modifier = Modifier.widthIn(min = 120.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("找零金額", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                                    Text(
                                                        "NT$ ${changeAmount.toInt()}",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 18.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Digital quick keypad pad for fast mobile click cash input!
                                        Text("熱快捷鍵櫃檯現金盤", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        FlowRow(
                                            maxItemsInEachRow = 4,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val exactVal = totalPrice.toInt()
                                            val fastKeys = listOf(
                                                "100", "200", "500", "1000"
                                            )

                                            // Exact amount key
                                            Surface(
                                                onClick = { viewModel.checkOutCashAmount.value = exactVal.toString() },
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    "剛好 ${exactVal}元",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            }

                                            fastKeys.forEach { k ->
                                                val intK = k.toInt()
                                                if (intK >= exactVal) {
                                                    Surface(
                                                        onClick = { viewModel.checkOutCashAmount.value = k },
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            "${k} 元",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(vertical = 8.dp),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }

                                            // Clear key
                                            Surface(
                                                onClick = { viewModel.checkOutCashAmount.value = "" },
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    "清除",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(vertical = 8.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Execute checkout button
                                Button(
                                    onClick = {
                                        viewModel.performCheckout()
                                        onDismiss() // Close Cart Dialog
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Inventory, contentDescription = "Checkout")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "確認收銀。列印明細 ❯",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================
// COMPONENT: RECEIPT SUCCESS & POPUP CHANGE DIALOG
// ============================
@Composable
fun ReceiptPreviewDialog(
    order: OrderWithItems,
    onDismiss: () -> Unit
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Done Ring
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "完成交易",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("交易成功！", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                Text("已開立電子發票收據與出紙明細", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(16.dp))

                // The virtual receipt ticket paper
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "--- 手作茶飲明細單 ---",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("銷售單號:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("B_TEA#${order.order.id}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("印表款時間:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(formatter.format(Date(order.order.timestamp)), fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("結帳方式:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(order.order.paymentMethod, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Scrollable list of items
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(0.7f)) {
                                    Text(item.drinkName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${item.size} / ${item.sweetness} / ${item.ice} (${item.toppings})", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Text("x${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.1f), textAlign = TextAlign.Center)
                                Text("NT$ ${(item.unitPrice * item.quantity).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(0.2f), textAlign = TextAlign.End)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        // Sum receipts
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("合計金額:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("NT$ ${order.order.totalPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("實收額:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("NT$ ${order.order.receivedAmount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("找零額:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("NT$ ${order.order.changeAmount.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("完成並開啟下一單", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

// ============================
// PANEL 2: COMPLETED ORDERS HISTORY
// ============================
@Composable
fun HistoryPanel(
    orders: List<OrderWithItems>,
    viewModel: PosViewModel
) {
    val searchHistoryQuery by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())

    val filteredOrders = remember(orders, searchHistoryQuery) {
        orders.filter { item ->
            searchHistoryQuery.isBlank() ||
            item.order.id.toString() == searchHistoryQuery ||
            item.order.paymentMethod.contains(searchHistoryQuery, ignoreCase = true) ||
            item.items.any { it.drinkName.contains(searchHistoryQuery, ignoreCase = true) }
        }
    }

    var selectedOrderForDetail by remember { mutableStateOf<OrderWithItems?>(null) }
    var showConfirmClearPrompt by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper section toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "銷貨歷史票據",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "檢視與退除歷史收銀項目 (${orders.size} 筆訂單)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (orders.isNotEmpty()) {
                IconButton(
                    onClick = { showConfirmClearPrompt = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "ClearAll")
                }
            }
        }

        // Search within history
        OutlinedTextField(
            value = searchHistoryQuery,
            onValueChange = { viewModel.setHistorySearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = { Text("搜尋訂單號、飲品名稱或付款方式...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchHistoryQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧾", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (orders.isEmpty()) "尚無結帳歷史紀錄" else "無符合篩選標準的訂單",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (orders.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.injectSampleHistory() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("匯入模擬測試帳單資料")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.order.id }) { item ->
                    val totalCups = item.items.sumOf { it.quantity }
                    Surface(
                        onClick = { selectedOrderForDetail = item },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "單號 B_TEA#${item.order.id}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (item.order.paymentMethod) {
                                            "現金" -> MaterialTheme.colorScheme.primaryContainer
                                            "信用卡" -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.tertiaryContainer
                                        },
                                        contentColor = when (item.order.paymentMethod) {
                                            "現金" -> MaterialTheme.colorScheme.primary
                                            "信用卡" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.tertiary
                                        }
                                    ) {
                                        Text(
                                            item.order.paymentMethod,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    item.items.joinToString { "${it.drinkName} x${it.quantity}" },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "時間: ${formatter.format(Date(item.order.timestamp))}  /  共 ${totalCups} 杯",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Row(
                                modifier = Modifier.weight(0.3f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "NT$ ${item.order.totalPrice.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail pop-up dialogue for history bills
    if (selectedOrderForDetail != null) {
        Dialog(onDismissRequest = { selectedOrderForDetail = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "帳單票據 B_TEA#${selectedOrderForDetail!!.order.id}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedOrderForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Text("交易明細列表:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedOrderForDetail!!.items.forEach { itm ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(0.7f)) {
                                    Text(itm.drinkName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "${itm.size} / ${itm.sweetness} / ${itm.ice}" +
                                        (if (itm.toppings != "無加料") " (${itm.toppings})" else ""),
                                        fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text("x${itm.quantity}", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f), textAlign = TextAlign.Center)
                                Text("NT$ ${(itm.unitPrice * itm.quantity).toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f), textAlign = TextAlign.End)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("付款方式:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(selectedOrderForDetail!!.order.paymentMethod, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("應付總計:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("NT$ ${selectedOrderForDetail!!.order.totalPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (selectedOrderForDetail!!.order.paymentMethod == "現金") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("實收額:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("NT$ ${selectedOrderForDetail!!.order.receivedAmount.toInt()}", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("找零額:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("NT$ ${selectedOrderForDetail!!.order.changeAmount.toInt()}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Delete order option (Refund)
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteOrder(selectedOrderForDetail!!.order.id)
                                selectedOrderForDetail = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("退單/退款", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedOrderForDetail = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("關閉檢視", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Confirm wipe database pop
    if (showConfirmClearPrompt) {
        AlertDialog(
            onDismissRequest = { showConfirmClearPrompt = false },
            title = { Text("確定要清除所有收銀紀錄？") },
            text = { Text("此操作不可逆，這將會永久刪除資料庫內所有的銷售紀錄歷史與報表統計。") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showConfirmClearPrompt = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確定清除", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearPrompt = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ============================
// PANEL 3: BUSINESS REVENUE ANALYTICS
// ============================
@Composable
fun AnalyticsPanel(
    orders: List<OrderWithItems>,
    viewModel: PosViewModel
) {
    val df = DecimalFormat("#,###")

    // Calculations based on local database records
    val totalRevenue = orders.sumOf { it.order.totalPrice }
    val totalOrdersCount = orders.size
    val averageBasket = if (totalOrdersCount > 0) totalRevenue / totalOrdersCount else 0.0

    // Analytical item sales
    val drinkSalesMap = remember(orders) {
        val map = mutableMapOf<String, Int>()
        orders.flatMap { it.items }.forEach { item ->
            map[item.drinkName] = (map[item.drinkName] ?: 0) + item.quantity
        }
        map.toList().sortedByDescending { it.second }.take(4)
    }

    val categorySalesMap = remember(orders) {
        val map = mutableMapOf<String, Double>()
        orders.flatMap { it.items }.forEach { item ->
            val revenue = item.unitPrice * item.quantity
            map[item.category] = (map[item.category] ?: 0.0) + revenue
        }
        map.toList().sortedByDescending { it.second }
    }

    val paymentSalesMap = remember(orders) {
        val map = mutableMapOf<String, Double>()
        orders.forEach { o ->
            map[o.order.paymentMethod] = (map[o.order.paymentMethod] ?: 0.0) + o.order.totalPrice
        }
        map.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Analytical screen header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "營收與銷售分析",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "自動整合 Room 本地資料庫歷史資訊及比率佔比",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Refresh or mock data triggers
            TextButton(
                onClick = { viewModel.injectSampleHistory() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重置演示資料", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("📊", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("尚未有任何銷售金額", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("當您在結帳大廳完成訂單結帳，大數據報表會在此處即時以圖表生成展現。", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.injectSampleHistory() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("一鍵匯入收銀演示數據", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: KPI Stats Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Total Revenue Primary Card
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            shadowElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text("今日累計營業額 (NT$)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        "NT$ ${df.format(totalRevenue)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 32.sp
                                    )
                                    Text("📈 手作熱銷中", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                                }
                            }
                        }

                        // Orders and ticket values secondary cards
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Order Count
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("成交單數", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("${totalOrdersCount} 筆單", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            // Average ticket
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("客單價", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("NT$ ${df.format(averageBasket.toInt())}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                // Section 2: Custom Category Sales Distribution Pie-Bar
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("熱銷大類佔比 (依金額)", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(14.dp))

                            // Draw a beautiful contiguous progress ratio bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                            ) {
                                val colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.outline
                                )

                                if (totalRevenue > 0) {
                                    categorySalesMap.forEachIndexed { index, pair ->
                                        val ratio = (pair.second / totalRevenue).toFloat()
                                        if (ratio > 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .weight(ratio)
                                                    .background(colors[index % colors.size])
                                            )
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Category Legend
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.outline
                                )

                                categorySalesMap.forEachIndexed { index, pair ->
                                    val percent = if (totalRevenue > 0) (pair.second / totalRevenue) * 100 else 0.0
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(colors[index % colors.size])
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(pair.first, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("NT$ ${df.format(pair.second.toInt())}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(String.format("%.1f%%", percent), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = colors[index % colors.size])
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Popular Drinks Sold Rankings
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("單品銷量明星排行 (Top 🏆)", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (drinkSalesMap.isEmpty()) {
                                Text("無足夠交易分析", color = MaterialTheme.colorScheme.secondary)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    drinkSalesMap.forEachIndexed { index, pair ->
                                        val totalVolume = drinkSalesMap.sumOf { it.second }.toFloat()
                                        val ratio = if (totalVolume > 0) pair.second / totalVolume else 0f

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Place tag number
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when (index) {
                                                            0 -> Color(0xFFECA36B)
                                                            1 -> Color(0xFFD6C0B3)
                                                            2 -> Color(0xFFE2CCB5)
                                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "${index + 1}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(pair.first, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    Text("${pair.second} 杯", fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Progress Line bar helper representation
                                                LinearProgressIndicator(
                                                    progress = ratio,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 4: Payment Popularity breakdown
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("付款方式佔比", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                paymentSalesMap.forEach { pair ->
                                    val ratio = if (totalRevenue > 0) pair.second / totalRevenue else 0.0
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(0.4f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = when (pair.first) {
                                                    "現金" -> Icons.Default.Payments
                                                    "信用卡" -> Icons.Default.CreditCard
                                                    else -> Icons.Default.QrCodeScanner
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(pair.first, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        // Horizontal bar
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(8.dp)
                                                .padding(horizontal = 8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(ratio.toFloat())
                                                    .background(MaterialTheme.colorScheme.primary)
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(0.4f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text("NT$ ${df.format(pair.second.toInt())}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(String.format("%.1f%%", ratio * 100), fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
