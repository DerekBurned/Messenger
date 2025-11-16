package com.example.messenger.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.CustomNavigationBarItem( // Dodano RowScope jako odbiorcę
    imageRes: Int,
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    colors: NavigationBarItemColors
) {
    NavigationBarItem(
        icon = {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(32.dp)
                    .offset(y = 4.dp)
            )
        },
        label = label,
        selected = selected,
        onClick = onClick,
        colors = colors
    )
}