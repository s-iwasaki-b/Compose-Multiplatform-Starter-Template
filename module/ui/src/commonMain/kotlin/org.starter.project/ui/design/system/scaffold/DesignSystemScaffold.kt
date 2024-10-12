package org.starter.project.ui.design.system.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.starter.project.ui.design.system.loading.LoadingIndicator
import org.starter.project.ui.design.system.theme.DesignSystemTheme
import org.starter.project.ui.resources.Res
import org.starter.project.ui.resources.loading_failure_default_action_label
import org.starter.project.ui.resources.loading_failure_default_message
import org.starter.project.ui.shared.state.ScreenLoadingState
import org.starter.project.ui.shared.state.ScreenState

@Composable
fun DesignSystemScaffold(
    modifier: Modifier = Modifier,
    screenState: ScreenState,
    backgroundColor: Color = DesignSystemTheme.colors.background,
    onTapErrorActionButton: (() -> Unit)? = null,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(screenState.snackBarState) {
        val message = screenState.snackBarState?.message
        if (message.isNullOrEmpty()) return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message, duration = SnackbarDuration.Short
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            topBar?.let {
                Box(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .statusBarsPadding()
                ) {
                    topBar()
                }
            }
        },
        bottomBar = {
            bottomBar?.let {
                bottomBar()
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        backgroundColor = backgroundColor,
    ) { paddingValues ->
        if (screenState.screenLoadingState is ScreenLoadingState.Failure) {
            ErrorContent(
                modifier = if (topBar != null) Modifier.padding(top = 32.dp) else Modifier,
                title = stringResource(Res.string.loading_failure_default_message),
                message = screenState.screenLoadingState.throwable.message,
                actionLabel = stringResource(Res.string.loading_failure_default_action_label),
                onTapActionButton = onTapErrorActionButton
            )
        } else {
            content(paddingValues)
        }
    }

    AnimatedVisibility(
        visible = screenState.screenLoadingState.showLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    title: String,
    message: String?,
    actionLabel: String,
    onTapActionButton: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DesignSystemTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                style = DesignSystemTheme.typography.subtitle2,
                text = title
            )
            message?.let {
                Text(
                    style = DesignSystemTheme.typography.caption,
                    text = it
                )
            }
            onTapActionButton?.let {
                Button(
                    shape = CircleShape,
                    onClick = it
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}
