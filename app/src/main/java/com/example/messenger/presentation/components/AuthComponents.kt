package com.example.messenger.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AuthMethod {
    EMAIL, PHONE
}


@Composable
fun AuthMethodToggle(
    selectedMethod: AuthMethod,
    onMethodSelected: (AuthMethod) -> Unit
) {
    val containerColor = Color(0xFFCCCCCC).copy(alpha = 0.8f)
    val selectedColor = Color(0xFFA0A0A0)
    val unselectedTextColor = Color.DarkGray

    Row(
        modifier = Modifier
            .width(280.dp)
            .height(48.dp)
            .background(containerColor, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuthMethod.values().forEach { method ->
            ToggleButtonSegment(
                text = method.name.lowercase().replaceFirstChar { it.uppercase() },
                isSelected = selectedMethod == method,
                onClick = { onMethodSelected(method) },
                selectedBackgroundColor = selectedColor,
                selectedContentColor = Color.White, // Цвет текста для ВЫБРАННОЙ кнопки
                unselectedContentColor = unselectedTextColor, // Цвет текста для НЕВЫБРАННОЙ
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RowScope.ToggleButtonSegment(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedBackgroundColor: Color,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp)) // clip предпочтительнее для формы
            .background(if (isSelected) selectedBackgroundColor else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) selectedContentColor else unselectedContentColor,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun AuthInputTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(4.dp),
        singleLine = true
    )
}