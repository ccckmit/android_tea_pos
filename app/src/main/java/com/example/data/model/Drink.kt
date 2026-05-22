package com.example.data.model

data class Drink(
    val id: String,
    val name: String,
    val basePrice: Double,
    val category: String, // 經典奶茶, 鮮果茶, 拿鐵/鮮奶茶, 特調原茶
    val description: String,
    val iconEmoji: String
)

object Menu {
    val categories = listOf("經典奶茶", "鮮果茶", "鮮奶茶/拿鐵", "特調原茶")

    val drinks = listOf(
        // 經典奶茶
        Drink("m1", "珍珠奶茶", 50.0, "經典奶茶", "店長推薦！手工Q彈黑糖蜜珍珠搭配香醇奶茶", "🧋"),
        Drink("m2", "椰果奶茶", 50.0, "經典奶茶", "香甜Q脆椰果，奶香四溢的咀嚼系最愛", "🥥"),
        Drink("m3", "布丁凍奶茶", 60.0, "經典奶茶", "整顆綿密香甜布丁融入香濃奶茶中", "🍮"),
        Drink("m4", "仙草凍奶茶", 50.0, "經典奶茶", "滑嫩古早味仙草，消暑清涼首選", "🖤"),
        Drink("m5", "經典雙響炮", 60.0, "經典奶茶", "珍珠、椰果一次滿足，雙重咀嚼口感", "🧋"),
        Drink("m6", "經典奶茶", 45.0, "經典奶茶", "傳統黃金比例香濃奶茶，醇厚口感", "☕"),

        // 鮮果茶
        Drink("f1", "冰釀鮮橙綠", 65.0, "鮮果茶", "現搾新鮮柳橙汁搭配清新茉莉綠茶", "🍊"),
        Drink("f2", "蜂蜜檸檬蘆薈", 55.0, "鮮果茶", "純正濃郁蜂蜜，微酸檸檬搭爽口蘆薈", "🍋"),
        Drink("f3", "香柚百香果綠", 60.0, "鮮果茶", "百香果粒與葡萄柚雙重果香，酸甜開胃", "🍹"),
        Drink("f4", "翡翠檸檬紅茶", 50.0, "鮮果茶", "現搾檸檬丁搭配莊園紅茶，夏日清爽", "🍋"),
        Drink("f5", "盛夏芒果綠", 65.0, "鮮果茶", "新鮮完熟芒果泥融入綠茶的果香饗宴", "🥭"),

        // 拿鐵鮮奶
        Drink("l1", "紅茶拿鐵", 60.0, "鮮奶茶/拿鐵", "大樹鮮乳與熟成莊園紅茶完美交融", "🥛"),
        Drink("l2", "綠茶拿鐵", 60.0, "鮮奶茶/拿鐵", "醇香鮮奶與頂級翡翠茉莉綠茶的純淨搭配", "🍵"),
        Drink("l3", "鐵觀音拿鐵", 65.0, "鮮奶茶/拿鐵", "炭焙香鐵觀音微苦甘甜搭配鮮奶極致溫潤", "🪵"),
        Drink("l4", "經典芋頭鮮奶", 70.0, "鮮奶茶/拿鐵", "大甲香Q手工芋泥與純鮮乳，濃郁香甜", "🍠"),
        Drink("l5", "香濃可可拿鐵", 65.0, "鮮奶茶/拿鐵", "比利時香濃黑可可與純鮮乳的滑順口感", "🍫"),

        // 特調原茶
        Drink("o1", "翡翠茉莉綠茶", 35.0, "特調原茶", "清新茉莉花香襯托高山優雅綠茶", "🍃"),
        Drink("o2", "極致莊園紅茶", 35.0, "特調原茶", "全發酵阿薩姆紅茶，茶湯紅潤甘醇", "🍂"),
        Drink("o3", "四季春青茶", 35.0, "特調原茶", "台灣特有四季青茶，清香甘甜不乾澀", "🍀"),
        Drink("o4", "炭焙烏龍茶", 35.0, "特調原茶", "厚重炭火慢焙，深邃木質茶香與回甘", "🪵"),
        Drink("o5", "多多綠茶", 50.0, "特調原茶", "兩瓶養樂多結合清爽茉莉綠茶，微酸百搭", "🥛")
    )
}

// Order item configuration draft for customization in active state
data class CartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val drink: Drink,
    val size: String = "大杯", // 中杯, 大杯
    val sweetness: String = "半糖", // 正常糖, 少糖, 半糖, 微糖, 無糖
    val ice: String = "微冰", // 正常冰, 少冰, 微冰, 去冰, 溫熱
    val toppings: List<String> = emptyList(), // 珍珠, 椰果, 布丁, 仙草凍, 蘆薈, 紅豆
    val quantity: Int = 1
) {
    val singlePrice: Double
        get() {
            var price = drink.basePrice
            if (size == "大杯") {
                price += 10.0
            }
            toppings.forEach { topping ->
                price += when (topping) {
                    "珍珠" -> 10.0
                    "椰果" -> 10.0
                    "布丁" -> 15.0
                    "仙草凍" -> 10.0
                    "蘆薈" -> 10.0
                    "紅豆" -> 10.0
                    else -> 10.0
                }
            }
            return price
        }

    val totalPrice: Double
        get() = singlePrice * quantity
}
