package com.positivefinancial.app.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Every list on these screens is already backed by a reactive database query,
 * so there is nothing to actually re-fetch on pull-to-refresh — this exists
 * purely to give the standard swipe-down gesture users expect, with a brief
 * spinner for reassurance that the screen is current.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                delay(400)
                isRefreshing = false
            }
        },
        modifier = modifier
    ) {
        content()
    }
}
