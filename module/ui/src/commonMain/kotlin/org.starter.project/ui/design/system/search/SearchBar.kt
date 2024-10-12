package org.starter.project.ui.design.system.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.starter.project.ui.design.system.theme.DesignSystemTheme
import org.starter.project.ui.resources.Res
import org.starter.project.ui.resources.search_bar_default_action
import org.starter.project.ui.resources.search_bar_default_placeholder

@Preview
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String = "",
    placeholder: String = stringResource(Res.string.search_bar_default_placeholder),
    onValueChange: (String) -> Unit,
    onTapClear: () -> Unit = {},
    onTapAction: () -> Unit = {}
) {
    val focusState = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .onFocusChanged {
                    focusState.value = it.isFocused
                },
            value = value,
            onValueChange = onValueChange,
            textStyle = DesignSystemTheme.typography.body2,
            singleLine = true,
            cursorBrush = SolidColor(DesignSystemTheme.colors.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = rememberVectorPainter(image = Icons.Default.Search),
                        contentDescription = null,
                        tint = Color.Gray
                    )
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                        if (value.isEmpty()) {
                            Text(
                                style = DesignSystemTheme.typography.body2,
                                color = Color.Gray,
                                text = placeholder
                            )
                        }
                    }
                    if (value.isNotEmpty()) {
                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .clickable { onTapClear() },
                            painter = rememberVectorPainter(image = Icons.Default.Close),
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions {
                focusManager.clearFocus()
                onTapAction()
            }
        )
        if (focusState.value) {
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onTapAction() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = DesignSystemTheme.typography.body2,
                color = Color.Black,
                text = stringResource(Res.string.search_bar_default_action),
                textAlign = TextAlign.Center
            )
        }
    }
}
