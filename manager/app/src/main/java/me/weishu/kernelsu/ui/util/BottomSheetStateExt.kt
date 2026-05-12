package me.weishu.kernelsu.ui.util

import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable


@Composable
fun rememberBSState(
    skipPartiallyExpanded: Boolean = false,
    confirmValueChange: (SheetValue) -> Boolean = { true },
) =
    rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues =
            if (skipPartiallyExpanded) setOf(SheetValue.Hidden, SheetValue.Expanded)
            else setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded),
        confirmValueChange = confirmValueChange
    )
