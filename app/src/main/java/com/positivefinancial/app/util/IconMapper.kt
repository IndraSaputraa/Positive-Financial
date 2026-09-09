package com.positivefinancial.app.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun resolve(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Filled.Restaurant
        "local_grocery_store" -> Icons.Filled.LocalGroceryStore
        "directions_car" -> Icons.Filled.DirectionsCar
        "local_gas_station" -> Icons.Filled.LocalGasStation
        "shopping_bag" -> Icons.Filled.ShoppingBag
        "receipt_long" -> Icons.Filled.ReceiptLong
        "phone_android" -> Icons.Filled.PhoneAndroid
        "home" -> Icons.Filled.Home
        "favorite" -> Icons.Filled.Favorite
        "school" -> Icons.Filled.School
        "movie" -> Icons.Filled.Movie
        "groups" -> Icons.Filled.Groups
        "spa" -> Icons.Filled.Spa
        "volunteer_activism" -> Icons.Filled.VolunteerActivism
        "trending_up" -> Icons.Filled.TrendingUp
        "credit_card" -> Icons.Filled.CreditCard
        "work" -> Icons.Filled.Work
        "storefront" -> Icons.Filled.Storefront
        "laptop_mac" -> Icons.Filled.LaptopMac
        "card_giftcard" -> Icons.Filled.CardGiftcard
        "redeem" -> Icons.Filled.Redeem
        "savings" -> Icons.Filled.Savings
        "attach_money" -> Icons.Filled.AttachMoney
        "payments" -> Icons.Filled.Payments
        "account_balance" -> Icons.Filled.AccountBalance
        "account_balance_wallet" -> Icons.Filled.AccountBalanceWallet
        "swap_horiz" -> Icons.Filled.SwapHoriz
        "receipt" -> Icons.Filled.Receipt
        else -> Icons.Filled.Category
    }
}
