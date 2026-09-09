package com.positivefinancial.app.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Accounts : Screen("accounts")
    object Cards : Screen("cards")
    object Settings : Screen("settings")

    object AddEditTransaction : Screen("add_edit_transaction?transactionId={transactionId}&type={type}") {
        fun create(transactionId: Long = -1L, type: String = "EXPENSE") =
            "add_edit_transaction?transactionId=$transactionId&type=$type"
    }

    object Transfer : Screen("transfer")

    object AccountDetail : Screen("account_detail/{accountId}") {
        fun create(accountId: Long) = "account_detail/$accountId"
    }

    object AddEditAccount : Screen("add_edit_account?accountId={accountId}") {
        fun create(accountId: Long = -1L) = "add_edit_account?accountId=$accountId"
    }

    object CardDetail : Screen("card_detail/{accountId}") {
        fun create(accountId: Long) = "card_detail/$accountId"
    }

    object AddEditCard : Screen("add_edit_card?accountId={accountId}") {
        fun create(accountId: Long = -1L) = "add_edit_card?accountId=$accountId"
    }

    companion object {
        const val NEW_ID = -1L
    }
}
